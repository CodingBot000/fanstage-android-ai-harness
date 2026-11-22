package dev.fanstage.android.core.model

typealias ArtistId = String
typealias ContentId = String
typealias PostId = String
typealias ConversationId = String

data class CommunityPost(val id: PostId, val artistId: ArtistId, val title: String, val body: String, val image: ArtworkKey, val liked: Boolean = false, val comments: List<String> = emptyList())
interface CommunityRepository {
    suspend fun posts(): List<CommunityPost>
    fun toggleLike(id: PostId): List<CommunityPost>
    fun addComment(id: PostId, text: String): List<CommunityPost>
}
data class LocalMessage(val id: Long, val text: String, val fromMe: Boolean)
data class LocalConversation(val artistId: ConversationId, val messages: List<LocalMessage>, val unread: Boolean = true)
interface MessageRepository {
    fun snapshot(): List<LocalConversation>
    fun markRead(artistId: ConversationId): List<LocalConversation>
    fun send(artistId: ConversationId, text: String): List<LocalConversation>
}
