// COUI17: a ColorOS 17 styled Jetpack Compose component library for Android.
pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("""com\.android.*""")
                includeGroupByRegex("""com\.google.*""")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google {
            content {
                includeGroupByRegex("""com\.android.*""")
                includeGroupByRegex("""com\.google.*""")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
    }
}

rootProject.name = "COUI17"

// Foundation: shaders, blur / glass, squircle shapes, shared annotations.
include(":coui-shader")
include(":coui-blur")
include(":coui-squircle")
include(":coui-core")
// Components, preference rows, icons.
include(":coui-ui")
include(":coui-preference")
include(":coui-icons")
// Page-level patterns built from the components (this repository's own module).
include(":coui-patterns")
// Demo application.
include(":gallery")
