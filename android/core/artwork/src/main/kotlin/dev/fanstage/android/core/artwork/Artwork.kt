package dev.fanstage.android.core.artwork
import dev.fanstage.android.core.model.ArtworkKey
fun artworkResource(key: ArtworkKey): Int = when (key) {
    ArtworkKey.PAPER_HERO -> R.drawable.paper_lantern_hero
    ArtworkKey.PAPER_COVER -> R.drawable.paper_lantern_cover
    ArtworkKey.PAPER_FEED -> R.drawable.paper_lantern_feed_01
    ArtworkKey.BLUE_HERO -> R.drawable.blue_pencil_hero
    ArtworkKey.BLUE_COVER -> R.drawable.blue_pencil_cover
    ArtworkKey.BLUE_FEED -> R.drawable.blue_pencil_feed_01
    ArtworkKey.DICE_HERO -> R.drawable.dice_garden_hero
    ArtworkKey.DICE_COVER -> R.drawable.dice_garden_cover
    ArtworkKey.DICE_FEED -> R.drawable.dice_garden_feed_01
}
