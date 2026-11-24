package io.github.tianli0.coui17.gallery

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import io.github.suqi8.coui.kmp.basic.BottomFloatingNavigationBar
import io.github.suqi8.coui.kmp.basic.BottomFloatingNavigationBarItem
import io.github.suqi8.coui.kmp.basic.Icon
import io.github.suqi8.coui.kmp.blur.LocalBackdrop
import io.github.suqi8.coui.kmp.blur.LocalBlurEnabled
import io.github.suqi8.coui.kmp.blur.isRuntimeShaderSupported
import io.github.suqi8.coui.kmp.blur.layerBackdrop
import io.github.suqi8.coui.kmp.blur.rememberLayerBackdrop
import io.github.suqi8.coui.kmp.icon.COUIIcons
import io.github.suqi8.coui.kmp.icon.extended.GridView
import io.github.suqi8.coui.kmp.icon.extended.Layers
import io.github.suqi8.coui.kmp.icon.extended.Theme
import io.github.suqi8.coui.kmp.squircle.SquircleShape
import io.github.tianli0.coui17.patterns.Accent
import io.github.tianli0.coui17.patterns.COUI17Theme
import io.github.tianli0.coui17.patterns.LocalBottomBarSpace
import io.github.tianli0.coui17.patterns.ThemeMode
import io.github.tianli0.coui17.patterns.pageContainerColor
import io.github.tianli0.coui17.patterns.pageListPadding
import io.github.tianli0.coui17.patterns.pageScroll
import io.github.tianli0.coui17.patterns.rememberPageListState

/** The gallery's two languages: 简体中文 when the app locale is Chinese, English otherwise. */
@Composable
@ReadOnlyComposable
fun tr(zh: String, en: String): String = if (LocalConfiguration.current.locales[0].language == "zh") zh else en

enum class Tab { Components, Overlays, Theme }

/** A detail page opened from the Components tab. */
enum class Detail { Buttons, Selection, Input, Tags, Feedback, Lists, Glass }

@Composable
fun GalleryApp() {
    var modeIndex by rememberSaveable { mutableStateOf(ThemeMode.System.ordinal) }
    var accentIndex by rememberSaveable { mutableStateOf(0) }
    var tab by rememberSaveable { mutableStateOf(Tab.Components) }
    var detail by rememberSaveable { mutableStateOf<Detail?>(null) }

    COUI17Theme(mode = ThemeMode.entries[modeIndex], accent = Accent.Presets[accentIndex]) {
        BackHandler(enabled = detail != null) { detail = null }
        val showBar = detail == null
        val pageColor = pageContainerColor()
        // The floating capsule blurs the page: the page is recorded as a layer and the bar is its
        // SIBLING (never inside the recorded layer), so lists scroll underneath real glass.
        val backdrop = if (isRuntimeShaderSupported()) {
            rememberLayerBackdrop {
                drawRect(pageColor)
                drawContent()
            }
        } else {
            null
        }
        Box(Modifier.fillMaxSize().background(pageColor)) {
            Box(Modifier.fillMaxSize().then(if (backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier)) {
                CompositionLocalProvider(
                    LocalBlurEnabled provides true,
                    LocalBottomBarSpace provides if (showBar) 96.dp else 0.dp,
                ) {
                    AnimatedContent(
                        targetState = detail,
                        transitionSpec = {
                            val push = targetState != null
                            val offset = spring<IntOffset>(dampingRatio = 1f, stiffness = 380f)
                            (slideInHorizontally(offset) { if (push) it / 4 else -it / 4 } + fadeIn()) togetherWith
                                (slideOutHorizontally(offset) { if (push) -it / 4 else it / 4 } + fadeOut())
                        },
                        label = "page",
                    ) { open ->
                        when (open) {
                            null -> when (tab) {
                                Tab.Components -> ComponentsHome(onOpen = { detail = it })
                                Tab.Overlays -> OverlaysPage()
                                Tab.Theme -> ThemePage(
                                    modeIndex = modeIndex,
                                    onMode = { modeIndex = it },
                                    accentIndex = accentIndex,
                                    onAccent = { accentIndex = it },
                                )
                            }
                            Detail.Buttons -> ButtonsPage { detail = null }
                            Detail.Selection -> SelectionPage { detail = null }
                            Detail.Input -> InputPage { detail = null }
                            Detail.Tags -> TagsPage { detail = null }
                            Detail.Feedback -> FeedbackPage { detail = null }
                            Detail.Lists -> ListsPage { detail = null }
                            Detail.Glass -> GlassPage { detail = null }
                        }
                    }
                }
            }
            AnimatedVisibility(
                visible = showBar,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter),
            ) {
                Box(Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.navigationBars).padding(bottom = 12.dp)) {
                    CompositionLocalProvider(LocalBackdrop provides backdrop, LocalBlurEnabled provides true) {
                        BottomFloatingNavigationBar {
                            BottomFloatingNavigationBarItem(
                                selected = tab == Tab.Components,
                                onClick = { tab = Tab.Components },
                                icon = COUIIcons.GridView,
                                label = tr("组件", "Components"),
                            )
                            BottomFloatingNavigationBarItem(
                                selected = tab == Tab.Overlays,
                                onClick = { tab = Tab.Overlays },
                                icon = COUIIcons.Layers,
                                label = tr("弹层", "Overlays"),
                            )
                            BottomFloatingNavigationBarItem(
                                selected = tab == Tab.Theme,
                                onClick = { tab = Tab.Theme },
                                icon = COUIIcons.Theme,
                                label = tr("主题", "Theme"),
                            )
                        }
                    }
                }
            }
        }
    }
}

/** The main list of a page: COUI overscroll, the collapsing title, scroll-to-top, and the page's paddings. */
@Composable
fun PageList(content: LazyListScope.() -> Unit) {
    LazyColumn(
        state = rememberPageListState(),
        modifier = Modifier.fillMaxWidth().pageScroll(),
        contentPadding = pageListPadding(),
        content = content,
    )
}

/** The leading glyph of a settings row: a white icon on a coloured ColorOS squircle. */
@Composable
fun IconTile(icon: ImageVector, color: Color) {
    Box(Modifier.size(32.dp).background(color, SquircleShape(9.dp)), contentAlignment = Alignment.Center) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
    }
}

/** Padding of free-form demo content inside a group card (rows bring their own). */
val CardContentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
