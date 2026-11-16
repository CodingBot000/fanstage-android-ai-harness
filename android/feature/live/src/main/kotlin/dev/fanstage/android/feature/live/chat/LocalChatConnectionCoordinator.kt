package dev.fanstage.android.feature.live.chat

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/** Composition-owned verification. An accepted transport belongs to ChatViewModel. */
internal class LocalChatConnectionCoordinator(
    private val scope: CoroutineScope,
    private val createTransport: (endpoint: String, token: String) -> ChatTransport = { endpoint, token ->
        RealChatTransport(endpoint, token, allowInsecure = true)
    },
) {
    data class State(val checking: Boolean = false, val configError: Boolean = false)
    private val mutableState = MutableStateFlow(State())
    val state = mutableState.asStateFlow()
    private var checkJob: Job? = null
    private var generation = 0L
    private var closed = false

    fun connect(
        endpoint: String,
        token: String,
        roomId: String,
        replaceTransport: (ChatTransport) -> Boolean,
        onConnected: () -> Unit,
    ) {
        if (closed || !scope.isActive || mutableState.value.checking) return
        val probe = try { createTransport(endpoint, token) }
        catch (_: Exception) {
            mutableState.value = State(configError = true)
            return
        }
        val epoch = ++generation
        mutableState.value = State(checking = true)
        // Install cleanup before returning to Compose, including immediate scope cancellation.
        checkJob = scope.launch(start = CoroutineStart.UNDISPATCHED) {
            try {
                val valid = try {
                    withTimeoutOrNull(10_000) {
                        coroutineScope {
                            val reader = async { probe.events(roomId).collect {} }
                            try {
                                requireNotNull(probe.connectionState).first { it == ChatConnectionStatus.CONNECTED }
                                true
                            } finally { reader.cancelAndJoin() }
                        }
                    } == true
                } catch (cancelled: CancellationException) { throw cancelled }
                catch (_: Exception) { false }
                currentCoroutineContext().ensureActive()
                if (closed || epoch != generation) return@launch
                if (valid) {
                    val next = createTransport(endpoint, token)
                    var handedOff = false
                    try {
                        currentCoroutineContext().ensureActive()
                        if (closed || epoch != generation) return@launch
                        // replaceTransport owns next on both acceptance and explicit rejection.
                        handedOff = true
                        if (replaceTransport(next)) onConnected()
                    } finally { if (!handedOff) next.close() }
                } else {
                    mutableState.value = mutableState.value.copy(configError = true)
                }
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) {
                if (!closed && epoch == generation) mutableState.value = mutableState.value.copy(configError = true)
            } finally {
                probe.close()
                if (epoch == generation) mutableState.value = mutableState.value.copy(checking = false)
            }
        }
    }

    fun cancel() {
        generation++
        checkJob?.cancel()
        checkJob = null
        mutableState.value = mutableState.value.copy(checking = false)
    }

    fun clearError() { mutableState.value = mutableState.value.copy(configError = false) }

    fun close() {
        closed = true
        cancel()
    }
}
