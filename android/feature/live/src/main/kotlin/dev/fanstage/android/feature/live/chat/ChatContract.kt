package dev.fanstage.android.feature.live.chat

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/** IDs and sequence are synthetic in M3; user text is never logged or persisted. */
data class ServerMessage(
    val roomId: String,
    val messageId: String,
    val clientMessageId: String?,
    val serverSequence: Long,
    val text: String,
    val isOwn: Boolean,
    val sentAt: String? = null,
)

enum class Delivery { PENDING, SENT, FAILED }

data class ChatMessage(
    val key: String,
    val clientMessageId: String?,
    val sequence: Long?,
    val text: String,
    val isOwn: Boolean,
    val delivery: Delivery,
    val isUncertain: Boolean = false,
    val sentAt: String? = null,
)

sealed interface ChatEvent {
    val roomId: String
    data class Message(val message: ServerMessage) : ChatEvent { override val roomId get() = message.roomId }
    data class Ack(val message: ServerMessage) : ChatEvent { override val roomId get() = message.roomId }
}

/** A definite rejection; an unknown transport exception must not imply delivery failed. */
class SendRejected : Exception()
/** Authentication or a terminal setup failure, distinct from an uncertain send. */
open class ChatConnectionFailure : java.io.IOException("Chat connection unavailable")

enum class ChatConnectionStatus { CONNECTING, CONNECTED, RECONNECTING, DISCONNECTED }

interface ChatTransport {
    /** Shared only for an in-process transport; a remote transport has no retained local history. */
    val memoryBudget: ChatMemoryBudget? get() = null
    val connectionState: StateFlow<ChatConnectionStatus>? get() = null
    fun retryConnection() {}
    fun close() {}
    fun events(roomId: String): Flow<ChatEvent>
    suspend fun send(roomId: String, clientMessageId: String, text: String)
    /** Ascending exclusive cursor, capped page, returning only messages through upperSequence. */
    suspend fun fetchAfter(roomId: String, afterSequence: Long, upperSequence: Long, limit: Int): List<ServerMessage>
}

object ChatLimits {
    const val RETAINED = 1_000
    const val DISPLAYED = 500
    const val MAX_CODE_POINTS = 1_000
}

data class ChatUiState(
    val roomId: String,
    val messages: List<ChatMessage> = emptyList(),
    val gapUnresolved: Boolean = false,
    val sendBlocked: Boolean = false,
    val transportUncertain: Boolean = false,
    val inputTooLong: Boolean = false,
    val switchBlocked: Boolean = false,
    val connectionError: Boolean = false,
    val connectionStatus: ChatConnectionStatus? = null,
    val localServer: Boolean = false,
    val fictionalFixtures: Boolean = false,
    val draft: String = "",
)

internal fun String.chatCodePoints(): Int = codePointCount(0, length)
