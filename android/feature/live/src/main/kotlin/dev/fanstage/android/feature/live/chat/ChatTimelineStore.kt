package dev.fanstage.android.feature.live.chat

import java.util.TreeMap

/** Main-thread-confined message state. The ViewModel owns jobs and transport lifetime. */
internal class ChatTimelineStore(
    private var roomId: String,
    private var budget: ChatMemoryBudget,
    private val onChanged: (gap: Boolean?) -> Unit,
) {
    private val budgetOwner = Any()
    private val confirmed = TreeMap<Long, ServerMessage>()
    private val pending = LinkedHashMap<String, ChatMessage>()
    private val uncertainIds = mutableSetOf<String>()
    var cursor = 0L
        private set
    var highestObserved = 0L
        private set
    var protocolIssue: ChatProtocolIssue? = null
        private set
    val invalidEvent: Boolean get() = protocolIssue != null
    val hasGap: Boolean get() = highestObserved > cursor
    val pendingCount: Int get() = pending.size
    val retainedMessageCount: Int get() = confirmed.size + pending.size
    val totalLogicalMessages: Int get() = budget.size
    val transportUncertain: Boolean get() = uncertainIds.isNotEmpty()

    fun reset(roomId: String, budget: ChatMemoryBudget = this.budget) {
        confirmed.values.forEach { this.budget.release(it.memoryKey(), budgetOwner) }
        pending.keys.forEach { this.budget.release(clientMemoryKey(this.roomId, it), budgetOwner) }
        confirmed.clear()
        pending.clear()
        uncertainIds.clear()
        cursor = 0
        highestObserved = 0
        protocolIssue = null
        this.roomId = roomId
        this.budget = budget
    }

    fun enqueue(id: String, text: String): Boolean {
        if (pending.size >= ChatLimits.RETAINED) return false
        if (!budget.acquire(clientMemoryKey(roomId, id), budgetOwner, true) {
                pending.remove(id)
                uncertainIds.remove(id)
                onChanged(null)
            }) return false
        pending[id] = ChatMessage("client:$id", id, null, text, true, Delivery.PENDING)
        trim()
        onChanged(null)
        return true
    }

    fun retry(id: String): String? {
        val message = pending[id] ?: return null
        if (message.delivery != Delivery.FAILED && id !in uncertainIds) return null
        pending[id] = message.copy(delivery = Delivery.PENDING, isUncertain = false)
        uncertainIds.remove(id)
        onChanged(null)
        return message.text
    }

    fun reject(id: String) {
        pending[id]?.let { pending[id] = it.copy(delivery = Delivery.FAILED) }
        onChanged(null)
    }

    fun markUncertain(id: String) {
        val message = pending[id] ?: return
        uncertainIds.add(id)
        pending[id] = message.copy(isUncertain = true)
        onChanged(null)
    }

    fun dismissFailed(id: String) {
        if (pending[id]?.delivery != Delivery.FAILED) return
        pending.remove(id)
        uncertainIds.remove(id)
        budget.release(clientMemoryKey(roomId, id), budgetOwner)
        onChanged(null)
    }

    fun accept(message: ServerMessage) {
        if (message.roomId != roomId) return
        if (message.serverSequence <= 0 || message.text.chatCodePoints() > ChatLimits.MAX_CODE_POINTS) {
            recordIssue(if (message.serverSequence <= 0) ChatProtocolIssue.INVALID_SEQUENCE else ChatProtocolIssue.INVALID_LENGTH)
            onChanged(null)
            return
        }
        val existing = confirmed[message.serverSequence]
        if ((existing != null && existing != message) || confirmed.values.any {
                (it.messageId == message.messageId || (message.clientMessageId != null && it.clientMessageId == message.clientMessageId)) && it.serverSequence != message.serverSequence
            }) {
            recordIssue(ChatProtocolIssue.CONFLICTING_MESSAGE)
            onChanged(null)
            return
        }
        val matchingPending = message.clientMessageId?.let { pending[it] }
        if (matchingPending != null && (!message.isOwn || matchingPending.text != message.text)) {
            recordIssue(ChatProtocolIssue.MISMATCHED_PENDING)
            onChanged(null)
            return
        }
        if (message.serverSequence <= cursor && existing == null) {
            // A late ACK can resolve a pending item even after its server row was evicted.
            message.clientMessageId?.let {
                if (pending.remove(it) != null) {
                    uncertainIds.remove(it)
                    budget.release(message.memoryKey(), budgetOwner)
                    onChanged(null)
                }
            }
            return
        }
        highestObserved = maxOf(highestObserved, message.serverSequence)
        if (!budget.acquire(message.memoryKey(), budgetOwner, false) {
                confirmed.remove(message.serverSequence)
                onChanged(hasGap)
            }) {
            onChanged(true)
            return
        }
        message.clientMessageId?.let { pending.remove(it); uncertainIds.remove(it) }
        confirmed[message.serverSequence] = message
        while (confirmed.containsKey(cursor + 1)) cursor++
        trim()
        onChanged(hasGap)
    }

    private fun recordIssue(issue: ChatProtocolIssue) { if (protocolIssue == null) protocolIssue = issue }

    fun displayedMessages(): List<ChatMessage> {
        val resolved = confirmed.values.map {
            ChatMessage(it.clientMessageId?.let { id -> "client:$id" } ?: "server:${it.messageId}", it.clientMessageId, it.serverSequence, it.text, it.isOwn, Delivery.SENT, sentAt = it.sentAt)
        }
        return (resolved + pending.values).takeLast(ChatLimits.DISPLAYED)
    }

    private fun trim() {
        while (confirmed.size + pending.size > ChatLimits.RETAINED && confirmed.isNotEmpty()) {
            val removed = confirmed.pollFirstEntry()?.value ?: break
            budget.release(removed.memoryKey(), budgetOwner)
        }
    }
}
