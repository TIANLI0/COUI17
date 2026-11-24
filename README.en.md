# COUI17

A ColorOS 17 styled Jetpack Compose component library for Android: squircle corners, frosted glass, press light and spring motion, all drawn in Compose with no private OPPO SDK.

[简体中文](README.md) · [Design rules](docs/DESIGN.md) · [Component catalog](docs/COMPONENTS.md) · [Upstream](UPSTREAM.md)

## Modules

| Module | What it holds |
| :--- | :--- |
| `coui-ui` | 130+ components (buttons, cards, list rows, app bars, the floating navigation bar, dialogs, half-screen sheets, popups, pickers, sliders, search, date and time) and `COUITheme` |
| `coui-preference` | Settings rows: arrow, switch, radio, checkbox, slider, dropdown, input |
| `coui-patterns` | Page-level patterns: the `Page` scaffold, `GroupCard`, button tiers, `PanelSheet`, `GlassMenu`, floating glass surfaces, state views, `COUI17Theme` |
| `coui-icons` | ColorOS icons (`COUIIcons.*`) |
| `coui-blur` | Backdrop sampling and blur: `LayerBackdrop`, progressive blur, highlights |
| `coui-squircle` | Squircle shapes (`SquircleShape`) with pre-baked SDFs |
| `coui-shader` / `coui-core` | RuntimeShader wrappers, shared annotations and utilities |

`coui-patterns` is this repository's own module. The sources of the others come from upstream [Costben/coui17](https://github.com/Costben/coui17) and are followed automatically by CI (see [UPSTREAM.md](UPSTREAM.md)).

## Using it

Requirements: minSdk 23, compileSdk 37, Kotlin 2.4, JDK 21.

The library is not on Maven Central. Either add this repository as a git submodule and include the modules from your `settings.gradle.kts`:

```kotlin
listOf("coui-shader", "coui-blur", "coui-squircle", "coui-core", "coui-ui", "coui-preference", "coui-icons", "coui-patterns").forEach { name ->
    include(":$name")
    project(":$name").projectDir = file("third_party/COUI17/$name")
}
```

(merge the entries of [`gradle/libs.versions.toml`](gradle/libs.versions.toml) that the module build files reference into your own catalog), or publish to the local Maven repository:

```bash
./gradlew publishToMavenLocal
```

```kotlin
repositories { mavenLocal() }
dependencies { implementation("io.github.tianli0.coui17:coui-patterns:0.1.0") }
```

`coui-patterns` exposes `coui-ui`, `coui-preference` and `coui-icons` as `api`, so most apps depend on it alone.

## A page

```kotlin
setContent {
    COUI17Theme(accent = Accent.Blue) {
        var wifi by remember { mutableStateOf(true) }
        Page(title = "Settings", immersive = true) {
            LazyColumn(
                state = rememberPageListState(),
                modifier = Modifier.pageScroll(),
                contentPadding = pageListPadding(),
            ) {
                item { SectionHeader("Network") }
                item {
                    GroupCard {
                        SwitchPreference(
                            checked = wifi,
                            onCheckedChange = { wifi = it },
                            title = "WLAN",
                            cardListPosition = cardListPositionOf(0, 2),
                        )
                        ArrowPreference(title = "Mobile network", cardListPosition = cardListPositionOf(1, 2), onClick = { })
                    }
                }
            }
        }
    }
}
```

That gives the page colour, a collapsing large title, progressive glass where the list runs under the bar, COUI overscroll and the end-of-list haptic, tap-the-status-bar-to-top, a rounded group card and press feedback. Call `enableEdgeToEdge()` in the activity.

## Gallery

The `gallery` module shows every component family on real ColorOS 17 pages, in light and dark and with six accents: `./gradlew :gallery:installDebug`.

## Build

```bash
./gradlew :coui-patterns:testDebugUnitTest
./gradlew publishToMavenLocal
./gradlew :gallery:assembleDebug
```

Needs the Android SDK platform `android-37.2` and build-tools `37.0.0`.

## When glass is drawn

Glass uses `RuntimeShader`: Android 13 or newer, with system blur enabled. Otherwise every glass surface falls back to the opaque COUI fill with the same layout. By default the library never reflects into private `com.oplus.*` classes (`COUI17Theme(platformMaterials = false)`).

## License

Apache-2.0, see [LICENSE](LICENSE) and [NOTICE](NOTICE). Files in the `io.github.suqi8.coui.kmp.kit` packages of `coui-ui` are ported from COUI-Kit and stay under MPL-2.0 (headers kept; this repository is their source). Third-party dependencies: [THIRD-PARTY-LICENSES.md](THIRD-PARTY-LICENSES.md).

This project is not affiliated with OPPO. "ColorOS" is a trademark of its owner and is used here only to describe the design style.
