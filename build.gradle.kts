// Root build: plugin versions come from gradle/libs.versions.toml. Every library module is
// published as io.github.tianli0.coui17:<module>:<VERSION_NAME> (see gradle.properties).
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.androidKotlinMultiplatformLibrary) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.composeMultiplatform) apply false
}

val libraryGroup = providers.gradleProperty("GROUP").get()
val libraryVersion = providers.gradleProperty("VERSION_NAME").get()

subprojects {
    if (name == "gallery") return@subprojects
    group = libraryGroup
    version = libraryVersion
    apply(plugin = "maven-publish")

    extensions.configure<PublishingExtension> {
        publications.withType<MavenPublication>().configureEach {
            pom {
                name.set(project.name)
                description.set("COUI17: ColorOS 17 styled Jetpack Compose components (${project.name})")
                url.set("https://github.com/TIANLI0/COUI17")
                licenses {
                    license {
                        name.set("Apache-2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                    if (project.name == "coui-ui") {
                        license {
                            name.set("MPL-2.0")
                            url.set("https://www.mozilla.org/MPL/2.0/")
                            comments.set("Files in the io.github.suqi8.coui.kmp.kit packages")
                        }
                    }
                }
                scm { url.set("https://github.com/TIANLI0/COUI17") }
            }
        }
    }
}
