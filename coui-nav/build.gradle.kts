// Navigation runtime from upstream coui17 (`coui-nav`): a back-stack host whose push / pop, system
// predictive back and in-app swipe all drive one float, so the gesture follows the finger 1:1.
// Sources: upstream `src/{commonMain,androidMain}` + patches/ (see UPSTREAM.md). Android-only here.
plugins {
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatform)
}

kotlin {
    jvmToolchain(21)

    android {
        namespace = "io.github.suqi8.coui.kmp.nav"
        compileSdk {
            version = release(37) { minorApiLevel = 2 }
        }
        buildToolsVersion = "37.0.0"
        minSdk = 23
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":coui-squircle"))
            implementation(libs.androidx.navigationevent)
            implementation(libs.jetbrains.compose.foundation)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.kotlinx.serialization.json)
        }
    }
}
