import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "dev.fanstage.android"
    compileSdk = 37

    defaultConfig {
        applicationId = "dev.fanstage.android"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Opt in only for local instrumentation against the installed release-like RC.
    if (providers.gradleProperty("fanstageRcInstrumentation").orNull == "true") {
        testBuildType = "rcLocal"
    }

    buildTypes {
        // Local verification only. Never use this debug-signed variant for distribution.
        create("rcLocal") {
            initWith(getByName("release"))
            applicationIdSuffix = ".rclocal"
            isDebuggable = false
            val existingDebugSigning = signingConfigs.getByName("debug")
            if (existingDebugSigning.storeFile?.isFile == true) {
                signingConfig = existingDebugSigning
            }
            matchingFallbacks += listOf("release")
        }
        create("benchmark") {
            initWith(getByName("release"))
            applicationIdSuffix = ".benchmark"
            isDebuggable = false
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
        }
    }
    buildFeatures { compose = true; buildConfig = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin { compilerOptions { jvmTarget.set(JvmTarget.JVM_17) } }

dependencies {
    add("benchmarkImplementation", libs.androidx.profileinstaller)
    implementation(project(":feature:live"))
    implementation(project(":feature:discover"))
    implementation(project(":feature:community"))
    implementation(project(":feature:media"))
    implementation(project(":feature:messages"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:data"))
    implementation(project(":core:model"))
    implementation(project(":core:artwork"))
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.media3.exoplayer)
    androidTestImplementation(libs.media3.hls)
    androidTestImplementation(libs.okhttp)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
