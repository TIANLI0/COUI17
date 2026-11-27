package io.github.tianli0.coui17.gallery

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.github.suqi8.coui.kmp.basic.Badge
import io.github.suqi8.coui.kmp.basic.BasicComponent
import io.github.suqi8.coui.kmp.basic.ButtonDefaults
import io.github.suqi8.coui.kmp.basic.Checkbox
import io.github.suqi8.coui.kmp.basic.Chip
import io.github.suqi8.coui.kmp.basic.ChipTabData
import io.github.suqi8.coui.kmp.basic.ChipTabGroup
import io.github.suqi8.coui.kmp.basic.CircularProgressIndicator
import io.github.suqi8.coui.kmp.basic.ExpandArrow
import io.github.suqi8.coui.kmp.basic.HighlightedIconButton
import io.github.suqi8.coui.kmp.basic.Icon
import io.github.suqi8.coui.kmp.basic.InfiniteProgressIndicator
import io.github.suqi8.coui.kmp.basic.IntentSeekBar
import io.github.suqi8.coui.kmp.basic.NumberPicker
import io.github.suqi8.coui.kmp.basic.LinearProgressIndicator
import io.github.suqi8.coui.kmp.basic.RadioButton
import io.github.suqi8.coui.kmp.basic.RangeSlider
import io.github.suqi8.coui.kmp.basic.Slider
import io.github.suqi8.coui.kmp.basic.Stepper
import io.github.suqi8.coui.kmp.basic.Switch
import io.github.suqi8.coui.kmp.basic.TabRow
import io.github.suqi8.coui.kmp.basic.Text
import io.github.suqi8.coui.kmp.basic.TextButton
import io.github.suqi8.coui.kmp.basic.VerticalSlider
import io.github.suqi8.coui.kmp.basic.cardListPositionOf
import io.github.suqi8.coui.kmp.blur.isRuntimeShaderSupported
import io.github.suqi8.coui.kmp.blur.layerBackdrop
import io.github.suqi8.coui.kmp.blur.rememberLayerBackdrop
import io.github.suqi8.coui.kmp.icon.COUIIcons
import io.github.suqi8.coui.kmp.icon.extended.Add
import io.github.suqi8.coui.kmp.icon.extended.Edit
import io.github.suqi8.coui.kmp.icon.extended.Favorites
import io.github.suqi8.coui.kmp.icon.extended.Info
import io.github.suqi8.coui.kmp.icon.extended.Layers
import io.github.suqi8.coui.kmp.icon.extended.ListView
import io.github.suqi8.coui.kmp.icon.extended.Messages
import io.github.suqi8.coui.kmp.icon.extended.Mic
import io.github.suqi8.coui.kmp.icon.extended.Refresh
import io.github.suqi8.coui.kmp.icon.extended.Send
import io.github.suqi8.coui.kmp.icon.extended.Share
import io.github.suqi8.coui.kmp.icon.extended.Tune
import io.github.suqi8.coui.kmp.preference.ArrowPreference
import io.github.suqi8.coui.kmp.preference.CheckboxPreference
import io.github.suqi8.coui.kmp.preference.RadioButtonPreference
import io.github.suqi8.coui.kmp.preference.SwitchPreference
import io.github.suqi8.coui.kmp.theme.COUITheme
import io.github.tianli0.coui17.patterns.ActionTone
import io.github.tianli0.coui17.patterns.ComposerKey
import io.github.tianli0.coui17.patterns.ComposerKeyGap
import io.github.tianli0.coui17.patterns.InputPill
import io.github.tianli0.coui17.patterns.InputPillState
import io.github.tianli0.coui17.patterns.EmptyMessage
import io.github.tianli0.coui17.patterns.ErrorState
import io.github.tianli0.coui17.patterns.ExpandBody
import io.github.tianli0.coui17.patterns.GroupCard
import io.github.tianli0.coui17.patterns.KeyValueRow
import io.github.tianli0.coui17.patterns.LinkButton
import io.github.tianli0.coui17.patterns.LocalGlassBackdrop
import io.github.tianli0.coui17.patterns.NoticeBanner
import io.github.tianli0.coui17.patterns.Page
import io.github.tianli0.coui17.patterns.PageDefaults
import io.github.tianli0.coui17.patterns.SectionHeader
import io.github.tianli0.coui17.patterns.SmallButton
import io.github.tianli0.coui17.patterns.StatusPill
import io.github.tianli0.coui17.patterns.TextInput
import io.github.tianli0.coui17.patterns.ToolbarIcon
import io.github.tianli0.coui17.patterns.glassSurface
import io.github.tianli0.coui17.patterns.pageContainerColor
import io.github.tianli0.coui17.patterns.pageListPadding

private val TileBlue = Color(0xFF2E7CF6)
private val TileGreen = Color(0xFF1FB870)
private val TileOrange = Color(0xFFFF8A1F)
private val TilePurple = Color(0xFF8266F5)
private val TileRed = Color(0xFFF0554A)
private val TileCyan = Color(0xFF14B4CE)
private val TileGrey = Color(0xFF7C8594)

/** Loose text on a page: summary colour at the text gutter, in line with the row titles. */
@Composable
fun Note(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = COUITheme.textStyles.body2,
        color = COUITheme.colorScheme.onSurfaceVariantSummary,
        modifier = modifier.padding(horizontal = PageDefaults.TextGutter).padding(bottom = 16.dp),
    )
}

/** A caption above one demo inside a card. */
@Composable
private fun Caption(text: String) {
    Text(
        text,
        style = COUITheme.textStyles.footnote1,
        color = COUITheme.colorScheme.onSurfaceVariantSummary,
        modifier = Modifier.padding(bottom = 10.dp),
    )
}

@Composable
fun ComponentsHome(onOpen: (Detail) -> Unit) {
    Page(title = "COUI17", subtitle = tr("ColorOS 17 风格 · Jetpack Compose", "ColorOS 17 style · Jetpack Compose"), immersive = true) {
        PageList {
            item {
                GroupCard {
                    Column(Modifier.padding(CardContentPadding)) {
                        Text(
                            tr("为 Android 打造的 ColorOS 17 组件库", "ColorOS 17 components for Android"),
                            style = COUITheme.textStyles.title4,
                            fontWeight = FontWeight.SemiBold,
                            color = COUITheme.colorScheme.onSurface,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            tr(
                                "超椭圆圆角、毛玻璃材质、按压光效与弹性动效，全部用 Compose 绘制。这个应用里的每一页都由库里的组件搭成。",
                                "Squircle corners, frosted glass, press light and spring motion, all drawn in Compose. Every page of this app is built from the library.",
                            ),
                            style = COUITheme.textStyles.body2,
                            color = COUITheme.colorScheme.onSurfaceVariantSummary,
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatusPill(tr("8 个模块", "8 modules"), COUITheme.colorScheme.primary)
                            StatusPill(tr("130+ 组件", "130+ components"), TileGreen)
                            StatusPill("Apache-2.0", TileOrange)
                        }
                    }
                }
            }
            item { SectionHeader(tr("基础组件", "Components")) }
            item {
                val rows = listOf(
                    Entry(Detail.Buttons, tr("按钮", "Buttons"), tr("四个层级：大、面板确认、小、链接", "Four tiers: large, panel confirm, small, link"), COUIIcons.Add, TileBlue),
                    Entry(Detail.Selection, tr("选择控件", "Selection"), tr("开关、复选、单选、滑块、步进器", "Switch, checkbox, radio, slider, stepper"), COUIIcons.Tune, TileGreen),
                    Entry(Detail.Input, tr("输入", "Input"), tr("文本框与中文输入法", "Text fields and IME composition"), COUIIcons.Edit, TileOrange),
                    Entry(Detail.Tags, tr("标签与分段", "Tags and tabs"), tr("胶囊、标签、徽标、分段切换", "Chips, tags, badges, tab rows"), COUIIcons.Favorites, TilePurple),
                    Entry(Detail.Pickers, tr("日期与时间", "Date and time"), tr("日历、公历、农历、时间和数字滚轮", "Calendar, date, lunar date, time and number wheels"), COUIIcons.ListView, TileCyan),
                    Entry(Detail.Colors, tr("颜色选择", "Color pickers"), tr("色板、色样与四色彩空间", "Swatches, palette and four color spaces"), COUIIcons.Tune, TileOrange),
                    Entry(Detail.Security, tr("安全输入", "Security inputs"), tr("验证码、数字键盘、图案与密码", "Code field, keypad, pattern and password"), COUIIcons.Edit, TileRed),
                )
                EntryGroup(rows, onOpen)
            }
            item { SectionHeader(tr("页面", "Pages")) }
            item {
                val rows = listOf(
                    Entry(Detail.Lists, tr("列表与偏好项", "Lists and preferences"), tr("分组卡片里的行", "Rows inside group cards"), COUIIcons.ListView, TileCyan),
                    Entry(Detail.Preferences, tr("偏好行变体", "Preference variants"), tr("行内编辑、弹出选项和独立控件", "Inline editing, option menus and independent controls"), COUIIcons.Tune, TileGreen),
                    Entry(Detail.Feedback, tr("进度与状态", "Progress and states"), tr("加载、空、错误、提示条", "Loading, empty, error, notices"), COUIIcons.Info, TileRed),
                    Entry(Detail.Glass, tr("悬浮玻璃", "Floating glass"), tr("浮在内容之上的毛玻璃控件", "Frosted controls over scrolling content"), COUIIcons.Layers, TileGrey),
                    Entry(Detail.Search, tr("搜索", "Search"), tr("搜索历史、建议、加载与结果", "History, suggestions, loading and results"), COUIIcons.ListView, TileBlue),
                    Entry(Detail.Navigation, tr("导航与工具栏", "Navigation and toolbars"), tr("底栏、侧栏、悬浮导航与附加操作", "Bottom navigation, rail, drawer and floating controls"), COUIIcons.Layers, TilePurple),
                )
                EntryGroup(rows, onOpen)
            }
            item { Note(tr("弹窗、面板和菜单在“弹层”标签页；外观与强调色在“主题”标签页。", "Dialogs, panels and menus are under Overlays; appearance and accent under Theme.")) }
        }
    }
}

private class Entry(val detail: Detail, val title: String, val summary: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val color: Color)

@Composable
private fun EntryGroup(rows: List<Entry>, onOpen: (Detail) -> Unit) {
    GroupCard {
        rows.forEachIndexed { index, row ->
            ArrowPreference(
                title = row.title,
                summary = row.summary,
                startAction = { IconTile(row.icon, row.color) },
                cardListPosition = cardListPositionOf(index, rows.size),
                onClick = { onOpen(row.detail) },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ButtonsPage(onBack: () -> Unit) {
    var count by rememberSaveable { mutableIntStateOf(0) }
    Page(title = tr("按钮", "Buttons"), onBack = onBack, immersive = true) {
        PageList {
            item { SectionHeader(tr("大按钮", "Large")) }
            item {
                GroupCard {
                    Column(Modifier.padding(CardContentPadding)) {
                        Caption(tr("44dp 胶囊。只用在面板底部或引导步骤的末尾，一屏一个实心按钮。", "A 44dp capsule, for the end of a panel or an onboarding step. One filled button per screen."))
                        TextButton(
                            text = tr("继续", "Continue"),
                            onClick = { count++ },
                            colors = ButtonDefaults.textButtonColorsPrimary(),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            TextButton(text = tr("取消", "Cancel"), onClick = { count = 0 }, modifier = Modifier.weight(1f))
                            TextButton(text = tr("不可用", "Disabled"), onClick = {}, enabled = false, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
            item { SectionHeader(tr("小按钮", "Small")) }
            item {
                GroupCard {
                    Column(Modifier.padding(CardContentPadding)) {
                        Caption(tr("28dp 胶囊。行、卡片、工具栏里的操作，靠末端对齐，间距 8dp。", "A 28dp capsule: an action of a row, a card or a toolbar. End-aligned, 8dp apart."))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            SmallButton(tr("默认", "Default"), { count++ })
                            SmallButton(tr("主要", "Primary"), { count++ }, tone = ActionTone.Primary)
                            SmallButton(tr("危险", "Danger"), { count++ }, tone = ActionTone.Danger)
                            SmallButton(tr("不可用", "Disabled"), {}, enabled = false)
                            SmallButton(tr("主要", "Primary"), {}, tone = ActionTone.Primary, loading = true)
                        }
                    }
                    BasicComponent(
                        title = tr("蓝牙耳机", "Bluetooth headset"),
                        summary = tr("已连接", "Connected"),
                        endActions = { SmallButton(tr("断开", "Disconnect"), { count++ }, tone = ActionTone.Danger) },
                    )
                }
            }
            item { SectionHeader(tr("链接按钮", "Link")) }
            item {
                GroupCard {
                    Column(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        LinkButton(tr("加载更多", "Load more"), { count++ })
                    }
                }
            }
            item { SectionHeader(tr("工具栏圆键", "Toolbar keys")) }
            item {
                GroupCard {
                    Row(Modifier.padding(CardContentPadding), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        HighlightedIconButton(onClick = { count++ }) { Icon(COUIIcons.Refresh, contentDescription = tr("刷新", "Refresh")) }
                        HighlightedIconButton(onClick = { count++ }) { Icon(COUIIcons.Share, contentDescription = tr("分享", "Share")) }
                        HighlightedIconButton(onClick = { count++ }) { Icon(COUIIcons.Edit, contentDescription = tr("编辑", "Edit")) }
                        Spacer(Modifier.weight(1f))
                        Text(
                            tr("已点按 $count 次", "Tapped $count times"),
                            style = COUITheme.textStyles.body2,
                            color = COUITheme.colorScheme.onSurfaceVariantSummary,
                        )
                    }
                }
            }
            item { Note(tr("所有可点按的元素都带按压缩放与光效，反馈被裁剪在自身形状之内。", "Every pressable has touch motion and press light, clipped to its own shape.")) }
        }
    }
}

@Composable
fun SelectionPage(onBack: () -> Unit) {
    var wifi by rememberSaveable { mutableStateOf(true) }
    var loading by rememberSaveable { mutableStateOf(false) }
    var check by rememberSaveable { mutableStateOf(true) }
    var radio by rememberSaveable { mutableIntStateOf(0) }
    var brightness by rememberSaveable { mutableFloatStateOf(0.6f) }
    var size by rememberSaveable { mutableFloatStateOf(2f) }
    var copies by rememberSaveable { mutableIntStateOf(1) }
    var playback by rememberSaveable { mutableFloatStateOf(25f) }
    var picker by rememberSaveable { mutableIntStateOf(4) }
    var range by remember { mutableStateOf(2f..8f) }
    var vertical by rememberSaveable { mutableFloatStateOf(5f) }
    Page(title = tr("选择控件", "Selection"), onBack = onBack, immersive = true) {
        PageList {
            item { SectionHeader(tr("开关", "Switch")) }
            item {
                GroupCard {
                    SwitchPreference(
                        checked = wifi,
                        onCheckedChange = { wifi = it },
                        title = "WLAN",
                        summary = if (wifi) tr("已开启", "On") else tr("已关闭", "Off"),
                        cardListPosition = cardListPositionOf(0, 2),
                    )
                    BasicComponent(
                        title = tr("加载中的开关", "Loading switch"),
                        summary = tr("等待结果时显示转圈", "Spins while a result is pending"),
                        endActions = { Switch(checked = loading, onCheckedChange = { loading = it }, isLoading = loading) },
                        cardListPosition = cardListPositionOf(1, 2),
                    )
                }
            }
            item { SectionHeader(tr("单选与复选", "Radio and checkbox")) }
            item {
                GroupCard {
                    val options = listOf(tr("跟随系统", "System"), tr("浅色", "Light"), tr("深色", "Dark"))
                    options.forEachIndexed { index, label ->
                        RadioButtonPreference(
                            title = label,
                            selected = radio == index,
                            onClick = { radio = index },
                            cardListPosition = cardListPositionOf(index, options.size + 1),
                        )
                    }
                    CheckboxPreference(
                        title = tr("记住我的选择", "Remember my choice"),
                        checked = check,
                        onCheckedChange = { check = it },
                        cardListPosition = cardListPositionOf(options.size, options.size + 1),
                    )
                }
            }
            item {
                GroupCard {
                    Row(Modifier.padding(CardContentPadding), horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(state = if (check) ToggleableState.On else ToggleableState.Off, onClick = { check = !check })
                        Checkbox(state = ToggleableState.Indeterminate, onClick = null)
                        Checkbox(state = ToggleableState.On, onClick = null, enabled = false)
                        RadioButton(selected = radio == 0, onClick = { radio = 0 })
                        RadioButton(selected = radio != 0, onClick = { radio = 1 })
                        RadioButton(selected = true, onClick = null, enabled = false)
                    }
                }
            }
            item { SectionHeader(tr("滑块", "Slider")) }
            item {
                GroupCard {
                    Column(Modifier.padding(CardContentPadding)) {
                        Caption(tr("亮度 ${(brightness * 100).toInt()}%", "Brightness ${(brightness * 100).toInt()}%"))
                        Slider(value = brightness, onValueChange = { brightness = it })
                        Spacer(Modifier.height(20.dp))
                        Caption(tr("字号：第 ${size.toInt() + 1} 档（带吸附刻度）", "Font size: step ${size.toInt() + 1} (with key points)"))
                        Slider(value = size, onValueChange = { size = it }, valueRange = 0f..4f, steps = 3, showKeyPoints = true)
                    }
                }
            }
            item { SectionHeader(tr("播放进度", "Playback")) }
            item {
                GroupCard {
                    Column(Modifier.padding(CardContentPadding)) {
                        Caption(tr("已播放 ${playback.toInt()}%，已缓冲 75%", "Played ${playback.toInt()}%, buffered 75%"))
                        IntentSeekBar(
                            value = playback,
                            onValueChange = { playback = it },
                            valueRange = 0f..100f,
                            secondaryProgress = 75f,
                            steps = 3,
                            showThumb = true,
                        )
                        Spacer(Modifier.height(16.dp))
                        Caption(tr("从右到左", "Right to left"))
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                            IntentSeekBar(
                                value = playback,
                                onValueChange = { playback = it },
                                valueRange = 0f..100f,
                                secondaryProgress = 75f,
                                steps = 3,
                                showThumb = true,
                            )
                        }
                    }
                }
            }
            item {
                GroupCard {
                    Column(Modifier.padding(CardContentPadding), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Caption(tr("范围 ${range.start.toInt()}–${range.endInclusive.toInt()}", "Range ${range.start.toInt()}–${range.endInclusive.toInt()}"))
                        RangeSlider(value = range, onValueChange = { range = it }, valueRange = 0f..10f, steps = 9)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            VerticalSlider(value = vertical, onValueChange = { vertical = it }, valueRange = 0f..10f, steps = 9, modifier = Modifier.height(120.dp))
                            Caption(tr("垂直 ${vertical.toInt()}", "Vertical ${vertical.toInt()}"))
                        }
                    }
                }
            }
            item { SectionHeader(tr("数字选择器", "Number picker")) }
            item {
                GroupCard {
                    Box(Modifier.fillMaxWidth().padding(CardContentPadding), contentAlignment = Alignment.Center) {
                        NumberPicker(value = picker, onValueChange = { picker = it })
                    }
                }
            }
            item { SectionHeader(tr("步进器", "Stepper")) }
            item {
                GroupCard {
                    BasicComponent(
                        title = tr("打印份数", "Copies"),
                        summary = tr("1 到 9", "1 to 9"),
                        endActions = { Stepper(value = copies, onValueChange = { copies = it }, minValue = 1, maxValue = 9) },
                    )
                }
            }
        }
    }
}

@Composable
fun InputPage(onBack: () -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable { mutableStateOf("") }
    Page(title = tr("输入", "Input"), onBack = onBack) {
        PageList {
            item { SectionHeader(tr("单行", "Single line")) }
            item {
                TextInput(
                    value = name,
                    onValueChange = { name = it },
                    label = tr("名称", "Name"),
                    showClearButton = true,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = PageDefaults.CardGutter),
                )
            }
            item { SectionHeader(tr("多行", "Multi-line")) }
            item {
                TextInput(
                    value = note,
                    onValueChange = { note = it },
                    label = tr("备注", "Note"),
                    singleLine = false,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = PageDefaults.CardGutter),
                )
            }
            item { Spacer(Modifier.height(16.dp)) }
            item {
                GroupCard {
                    KeyValueRow(tr("名称", "Name"), name.ifEmpty { "—" }, cardListPosition = cardListPositionOf(0, 2))
                    KeyValueRow(tr("备注字数", "Note length"), note.length.toString(), cardListPosition = cardListPositionOf(1, 2))
                }
            }
            item {
                Note(
                    tr(
                        "TextInput 的所有者只保存字符串，但选区和输入法组合态不会丢：用拼音输入中文时候选词不会被打断。",
                        "The owner of a TextInput stores a plain String, yet selection and IME composition survive: composing CJK text is never interrupted.",
                    ),
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagsPage(onBack: () -> Unit) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var chipTab by rememberSaveable { mutableIntStateOf(0) }
    var selected by rememberSaveable { mutableStateOf(setOf(0)) }
    Page(title = tr("标签与分段", "Tags and tabs"), onBack = onBack) {
        PageList {
            item { SectionHeader(tr("分段切换", "Tab row")) }
            item {
                TabRow(
                    tabs = listOf(tr("进行中", "Live"), tr("历史", "History"), tr("全部", "All")),
                    selectedTabIndex = tab,
                    onTabSelected = { tab = it },
                    modifier = Modifier.padding(horizontal = PageDefaults.CardGutter).padding(bottom = 12.dp),
                )
            }
            item { SectionHeader(tr("胶囊标签页", "Chip tabs")) }
            item {
                ChipTabGroup(
                    tabs = listOf(
                        ChipTabData(tr("全部", "All")),
                        ChipTabData(tr("照片", "Photos"), count = 12),
                        ChipTabData(tr("视频", "Videos")),
                        ChipTabData(tr("文档", "Documents"), showRedDot = true),
                        ChipTabData(tr("音频", "Audio")),
                    ),
                    selectedIndex = chipTab,
                    onSelectedChange = { chipTab = it },
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = PageDefaults.CardGutter),
                    modifier = Modifier.padding(bottom = 12.dp),
                )
            }
            item { SectionHeader(tr("筛选胶囊", "Filter chips")) }
            item {
                GroupCard {
                    FlowRow(
                        Modifier.padding(CardContentPadding),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        listOf(tr("今天", "Today"), tr("本周", "This week"), tr("本月", "This month"), tr("更早", "Earlier")).forEachIndexed { index, label ->
                            Chip(
                                selected = index in selected,
                                onClick = { selected = if (index in selected) selected - index else selected + index },
                                label = label,
                            )
                        }
                    }
                }
            }
            item { SectionHeader(tr("状态标签与徽标", "Status tags and badges")) }
            item {
                GroupCard {
                    Column(Modifier.padding(CardContentPadding)) {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatusPill(tr("运行中", "Running"), TileGreen)
                            StatusPill(tr("等待确认", "Waiting"), TileOrange)
                            StatusPill(tr("出错", "Failed"), COUITheme.colorScheme.error)
                            StatusPill(tr("已结束", "Ended"), TileGrey)
                        }
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.CenterVertically) {
                            Badge()
                            Badge(count = 3)
                            Badge(count = 28)
                            Badge(count = 120)
                        }
                    }
                    BasicComponent(
                        title = tr("消息", "Messages"),
                        summary = tr("3 条未读", "3 unread"),
                        startAction = { IconTile(COUIIcons.Messages, TileGreen) },
                        endActions = { Badge(count = 3) },
                    )
                }
            }
            item { Note(tr("状态永远带文字，不只靠颜色区分。", "A state always carries text; it is never colour alone.")) }
        }
    }
}

@Composable
fun FeedbackPage(onBack: () -> Unit) {
    var progress by rememberSaveable { mutableFloatStateOf(0.45f) }
    var notice by rememberSaveable { mutableStateOf(true) }
    Page(title = tr("进度与状态", "Progress and states"), onBack = onBack, immersive = true) {
        PageList {
            item {
                if (notice) {
                    NoticeBanner(
                        text = tr("网络不稳定，正在重新连接…", "Connection is unstable, reconnecting…"),
                        color = TileOrange,
                        action = tr("知道了", "Got it"),
                        onAction = { notice = false },
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                }
            }
            item { SectionHeader(tr("进度", "Progress")) }
            item {
                GroupCard {
                    Column(Modifier.padding(CardContentPadding)) {
                        Caption(tr("确定进度 ${(progress * 100).toInt()}%", "Determinate ${(progress * 100).toInt()}%"))
                        LinearProgressIndicator(progress = progress, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(16.dp))
                        Caption(tr("不确定进度", "Indeterminate"))
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(20.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(progress = progress)
                            CircularProgressIndicator()
                            InfiniteProgressIndicator()
                            Spacer(Modifier.weight(1f))
                            SmallButton("+10%", { progress = if (progress >= 1f) 0f else (progress + 0.1f).coerceAtMost(1f) })
                        }
                    }
                }
            }
            item { SectionHeader(tr("空状态", "Empty")) }
            item { GroupCard { EmptyMessage(tr("还没有内容", "Nothing here yet"), subtitle = tr("新建的项目会显示在这里", "New items will show up here")) } }
            item { SectionHeader(tr("错误状态", "Error")) }
            item {
                GroupCard {
                    ErrorState(
                        title = tr("无法加载", "Couldn't load"),
                        message = tr("服务器没有响应。检查网络后再试一次。", "The server did not respond. Check your network and try again."),
                        retryLabel = tr("重试", "Retry"),
                        onRetry = { notice = true },
                    )
                }
            }
            item { SectionHeader(tr("加载状态", "Loading")) }
            item {
                GroupCard {
                    Box(Modifier.fillMaxWidth().height(140.dp)) {
                        io.github.tianli0.coui17.patterns.LoadingState(tr("正在加载…", "Loading…"))
                    }
                }
            }
        }
    }
}

@Composable
fun ListsPage(onBack: () -> Unit) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    var sync by rememberSaveable { mutableStateOf(true) }
    Page(title = tr("列表与偏好项", "Lists and preferences"), onBack = onBack, immersive = true) {
        PageList {
            item { SectionHeader(tr("可跳转的行", "Rows that open a page")) }
            item {
                GroupCard {
                    ArrowPreference(
                        title = tr("帐号与安全", "Account and security"),
                        startAction = { IconTile(COUIIcons.Info, TileBlue) },
                        cardListPosition = cardListPositionOf(0, 3),
                        onClick = {},
                    )
                    ArrowPreference(
                        title = tr("通知", "Notifications"),
                        summary = tr("横幅、声音、角标", "Banners, sounds, badges"),
                        startAction = { IconTile(COUIIcons.Messages, TileRed) },
                        statusText = tr("已开启", "On"),
                        cardListPosition = cardListPositionOf(1, 3),
                        onClick = {},
                    )
                    ArrowPreference(
                        title = tr("系统更新", "System update"),
                        startAction = { IconTile(COUIIcons.Refresh, TileGreen) },
                        showRedDot = true,
                        cardListPosition = cardListPositionOf(2, 3),
                        onClick = {},
                    )
                }
            }
            item { SectionHeader(tr("带控件的行", "Rows with a control")) }
            item {
                GroupCard {
                    SwitchPreference(
                        checked = sync,
                        onCheckedChange = { sync = it },
                        title = tr("自动同步", "Auto sync"),
                        summary = tr("仅在 WLAN 下同步", "Only over WLAN"),
                        cardListPosition = cardListPositionOf(0, 2),
                    )
                    BasicComponent(
                        title = tr("缓存", "Cache"),
                        summary = "128 MB",
                        endActions = { SmallButton(tr("清除", "Clear"), {}) },
                        cardListPosition = cardListPositionOf(1, 2),
                    )
                }
            }
            item { SectionHeader(tr("展开的行", "Expandable row")) }
            item {
                GroupCard {
                    BasicComponent(
                        title = tr("高级选项", "Advanced"),
                        summary = if (expanded) tr("点按收起", "Tap to collapse") else tr("点按展开", "Tap to expand"),
                        endActions = { ExpandArrow(expanded = expanded) },
                        onClick = { expanded = !expanded },
                    )
                    ExpandBody(expanded) {
                        KeyValueRow(tr("协议", "Protocol"), "TLS 1.3", cardListPosition = cardListPositionOf(1, 3))
                        KeyValueRow(tr("端口", "Port"), "47700", cardListPosition = cardListPositionOf(2, 3))
                    }
                }
            }
            item { SectionHeader(tr("键值", "Key / value")) }
            item {
                GroupCard {
                    KeyValueRow(tr("型号", "Model"), "PKX110", cardListPosition = cardListPositionOf(0, 3))
                    KeyValueRow(tr("系统版本", "System"), "ColorOS 17", cardListPosition = cardListPositionOf(1, 3))
                    KeyValueRow(tr("存储", "Storage"), "186 GB / 512 GB", cardListPosition = cardListPositionOf(2, 3))
                }
            }
            item { Note(tr("一个分组就是一张圆角卡片，距屏幕边缘 16dp；说明文字缩进 32dp，与行标题对齐。", "A group is one rounded card, 16dp from the screen edge; loose text sits at 32dp, in line with the row titles.")) }
        }
    }
}

@Composable
fun GlassPage(onBack: () -> Unit) {
    var draft by rememberSaveable { mutableStateOf("") }
    val voice = tr("语音", "Voice")
    val pageColor = pageContainerColor()
    // The list is recorded as a layer; the floating controls below are its siblings and blur it.
    val backdrop = if (isRuntimeShaderSupported()) {
        rememberLayerBackdrop {
            drawRect(pageColor)
            drawContent()
        }
    } else {
        null
    }
    val swatches = remember { listOf(TileBlue, TileGreen, TileOrange, TilePurple, TileRed, TileCyan) }
    Page(title = tr("悬浮玻璃", "Floating glass"), onBack = onBack, immersive = true, actions = {
        ToolbarIcon(COUIIcons.Refresh, tr("清空", "Clear"), onClick = { draft = "" })
    }) {
        Box(Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().then(if (backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier),
                contentPadding = pageListPadding().let {
                    androidx.compose.foundation.layout.PaddingValues(top = it.calculateTopPadding(), bottom = 120.dp)
                },
            ) {
                item { Note(tr("向上滚动，看彩色卡片从输入栏和标题栏下方经过；输入文字后发送键会填上强调色。", "Scroll and watch the coloured cards pass under the composer and the title bar; type and the send key fills with the accent.")) }
                items((0 until 18).toList()) { index ->
                    val color = swatches[index % swatches.size]
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = PageDefaults.CardGutter)
                            .padding(bottom = 12.dp)
                            .background(color.copy(alpha = 0.16f), RoundedCornerShape(20.dp))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(Modifier.size(40.dp).background(color, RoundedCornerShape(12.dp)))
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text(tr("卡片 ${index + 1}", "Card ${index + 1}"), style = COUITheme.textStyles.body1, fontWeight = FontWeight.Medium, color = COUITheme.colorScheme.onSurface)
                            Text(
                                tr("玻璃会把背后的亮度拉到自身的明暗", "Glass pulls what is behind it to its own brightness"),
                                style = COUITheme.textStyles.footnote1,
                                color = COUITheme.colorScheme.onSurfaceVariantSummary,
                            )
                        }
                    }
                }
            }
            CompositionLocalProvider(LocalGlassBackdrop provides backdrop) {
                Row(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(horizontal = PageDefaults.CardGutter, vertical = 12.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    ComposerKey(label = voice, onClick = {}) { Icon(COUIIcons.Mic, contentDescription = null) }
                    Spacer(Modifier.width(ComposerKeyGap))
                    InputPill(
                        value = draft,
                        onValueChange = { draft = it },
                        placeholder = tr("说点什么…", "Say something…"),
                        sendLabel = tr("发送", "Send"),
                        onSend = { draft = "" },
                        state = InputPillState(enabled = true, sendEnabled = draft.isNotBlank(), busy = false),
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}
