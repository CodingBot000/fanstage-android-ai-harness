package dev.fanstage.benchmark

import android.content.Intent
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Fixed 500-row fixture with the actual PlayerPanel and ChatPanel composables. */
@RunWith(AndroidJUnit4::class)
class ChatScrollBenchmark {
    @get:Rule val benchmark = MacrobenchmarkRule()

    @Test fun scrollWithPlayingFixture() = benchmark.measureRepeated(
        packageName = "dev.fanstage.android.benchmark",
        metrics = listOf(FrameTimingMetric()),
        compilationMode = CompilationMode.None(),
        iterations = 10,
        setupBlock = {
            // Reset each iteration to a known initial list position outside the measured section.
            killProcess()
            startActivityAndWait(Intent().setClassName(packageName, "dev.fanstage.android.BenchmarkRoomActivity"))
            check(device.wait(Until.hasObject(By.text("M6 fixture ready")), 60_000)) { "500 messages not ready" }
            check(device.wait(Until.hasObject(By.text("Playing")), 60_000)) { "synthetic player not playing" }
            check(device.wait(Until.hasObject(By.text("Synthetic message 1")), 10_000)) { "initial scroll position is not at the first row" }
        },
    ) {
        val list = checkNotNull(device.wait(Until.findObject(By.res("chat_messages")), 10_000))
        val bounds = list.visibleBounds
        val x = bounds.centerX()
        val top = bounds.top + bounds.height() / 4
        val bottom = bounds.bottom - bounds.height() / 4
        repeat(4) { check(device.swipe(x, bottom, x, top, 30)) }
        check(device.wait(Until.gone(By.text("Synthetic message 1")), 5_000)) { "forward gestures did not move the transcript" }
        repeat(6) { check(device.swipe(x, top, x, bottom, 30)) }
        check(device.wait(Until.hasObject(By.text("Synthetic message 1")), 5_000)) { "reverse gestures did not return to the first row" }
    }
}
