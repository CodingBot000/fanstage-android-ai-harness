package dev.fanstage.android.core.model

enum class ArtworkKey {
    PAPER_HERO, PAPER_COVER, PAPER_FEED,
    BLUE_HERO, BLUE_COVER, BLUE_FEED,
    DICE_HERO, DICE_COVER, DICE_FEED,
}

data class FictionalArtist(
    val id: ArtistId,
    val name: String,
    val artwork: ArtworkKey,
    val cover: ArtworkKey,
    val feed: ArtworkKey,
)

data class CatalogContent(
    val id: ContentId,
    val title: String,
    val artist: String,
    val artistId: ArtistId = id,
    val artwork: ArtworkKey = ArtworkKey.PAPER_HERO,
)

data class CatalogSnapshot(val artists: List<FictionalArtist>, val content: List<CatalogContent>)

interface CatalogRepository {
    suspend fun load(): CatalogSnapshot
}

fun interface ArtistCatalog {
    fun artists(): List<FictionalArtist>
}
