package dev.fanstage.android.feature.live.chat

/** One logical message slot is shared by client and in-process fake, even when both reference it. */
class ChatMemoryBudget {
    private data class Lease(val pinned: Boolean, val evict: () -> Unit)
    private val entries = LinkedHashMap<String, MutableMap<Any, Lease>>()
    val size: Int get() = entries.size

    /** Pending sends are pinned; when full, evict the oldest non-pending logical message. */
    fun acquire(key: String, owner: Any, pinned: Boolean, evict: () -> Unit): Boolean {
        if (key !in entries) {
            if (entries.size >= ChatLimits.RETAINED) {
                val victim = entries.entries.firstOrNull { (_, leases) -> leases.values.none { it.pinned } } ?: return false
                entries.remove(victim.key)
                victim.value.values.toList().forEach { it.evict() }
            }
            entries[key] = mutableMapOf()
        }
        entries.getValue(key)[owner] = Lease(pinned, evict)
        return true
    }

    fun release(key: String, owner: Any) {
        val leases = entries[key] ?: return
        leases.remove(owner)
        if (leases.isEmpty()) entries.remove(key)
    }
}

/** Process-wide default ensures overlapping navigation entries share the same limit. */
internal object ProcessChatMemory { val budget = ChatMemoryBudget() }

internal fun clientMemoryKey(roomId: String, id: String) = "client:$roomId:$id"
internal fun ServerMessage.memoryKey(): String = clientMessageId?.let { clientMemoryKey(roomId, it) } ?: "server:$roomId:$messageId"
