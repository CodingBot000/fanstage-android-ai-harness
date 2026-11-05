package dev.fanstage.android.core.model

typealias ArtistId = String
typealias ContentId = String
typealias PostId = String
typealias ConversationId = String

enum class ArtworkKey { PAPER_HERO, PAPER_COVER, PAPER_FEED, BLUE_HERO, BLUE_COVER, BLUE_FEED, DICE_HERO, DICE_COVER, DICE_FEED }
data class FictionalArtist(val id: ArtistId, val name: String, val artwork: ArtworkKey, val cover: ArtworkKey, val feed: ArtworkKey)
data class DiscoverContent(val id: ContentId, val title: String, val artist: String, val artistId: ArtistId = id, val artwork: ArtworkKey = ArtworkKey.PAPER_HERO)
data class DiscoverData(val artists: List<FictionalArtist>, val content: List<DiscoverContent>)
interface DiscoverRepository { suspend fun load(): DiscoverData }
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
