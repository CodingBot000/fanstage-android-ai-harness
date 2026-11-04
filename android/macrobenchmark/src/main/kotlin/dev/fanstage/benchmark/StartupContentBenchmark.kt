package dev.fanstage.benchmark

import android.os.Trace
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.ExperimentalMetricApi
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.TraceSectionMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Startup and UI-automator content detection, not a fully-drawn or first-frame timestamp. */
@OptIn(ExperimentalMetricApi::class)
@RunWith(AndroidJUnit4::class)
class StartupContentBenchmark {
    @get:Rule val benchmark = MacrobenchmarkRule()
    @Test fun coldStartupAndContent() = benchmark.measureRepeated(
        packageName = "dev.fanstage.android.benchmark",
        metrics = listOf(StartupTimingMetric(), TraceSectionMetric("M6LaunchToContent", targetPackageOnly = false)),
        compilationMode = CompilationMode.None(), startupMode = StartupMode.COLD, iterations = 10,
    ) {
        Trace.beginSection("M6LaunchToContent")
        try {
            startActivityAndWait()
            check(device.wait(Until.hasObject(By.text("Studio session")), 30_000)) { "room content not visible" }
        } finally { Trace.endSection() }
    }
}
