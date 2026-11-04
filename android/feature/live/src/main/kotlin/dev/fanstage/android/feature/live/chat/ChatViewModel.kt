package dev.fanstage.android.feature.live.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import java.util.TreeMap
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

class ChatViewModel(private var transport: ChatTransport, initialRoomId: String) : ViewModel() {
    private val mutableState = MutableStateFlow(ChatUiState(initialRoomId))
    val uiState: StateFlow<ChatUiState> = mutableState.asStateFlow()
    private var budget = transport.memoryBudget ?: ProcessChatMemory.budget
    private val budgetOwner = Any()
    private val confirmed = TreeMap<Long, ServerMessage>()
    private val pending = LinkedHashMap<String, ChatMessage>()
    private val uncertainIds = mutableSetOf<String>()
    private var cursor = 0L
    private var highestObserved = 0L
    private var generation = 0L
    private var roomJob: Job? = null
    private var connectionJob: Job? = null
    private var recoveryJob: Job? = null
    private val sendJobs = mutableSetOf<Job>()
    private var invalidEvent = false
    internal val retainedMessageCount: Int get() = confirmed.size + pending.size
    internal val totalLogicalMessages: Int get() = budget.size
    internal val confirmedCursor: Long get() = cursor

    init { openRoom(initialRoomId) }

    fun openRoom(roomId: String) {
        generation++
        roomJob?.cancel()
        connectionJob?.cancel()
        recoveryJob?.cancel()
        recoveryJob = null
        sendJobs.toList().forEach { it.cancel() }
        sendJobs.clear()
        confirmed.values.forEach { budget.release(it.memoryKey(), budgetOwner) }
        pending.keys.forEach { budget.release(clientMemoryKey(mutableState.value.roomId, it), budgetOwner) }
        confirmed.clear()
        pending.clear()
        uncertainIds.clear()
        cursor = 0
        highestObserved = 0
        invalidEvent = false
        mutableState.value = ChatUiState(roomId, localServer = transport is RealChatTransport)
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
                    accept(message)
                    if (highestObserved > cursor) recover()
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
        if (pending.isNotEmpty()) {
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
        confirmed.values.forEach { budget.release(it.memoryKey(), budgetOwner) }
        confirmed.clear()
        transport.close()
        transport = next
        budget = next.memoryBudget ?: ProcessChatMemory.budget
        openRoom(room)
        return true
    }

    fun useOfflineFake(): Boolean {
        if (pending.isNotEmpty()) {
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
        if (pending.size >= ChatLimits.RETAINED) {
            publish(sendBlocked = true)
            return false
        }
        mutableState.value = mutableState.value.copy(inputTooLong = false)
        val id = UUID.randomUUID().toString()
        val key = clientMemoryKey(mutableState.value.roomId, id)
        if (!budget.acquire(key, budgetOwner, true) { pending.remove(id); uncertainIds.remove(id); publish() }) {
            publish(sendBlocked = true)
            return false
        }
        pending[id] = ChatMessage("client:$id", id, null, clean, true, Delivery.PENDING)
        trim()
        publish()
        dispatch(id, clean)
        return true
    }

    fun retry(clientMessageId: String) {
        val message = pending[clientMessageId] ?: return
        if (message.delivery != Delivery.FAILED && clientMessageId !in uncertainIds) return
        pending[clientMessageId] = message.copy(delivery = Delivery.PENDING, isUncertain = false)
        uncertainIds.remove(clientMessageId)
        publish()
        dispatch(clientMessageId, message.text)
    }

    /** Only an explicit user action can remove a definitively failed message. */
    fun dismissFailed(clientMessageId: String) {
        if (pending[clientMessageId]?.delivery != Delivery.FAILED) return
        pending.remove(clientMessageId)
        uncertainIds.remove(clientMessageId)
        budget.release(clientMemoryKey(mutableState.value.roomId, clientMessageId), budgetOwner)
        publish()
    }

    fun retryGap() { if (highestObserved > cursor) recover() }

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
                    pending[id]?.let { pending[id] = it.copy(delivery = Delivery.FAILED) }
                    publish()
                }
            } catch (_: Exception) {
                currentCoroutineContext().ensureActive()
                // A timeout/unknown outcome is not proof of rejection; keep Pending.
                if (epoch == generation && pending.containsKey(id)) {
                    uncertainIds.add(id)
                    pending[id]?.let { pending[id] = it.copy(isUncertain = true) }
                    publish()
                }
            }
        }
        sendJobs.add(job)
        job.invokeOnCompletion { sendJobs.remove(job) }
    }

    private fun accept(message: ServerMessage) {
        if (message.roomId != mutableState.value.roomId) return
        if (message.serverSequence <= 0 || message.text.chatCodePoints() > ChatLimits.MAX_CODE_POINTS) {
            invalidEvent = true
            publish()
            return
        }
        val existing = confirmed[message.serverSequence]
        if ((existing != null && existing != message) || confirmed.values.any {
                (it.messageId == message.messageId || (message.clientMessageId != null && it.clientMessageId == message.clientMessageId)) && it.serverSequence != message.serverSequence
            }) {
            invalidEvent = true
            publish()
            return
        }
        val matchingPending = message.clientMessageId?.let { pending[it] }
        if (matchingPending != null && (!message.isOwn || matchingPending.text != message.text)) {
            invalidEvent = true
            publish()
            return
        }
        if (message.serverSequence <= cursor && existing == null) {
            // A late ACK can still resolve a retained pending item after its server row was evicted.
            message.clientMessageId?.let {
                if (pending.remove(it) != null) { uncertainIds.remove(it); budget.release(message.memoryKey(), budgetOwner); publish() }
            }
            return
        }
        highestObserved = maxOf(highestObserved, message.serverSequence)
        if (!budget.acquire(message.memoryKey(), budgetOwner, false) {
                confirmed.remove(message.serverSequence)
                publish(gap = highestObserved > cursor)
            }) {
            publish(gap = true)
            return
        }
        message.clientMessageId?.let { pending.remove(it); uncertainIds.remove(it) }
        confirmed[message.serverSequence] = message
        while (confirmed.containsKey(cursor + 1)) cursor++
        trim()
        publish(gap = highestObserved > cursor)
    }

    private fun recover() {
        if (recoveryJob?.isActive == true) return
        val epoch = generation
        val room = mutableState.value.roomId
        recoveryJob = viewModelScope.launch {
            try {
                while (epoch == generation && cursor < highestObserved) {
                    val before = cursor
                    val batch = transport.fetchAfter(room, cursor, highestObserved, ChatLimits.RETAINED)
                    currentCoroutineContext().ensureActive()
                    if (epoch != generation) return@launch
                    batch.sortedBy { it.serverSequence }.forEach { accept(it) }
                    if (cursor == before) { publish(gap = true); return@launch }
                }
                if (epoch == generation) publish(gap = false)
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) {
                currentCoroutineContext().ensureActive()
                if (epoch == generation) publish(gap = true)
            }
        }
    }

    private fun trim() {
        while (confirmed.size + pending.size > ChatLimits.RETAINED && confirmed.isNotEmpty()) {
            val removed = confirmed.pollFirstEntry()?.value ?: break
            budget.release(removed.memoryKey(), budgetOwner)
        }
    }

    private fun publish(gap: Boolean = mutableState.value.gapUnresolved, sendBlocked: Boolean = false) {
        // Pending messages follow confirmed ones; only convert the portion that
        // can appear in the bounded display tail.
        val visiblePending = pending.values.toList().takeLast(ChatLimits.DISPLAYED)
        val remaining = ChatLimits.DISPLAYED - visiblePending.size
        val visibleConfirmed = confirmed.descendingMap().values.asSequence()
            .take(remaining).toList().asReversed()
        val resolved = visibleConfirmed.map {
            ChatMessage(it.clientMessageId?.let { id -> "client:$id" } ?: "server:${it.messageId}", it.clientMessageId, it.serverSequence, it.text, it.isOwn, Delivery.SENT)
        }
        mutableState.value = mutableState.value.copy(
            messages = resolved + visiblePending,
            gapUnresolved = gap || invalidEvent,
            sendBlocked = sendBlocked,
            transportUncertain = uncertainIds.isNotEmpty(),
        )
    }

    override fun onCleared() {
        generation++
        roomJob?.cancel()
        connectionJob?.cancel()
        recoveryJob?.cancel()
        sendJobs.toList().forEach { it.cancel() }
        confirmed.values.forEach { budget.release(it.memoryKey(), budgetOwner) }
        pending.keys.forEach { budget.release(clientMemoryKey(mutableState.value.roomId, it), budgetOwner) }
        confirmed.clear()
        pending.clear()
        uncertainIds.clear()
        transport.close()
        super.onCleared()
    }

    class Factory(private val roomId: String, private val transport: ChatTransport? = null) : ViewModelProvider.Factory {
        // Do not allocate a second fake store when a retained ViewModel is reused after recreation.
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            modelClass.cast(ChatViewModel(transport ?: FakeChatTransport(roomId, memoryBudget = ProcessChatMemory.budget), roomId))!!
    }
}
