plugins {
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatform)
}

kotlin {
    jvmToolchain(21)

    android {
        namespace = "io.github.suqi8.coui.kmp.icon"
        compileSdk {
            version = release(37) { minorApiLevel = 2 }
        }
        buildToolsVersion = "37.0.0"
        minSdk = 23
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":coui-core"))
            implementation(libs.jetbrains.compose.foundation)
        }
        androidMain.dependencies {

        }
    }
}
