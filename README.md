# COUI17

ColorOS 17 风格的 Jetpack Compose 组件库（Android）。超椭圆圆角、毛玻璃材质、按压光效、弹性动效，全部用 Compose 绘制，不依赖 OPPO 的私有 SDK。

[English](README.en.md) · [设计规范](docs/DESIGN.md) · [组件目录](docs/COMPONENTS.md) · [上游关系](UPSTREAM.md)

## 模块

| 模块 | 内容 |
| :--- | :--- |
| `coui-ui` | 130 多个组件：按钮、卡片、列表行、标题栏、底部悬浮导航、对话框、半屏面板、弹出菜单、选择器、滑块、搜索、日期时间等，以及 `COUITheme` |
| `coui-preference` | 设置页的行：箭头、开关、单选、复选、滑块、下拉、输入等偏好项 |
| `coui-patterns` | 页面级模式：`Page` 页面骨架、`GroupCard` 分组卡片、按钮分级、`PanelSheet` 面板、`GlassMenu` 玻璃菜单、悬浮玻璃材质、状态页、`COUI17Theme` |
| `coui-icons` | ColorOS 图标（`COUIIcons.*`） |
| `coui-nav` | 页面栈导航：`NavDisplay`，推入、返回、系统预测性返回和页内右滑驱动同一个位置，手势跟手（可选） |
| `coui-blur` | 背景采样与模糊：`LayerBackdrop`、渐进模糊、高光 |
| `coui-squircle` | 超椭圆形状（`SquircleShape`），带预烘焙的 SDF |
| `coui-shader` / `coui-core` | RuntimeShader 封装、公共注解与工具 |

`coui-patterns` 是本仓库自己的模块，其余模块的源码来自上游 [Costben/coui17](https://github.com/Costben/coui17)，并由 CI 自动跟进（见 [UPSTREAM.md](UPSTREAM.md)）。

## 使用

要求：minSdk 23，compileSdk 37，Kotlin 2.4，JDK 21。

目前没有发布到 Maven Central。两种接入方式：

**作为源码（推荐，随时跟进）**：把本仓库加为 git submodule，然后在你的 `settings.gradle.kts` 里引入模块：

```kotlin
listOf("coui-shader", "coui-blur", "coui-squircle", "coui-core", "coui-ui", "coui-preference", "coui-icons", "coui-nav", "coui-patterns").forEach { name ->
    include(":$name")
    project(":$name").projectDir = file("third_party/COUI17/$name")
}
```

模块的构建文件引用版本目录里的条目，把 [`gradle/libs.versions.toml`](gradle/libs.versions.toml) 里 `jetbrains-compose-*`、`androidx-navigationevent`、`materialkolor-utilities`、`androidx-core-ktx`、`androidx-compose-*` 以及对应插件并入你的版本目录即可。

**作为本地 Maven 制品**：

```bash
./gradlew publishToMavenLocal
```

```kotlin
repositories { mavenLocal() }
dependencies { implementation("io.github.tianli0.coui17:coui-patterns:0.1.0") }
```

`coui-patterns` 以 `api` 方式带出 `coui-ui`、`coui-preference` 和 `coui-icons`，多数应用只需要依赖它一个。

## 一个页面

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            COUI17Theme(accent = Accent.Blue) {
                var wifi by remember { mutableStateOf(true) }
                Page(title = "设置", immersive = true) {
                    LazyColumn(
                        state = rememberPageListState(),
                        modifier = Modifier.pageScroll(),
                        contentPadding = pageListPadding(),
                    ) {
                        item { SectionHeader("网络") }
                        item {
                            GroupCard {
                                SwitchPreference(
                                    checked = wifi,
                                    onCheckedChange = { wifi = it },
                                    title = "WLAN",
                                    cardListPosition = cardListPositionOf(0, 2),
                                )
                                ArrowPreference(
                                    title = "移动网络",
                                    cardListPosition = cardListPositionOf(1, 2),
                                    onClick = { },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
```

这一段就得到了：页面底色、可折叠的大标题、列表滚到标题栏下方时的渐进毛玻璃、COUI 的过度滚动与触底震动、点状态栏回到顶部、圆角分组卡片和按压反馈。

## 演示应用

`gallery` 模块把每一类组件都放在真实的 ColorOS 17 页面里，支持浅色 / 深色和六种强调色。

```bash
./gradlew :gallery:installDebug
```

## 构建与检查

```bash
./gradlew :coui-patterns:testDebugUnitTest :coui-ui:testAndroidHostTest   # 单元测试
./gradlew publishToMavenLocal                                              # 所有库模块
./gradlew :gallery:assembleDebug                                           # 演示应用
```

需要 Android SDK 平台 `android-37.2` 和 build-tools `37.0.0`，在 `local.properties` 里写 `sdk.dir=…`（或设置 `ANDROID_HOME`）。

## 玻璃效果的条件

毛玻璃用 `RuntimeShader` 实现，需要 Android 13 及以上，并且系统没有关闭模糊。低于这个版本、或用户关闭了模糊时，所有玻璃表面自动退回不透明的 COUI 填充色，布局不变。默认情况下库不会反射 `com.oplus.*` 私有类（`COUI17Theme(platformMaterials = false)`）。

## 许可

Apache-2.0，见 [LICENSE](LICENSE) 与 [NOTICE](NOTICE)。`coui-ui` 中 `io.github.suqi8.coui.kmp.kit` 包下的文件来自 COUI-Kit，沿用 MPL-2.0（文件头保留，源码即本仓库）。第三方依赖见 [THIRD-PARTY-LICENSES.md](THIRD-PARTY-LICENSES.md)。

本项目与 OPPO 没有关联。“ColorOS”是其所有者的商标，这里只用于描述设计风格。
