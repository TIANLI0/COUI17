// :coui-patterns — page-level ColorOS 17 patterns built from the coui components: the page
// scaffold, group cards, button tiers, half-screen panels, the glass menu, floating glass surfaces,
// state views and the theme entry point. This repository's own module (Apache-2.0).
plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeCompiler)
}

android {
    namespace = "io.github.tianli0.coui17.patterns"
    compileSdk {
        version = release(37) { minorApiLevel = 2 }
    }
    buildToolsVersion = "37.0.0"
    defaultConfig {
        minSdk = 23
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        compose = true
    }
    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    api(project(":coui-ui"))
    api(project(":coui-preference"))
    api(project(":coui-icons"))
    api(libs.androidx.compose.ui)
    api(libs.androidx.compose.foundation)
    implementation(libs.androidx.core.ktx)

    testImplementation(libs.junit4)
}

afterEvaluate {
    extensions.configure<PublishingExtension> {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
            }
        }
    }
}
