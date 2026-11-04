package dev.fanstage.android.feature.live

import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Test

private class FakePlayerController : PlayerController {
    override val state = MutableStateFlow<PlaybackState>(PlaybackState.Loading)
    var playCount = 0
    var pauseCount = 0
    var releaseCount = 0
    override fun play() { playCount++; state.value = PlaybackState.Playing }
    override fun pause() { pauseCount++; state.value = PlaybackState.Paused }
    override fun retry() { state.value = PlaybackState.Loading }
    override fun release() { releaseCount++ }
}

class PlaybackSessionTest {
    @Test fun backgroundPauseRequiresManualResumeAndReleaseIsIdempotent() {
        val fake = FakePlayerController()
        val session = PlaybackSession(fake)
        session.onResume()
        session.onResume()
        assertEquals(1, fake.playCount)
        session.onPause()
        session.onResume()
        assertEquals(1, fake.playCount)
        assertEquals(1, fake.pauseCount)
        fake.play()
        session.release()
        session.release()
        session.onResume()
        session.onPause()
        assertEquals(1, fake.releaseCount)
        assertEquals(2, fake.playCount)
        assertEquals(1, fake.pauseCount)
    }

    @Test fun recreatedSessionDoesNotAutoplayWhenAlreadyPaused() {
        val fake = FakePlayerController()
        val session = PlaybackSession(fake, initiallyAutoPlay = false)
        session.onResume()
        assertEquals(0, fake.playCount)
    }
}
