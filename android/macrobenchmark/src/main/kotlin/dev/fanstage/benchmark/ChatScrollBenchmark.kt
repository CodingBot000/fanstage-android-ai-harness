package dev.fanstage.benchmark

import android.content.Intent
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
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
            startActivityAndWait(Intent().setClassName(packageName, "dev.fanstage.android.BenchmarkRoomActivity"))
            check(device.wait(Until.hasObject(By.text("M6 fixture ready")), 30_000)) { "500 messages not ready" }
            check(device.wait(Until.hasObject(By.text("Playing")), 30_000)) { "synthetic player not playing" }
        },
    ) {
        val list = checkNotNull(device.wait(Until.findObject(By.res("chat_messages")), 10_000))
        list.setGestureMargin(device.displayWidth / 10)
        repeat(5) { list.fling(Direction.DOWN); list.fling(Direction.UP) }
    }
}
