package dev.fanstage.android.feature.live.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ChatViewModel(private var transport: ChatTransport, initialRoomId: String) : ViewModel() {
    @Inject constructor(savedStateHandle: SavedStateHandle, factory: ChatTransportFactory) : this(
        factory.create(requireNotNull(savedStateHandle["roomId"]) { "roomId is required" }),
        requireNotNull(savedStateHandle["roomId"]) { "roomId is required" },
    )

    private val mutableState = MutableStateFlow(ChatUiState(initialRoomId))
    val uiState: StateFlow<ChatUiState> = mutableState.asStateFlow()
    private val timeline = ChatTimelineStore(initialRoomId, transport.memoryBudget ?: ProcessChatMemory.budget) { gap ->
        publish(gap = gap ?: mutableState.value.gapUnresolved)
    }
    private var generation = 0L
    private var roomJob: Job? = null
    private var connectionJob: Job? = null
    private var recoveryJob: Job? = null
    private val sendJobs = mutableSetOf<Job>()
    internal val retainedMessageCount: Int get() = timeline.retainedMessageCount
    internal val totalLogicalMessages: Int get() = timeline.totalLogicalMessages
    internal val confirmedCursor: Long get() = timeline.cursor

    init { openRoom(initialRoomId) }

    fun openRoom(roomId: String) {
        generation++
        roomJob?.cancel()
        connectionJob?.cancel()
        recoveryJob?.cancel()
        recoveryJob = null
        sendJobs.toList().forEach { it.cancel() }
        sendJobs.clear()
        timeline.reset(roomId, transport.memoryBudget ?: ProcessChatMemory.budget)
        mutableState.value = ChatUiState(roomId, localServer = transport is RealChatTransport, fictionalFixtures = transport is FakeChatTransport)
        val epoch = generation
        connectionJob = transport.connectionState?.let { connection ->
            viewModelScope.launch(start = CoroutineStart.UNDISPATCHED) {
                connection.collect { status ->
                    if (epoch == generation) mutableState.value = mutableState.value.copy(connectionStatus = status)
                }
            }
        }
        roomJob = viewModelScope.launch(start = CoroutineStart.UNDISPATCHED) {
            try {
                transport.events(roomId).collect { event ->
                    currentCoroutineContext().ensureActive()
                    if (epoch != generation || event.roomId != mutableState.value.roomId) return@collect
                    val message = when (event) { is ChatEvent.Message -> event.message; is ChatEvent.Ack -> event.message }
                    timeline.accept(message)
                    if (timeline.hasGap) recover()
                }
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (_: ChatConnectionFailure) {
                currentCoroutineContext().ensureActive()
                if (epoch == generation) mutableState.value = mutableState.value.copy(connectionError = true)
            }
            catch (_: Exception) {
                currentCoroutineContext().ensureActive()
                if (epoch == generation) mutableState.value = mutableState.value.copy(gapUnresolved = true)
            }
        }
    }

    /** Switching is explicit and must not silently discard unconfirmed messages. */
    fun replaceTransport(next: ChatTransport): Boolean {
        if (timeline.pendingCount > 0) {
            next.close()
            mutableState.value = mutableState.value.copy(switchBlocked = true)
            return false
        }
        val room = mutableState.value.roomId
        generation++
        roomJob?.cancel()
        connectionJob?.cancel()
        recoveryJob?.cancel()
        sendJobs.toList().forEach { it.cancel() }
        timeline.reset(room, next.memoryBudget ?: ProcessChatMemory.budget)
        transport.close()
        transport = next
        openRoom(room)
        return true
    }

    fun useOfflineFake(): Boolean {
        if (timeline.pendingCount > 0) {
            mutableState.value = mutableState.value.copy(switchBlocked = true)
            return false
        }
        return replaceTransport(FakeChatTransport(mutableState.value.roomId, memoryBudget = ProcessChatMemory.budget))
    }

    fun updateDraft(text: String) {
        if (text.chatCodePoints() <= ChatLimits.MAX_CODE_POINTS) {
            mutableState.value = mutableState.value.copy(draft = text, inputTooLong = false)
        } else {
            mutableState.value = mutableState.value.copy(inputTooLong = true)
        }
    }

    fun send(text: String): Boolean {
        val clean = text.trim()
        if (clean.isEmpty()) return false
        if (clean.chatCodePoints() > ChatLimits.MAX_CODE_POINTS) {
            mutableState.value = mutableState.value.copy(inputTooLong = true)
            return false
        }
        if (timeline.pendingCount >= ChatLimits.RETAINED) {
            publish(sendBlocked = true)
            return false
        }
        mutableState.value = mutableState.value.copy(inputTooLong = false)
        val id = UUID.randomUUID().toString()
        if (!timeline.enqueue(id, clean)) {
            publish(sendBlocked = true)
            return false
        }
        dispatch(id, clean)
        return true
    }

    fun retry(clientMessageId: String) {
        val text = timeline.retry(clientMessageId) ?: return
        dispatch(clientMessageId, text)
    }

    /** Only an explicit user action can remove a definitively failed message. */
    fun dismissFailed(clientMessageId: String) = timeline.dismissFailed(clientMessageId)

    fun retryGap() { if (timeline.hasGap) recover() }

    fun retryConnection() { transport.retryConnection() }

    private fun dispatch(id: String, text: String) {
        val epoch = generation
        val room = mutableState.value.roomId
        val job = viewModelScope.launch {
            try {
                transport.send(room, id, text)
                currentCoroutineContext().ensureActive()
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (_: SendRejected) {
                currentCoroutineContext().ensureActive()
                if (epoch == generation) {
                    timeline.reject(id)
                }
            } catch (_: Exception) {
                currentCoroutineContext().ensureActive()
                // A timeout/unknown outcome is not proof of rejection; keep Pending.
                if (epoch == generation) timeline.markUncertain(id)
            }
        }
        sendJobs.add(job)
        job.invokeOnCompletion { sendJobs.remove(job) }
    }

    private fun recover() {
        if (recoveryJob?.isActive == true) return
        val epoch = generation
        val room = mutableState.value.roomId
        recoveryJob = viewModelScope.launch {
            try {
                while (epoch == generation && timeline.hasGap) {
                    val before = timeline.cursor
                    val batch = transport.fetchAfter(room, timeline.cursor, timeline.highestObserved, ChatLimits.RETAINED)
                    currentCoroutineContext().ensureActive()
                    if (epoch != generation) return@launch
                    batch.sortedBy { it.serverSequence }.forEach { timeline.accept(it) }
                    if (timeline.cursor == before) { publish(gap = true); return@launch }
                }
                if (epoch == generation) publish(gap = false)
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) {
                currentCoroutineContext().ensureActive()
                if (epoch == generation) publish(gap = true)
            }
        }
    }

    private fun publish(gap: Boolean = mutableState.value.gapUnresolved, sendBlocked: Boolean = false) {
        mutableState.value = mutableState.value.copy(
            messages = timeline.displayedMessages(),
            gapUnresolved = gap || timeline.invalidEvent,
            sendBlocked = sendBlocked,
            transportUncertain = timeline.transportUncertain,
        )
    }

    override fun onCleared() {
        generation++
        roomJob?.cancel()
        connectionJob?.cancel()
        recoveryJob?.cancel()
        sendJobs.toList().forEach { it.cancel() }
        timeline.reset(mutableState.value.roomId)
        transport.close()
        super.onCleared()
    }

    class Factory(private val roomId: String, private val transport: ChatTransport) : ViewModelProvider.Factory {
        // Do not allocate a second fake store when a retained ViewModel is reused after recreation.
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            modelClass.cast(ChatViewModel(transport, roomId))!!
    }
}
