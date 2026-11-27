package io.github.tianli0.coui17.gallery

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.suqi8.coui.kmp.basic.BasicComponent
import io.github.suqi8.coui.kmp.basic.BottomFloatingAccessoryBall
import io.github.suqi8.coui.kmp.basic.BottomFloatingAccessoryNavigationBar
import io.github.suqi8.coui.kmp.basic.BottomFloatingNavigationBarItem
import io.github.suqi8.coui.kmp.basic.BottomFloatingScrollToolbar
import io.github.suqi8.coui.kmp.basic.BottomFloatingScrollToolbarItem
import io.github.suqi8.coui.kmp.basic.BottomFloatingToolbar
import io.github.suqi8.coui.kmp.basic.BottomFloatingToolbarGroup
import io.github.suqi8.coui.kmp.basic.BottomFloatingToolbarItem
import io.github.suqi8.coui.kmp.basic.FloatingNavigationBar
import io.github.suqi8.coui.kmp.basic.FloatingNavigationBarItem
import io.github.suqi8.coui.kmp.basic.NavigationBar
import io.github.suqi8.coui.kmp.basic.NavigationBarItem
import io.github.suqi8.coui.kmp.basic.NavigationRail
import io.github.suqi8.coui.kmp.basic.NavigationRailItem
import io.github.suqi8.coui.kmp.basic.SideNavigationBar
import io.github.suqi8.coui.kmp.basic.Text
import io.github.suqi8.coui.kmp.basic.TextButton
import io.github.suqi8.coui.kmp.blur.LocalBackdrop
import io.github.suqi8.coui.kmp.blur.isRuntimeShaderSupported
import io.github.suqi8.coui.kmp.blur.layerBackdrop
import io.github.suqi8.coui.kmp.blur.rememberLayerBackdrop
import io.github.suqi8.coui.kmp.icon.COUIIcons
import io.github.suqi8.coui.kmp.icon.extended.Add
import io.github.suqi8.coui.kmp.icon.extended.Edit
import io.github.suqi8.coui.kmp.icon.extended.Favorites
import io.github.suqi8.coui.kmp.icon.extended.GridView
import io.github.suqi8.coui.kmp.icon.extended.ListView
import io.github.suqi8.coui.kmp.icon.extended.Refresh
import io.github.suqi8.coui.kmp.icon.extended.Share
import io.github.suqi8.coui.kmp.icon.extended.Tune
import io.github.suqi8.coui.kmp.basic.rememberSideNavigationBarState
import io.github.suqi8.coui.kmp.theme.COUITheme
import io.github.tianli0.coui17.patterns.GroupCard
import io.github.tianli0.coui17.patterns.KeyValueRow
import io.github.tianli0.coui17.patterns.Page
import io.github.tianli0.coui17.patterns.SectionHeader

@Composable
internal fun NavigationDemoPage(onBack: () -> Unit) {
    val labels = listOf(tr("浏览", "Browse"), tr("收藏", "Saved"), tr("列表", "List"))
    val icons = listOf(COUIIcons.GridView, COUIIcons.Favorites, COUIIcons.ListView)
    val toolbarLabels = listOf(
        tr("增强", "Enhance"),
        tr("调整", "Adjust"),
        tr("裁剪", "Crop"),
        tr("旋转", "Rotate"),
        tr("标记", "Markup"),
        tr("滤镜", "Filters"),
        tr("收藏", "Save"),
        tr("分享", "Share"),
    )
    val toolbarIcons = listOf(COUIIcons.Add, COUIIcons.Tune, COUIIcons.GridView, COUIIcons.Refresh, COUIIcons.Edit, COUIIcons.ListView, COUIIcons.Favorites, COUIIcons.Share)
    var selected by rememberSaveable { mutableIntStateOf(0) }
    var action by rememberSaveable { mutableIntStateOf(0) }
    var accessoryVisible by rememberSaveable { mutableStateOf(true) }
    var barVisible by rememberSaveable { mutableStateOf(true) }

    Page(title = tr("导航与工具栏", "Navigation and toolbars"), onBack = onBack, immersive = true) {
        PageList {
            item { SectionHeader(tr("固定底栏", "Fixed bottom bar")) }
            item {
                GroupCard {
                    KeyValueRow(tr("当前位置", "Current destination"), labels[selected], Modifier.padding(horizontal = 16.dp))
                    BasicComponent(
                        title = if (barVisible) tr("收起底栏", "Hide bottom bar") else tr("显示底栏", "Show bottom bar"),
                        onClick = { barVisible = !barVisible },
                    )
                    Box(Modifier.fillMaxWidth().height(76.dp), contentAlignment = Alignment.TopCenter) {
                        NavigationBar(backdrop = null, visible = barVisible) {
                            labels.forEachIndexed { index, label ->
                                NavigationBarItem(selected == index, { selected = index }, icons[index], label)
                            }
                        }
                    }
                }
            }
            item { SectionHeader(tr("悬浮底栏", "Floating bottom bar")) }
            item {
                GroupCard {
                    FloatingNavigationBar(defaultWindowInsetsPadding = false) {
                        labels.forEachIndexed { index, label ->
                            FloatingNavigationBarItem(selected == index, { selected = index }, icons[index], label)
                        }
                    }
                }
            }
            item { SectionHeader(tr("悬浮导航与附加按钮", "Floating navigation with accessory")) }
            item {
                GroupCard {
                    val attachLabel = tr("显示附加按钮", "Show accessory")
                    val removeLabel = tr("收起附加按钮", "Hide accessory")
                    BasicComponent(title = attachLabel, onClick = { accessoryVisible = true })
                    val surface = COUITheme.colorScheme.surface
                    val accent = COUITheme.colorScheme.primary
                    val backdrop = if (isRuntimeShaderSupported()) rememberLayerBackdrop() else null
                    Box(contentAlignment = Alignment.Center) {
                        Row(
                            Modifier.fillMaxWidth().then(if (backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier)
                                .background(surface).height(104.dp),
                        ) {
                            listOf(0.09f, 0.20f, 0.34f, 0.14f, 0.25f).forEach { alpha ->
                                Box(Modifier.weight(1f).height(104.dp).background(accent.copy(alpha = alpha)))
                            }
                        }
                        CompositionLocalProvider(LocalBackdrop provides backdrop) {
                            BottomFloatingAccessoryNavigationBar(
                                endAccessoryVisible = accessoryVisible,
                                endAccessory = {
                                    BottomFloatingAccessoryBall(
                                        onClick = { accessoryVisible = false },
                                        icon = COUIIcons.Add,
                                        contentDescription = removeLabel,
                                    )
                                },
                            ) {
                                labels.forEachIndexed { index, label ->
                                    BottomFloatingNavigationBarItem(selected == index, { selected = index }, icons[index], label)
                                }
                            }
                        }
                    }
                }
            }
            item { SectionHeader(tr("导航轨与抽屉", "Navigation rail and drawer")) }
            item {
                GroupCard {
                    DrawerDemo(labels, selected, onSelected = { selected = it }, rail = {
                        NavigationRail(defaultWindowInsetsPadding = false) {
                            labels.forEachIndexed { index, label ->
                                NavigationRailItem(selected == index, { selected = index }, icons[index], label)
                            }
                            NavigationRailItem(
                                selected = false,
                                onClick = {},
                                icon = COUIIcons.ListView,
                                label = tr("归档不可用", "Archive unavailable"),
                                enabled = false,
                            )
                        }
                    })
                }
            }
            item { SectionHeader(tr("悬浮工具栏", "Floating toolbar")) }
            item {
                GroupCard {
                    Column(Modifier.padding(vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        KeyValueRow(tr("当前工具", "Active tool"), toolbarLabels[action], Modifier.padding(horizontal = 16.dp))
                        BottomFloatingToolbar {
                            BottomFloatingToolbarGroup {
                                Row(horizontalArrangement = Arrangement.Center) {
                                    BottomFloatingToolbarItem(toolbarLabels[0], { action = 0 }, toolbarIcons[0])
                                    BottomFloatingToolbarItem(toolbarLabels[1], { action = 1 }, toolbarIcons[1])
                                    BottomFloatingToolbarItem(toolbarLabels[2], { action = 2 }, toolbarIcons[2])
                                }
                            }
                        }
                        BottomFloatingScrollToolbar {
                            toolbarLabels.forEachIndexed { index, label ->
                                BottomFloatingScrollToolbarItem(
                                    onClick = { action = index },
                                    icon = toolbarIcons[index],
                                    label = label,
                                    selected = action == index,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DrawerDemo(labels: List<String>, selected: Int, onSelected: (Int) -> Unit, rail: @Composable () -> Unit) {
    val drawer = rememberSideNavigationBarState()
    BackHandler(enabled = drawer.isOpen) { drawer.close() }
    SideNavigationBar(
        modifier = Modifier.fillMaxWidth().height(276.dp),
        state = drawer,
        parentChildHierarchy = true,
        drawerContent = {
            Column(Modifier.widthIn(max = 220.dp).fillMaxSize().background(COUITheme.colorScheme.surface)) {
                labels.forEachIndexed { index, label ->
                    BasicComponent(title = label, onClick = {
                        onSelected(index)
                        drawer.close()
                    })
                }
            }
        },
    ) {
        Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            rail()
            Column(
                Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(labels[selected], style = COUITheme.textStyles.title3)
                TextButton(text = tr("打开抽屉", "Open drawer"), onClick = drawer::open)
            }
        }
    }
}
