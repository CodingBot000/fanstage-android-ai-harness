import org.jetbrains.kotlin.gradle.dsl.JvmTarget
plugins { alias(libs.plugins.ksp); alias(libs.plugins.android.library); alias(libs.plugins.kotlin.compose) }
android {
    namespace = "dev.fanstage.android.feature.media"
    compileSdk = 37
    defaultConfig { minSdk = 24 }
    buildFeatures { compose = true }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
}
kotlin { compilerOptions { jvmTarget.set(JvmTarget.JVM_17) } }
dependencies {
    implementation(libs.hilt.android); ksp(libs.hilt.compiler)
    implementation(project(":core:model")); implementation(project(":core:designsystem")); implementation(project(":core:artwork"))
    implementation(platform(libs.androidx.compose.bom)); implementation(libs.androidx.compose.ui); implementation(libs.androidx.compose.foundation); implementation(libs.androidx.compose.material3); implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.lifecycle.runtime.compose); implementation(libs.androidx.lifecycle.viewmodel.ktx); implementation(libs.kotlinx.coroutines.core)
}
