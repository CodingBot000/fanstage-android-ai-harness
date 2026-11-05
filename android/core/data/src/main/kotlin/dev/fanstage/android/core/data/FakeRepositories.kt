package dev.fanstage.android.core.data

import dev.fanstage.android.core.model.*

internal val fictionalArtists: List<FictionalArtist> = listOf(
    FictionalArtist("paper-lantern", "Paper Lantern", ArtworkKey.PAPER_HERO, ArtworkKey.PAPER_COVER, ArtworkKey.PAPER_FEED),
    FictionalArtist("blue-pencil", "Blue Pencil", ArtworkKey.BLUE_HERO, ArtworkKey.BLUE_COVER, ArtworkKey.BLUE_FEED),
    FictionalArtist("dice-garden", "Dice Garden", ArtworkKey.DICE_HERO, ArtworkKey.DICE_COVER, ArtworkKey.DICE_FEED),
)
class FakeDiscoverRepository : DiscoverRepository {
    override suspend fun load() = DiscoverData(fictionalArtists, listOf(
        DiscoverContent("studio-session", "Studio session", "Paper Lantern", "paper-lantern", ArtworkKey.PAPER_HERO),
        DiscoverContent("sketch-break", "Afternoon sketch break", "Blue Pencil", "blue-pencil", ArtworkKey.BLUE_HERO),
        DiscoverContent("tabletop-night", "Tabletop night", "Dice Garden", "dice-garden", ArtworkKey.DICE_HERO),
    ))
}
class FakeCommunityRepository : CommunityRepository {
    private var state = fictionalArtists.map { CommunityPost("post-${it.id}", it.id, it.name, "A little moment from our world.", it.feed) }
    override suspend fun posts() = state.toList()
    override fun toggleLike(id: PostId): List<CommunityPost> { state = state.map { if (it.id == id) it.copy(liked = !it.liked) else it }; return state.toList() }
    override fun addComment(id: PostId, text: String): List<CommunityPost> { val value = text.trim(); if (value.isNotEmpty()) state = state.map { if (it.id == id) it.copy(comments = it.comments + value) else it }; return state.toList() }
}
class FakeMessageRepository : MessageRepository {
    private var state = fictionalArtists.map { LocalConversation(it.id, listOf(LocalMessage(0, "Welcome to our space!", false))) }
    override fun snapshot() = state.toList()
    override fun markRead(artistId: ConversationId): List<LocalConversation> { state = state.map { if (it.artistId == artistId) it.copy(unread = false) else it }; return snapshot() }
    override fun send(artistId: ConversationId, text: String): List<LocalConversation> { val value = text.trim(); if (value.isNotEmpty()) state = state.map { if (it.artistId == artistId) it.copy(messages = it.messages + LocalMessage((it.messages.maxOfOrNull { m -> m.id } ?: 0) + 1, value, true)) else it }; return snapshot() }
}
