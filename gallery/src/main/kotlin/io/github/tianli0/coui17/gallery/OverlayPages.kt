package io.github.tianli0.coui17.gallery

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.suqi8.coui.kmp.basic.BasicComponent
import io.github.suqi8.coui.kmp.basic.Text
import io.github.suqi8.coui.kmp.basic.cardListPositionOf
import io.github.suqi8.coui.kmp.icon.COUIIcons
import io.github.suqi8.coui.kmp.icon.extended.More
import io.github.suqi8.coui.kmp.layout.DialogButtonBarAction
import io.github.suqi8.coui.kmp.preference.ArrowPreference
import io.github.suqi8.coui.kmp.preference.RadioButtonPreference
import io.github.suqi8.coui.kmp.theme.COUITheme
import io.github.tianli0.coui17.patterns.ActionTone
import io.github.tianli0.coui17.patterns.ConfirmDialog
import io.github.tianli0.coui17.patterns.FormDialog
import io.github.tianli0.coui17.patterns.FullScreenDialog
import io.github.tianli0.coui17.patterns.GlassMenu
import io.github.tianli0.coui17.patterns.GroupCard
import io.github.tianli0.coui17.patterns.MenuEntry
import io.github.tianli0.coui17.patterns.Page
import io.github.tianli0.coui17.patterns.PageDefaults
import io.github.tianli0.coui17.patterns.PanelSheet
import io.github.tianli0.coui17.patterns.SectionHeader
import io.github.tianli0.coui17.patterns.SheetButtons
import io.github.tianli0.coui17.patterns.SheetConfirm
import io.github.tianli0.coui17.patterns.SmallButton
import io.github.tianli0.coui17.patterns.TextInput
import io.github.tianli0.coui17.patterns.ToolbarIcon
import io.github.tianli0.coui17.patterns.pageContainerColor

@Composable
fun OverlaysPage() {
    var confirm by rememberSaveable { mutableStateOf(false) }
    var form by rememberSaveable { mutableStateOf(false) }
    var decision by rememberSaveable { mutableStateOf(false) }
    var fullScreen by rememberSaveable { mutableStateOf(false) }
    var menu by rememberSaveable { mutableStateOf(false) }
    var rename by rememberSaveable { mutableStateOf(false) }
    var newName by rememberSaveable { mutableStateOf("") }
    val renamed = tr("已重命名", "Renamed")
    var rowMenu by rememberSaveable { mutableStateOf(false) }
    var sort by rememberSaveable { mutableIntStateOf(0) }
    var last by rememberSaveable { mutableStateOf("") }
    val sorts = listOf(tr("按名称", "By name"), tr("按时间", "By date"), tr("按大小", "By size"))
    val refreshed = tr("已刷新", "Refreshed")
    val allSelected = tr("已全选", "All selected")
    val deleted = tr("已删除 3 个项目", "3 items deleted")
    val continued = tr("已继续", "Continued")
    val copied = tr("已建立副本", "Copy created")

    Page(
        title = tr("弹层", "Overlays"),
        subtitle = last,
        immersive = true,
        actions = {
            Box {
                ToolbarIcon(COUIIcons.More, tr("更多", "More"), onClick = { menu = true })
                GlassMenu(
                    show = menu,
                    onDismiss = { menu = false },
                    entries = listOf(
                        MenuEntry(tr("刷新", "Refresh")) { last = refreshed },
                        MenuEntry(tr("全选", "Select all")) { last = allSelected },
                        MenuEntry(tr("删除", "Delete"), danger = true) { confirm = true },
                    ),
                )
            }
        },
    ) {
        PageList {
            item { SectionHeader(tr("对话框", "Dialog")) }
            item {
                GroupCard {
                    ArrowPreference(
                        title = tr("确认对话框", "Confirmation"),
                        summary = tr("居中的标题、一段说明、取消 | 确认", "Centred title, one message, cancel | confirm"),
                        cardListPosition = cardListPositionOf(0, 2),
                        onClick = { confirm = true },
                    )
                    ArrowPreference(
                        title = tr("输入对话框", "Form dialog"),
                        summary = tr("问一个值：名称、行号、口令", "Asks for one value: a name, a line number"),
                        cardListPosition = cardListPositionOf(1, 2),
                        onClick = { rename = true },
                    )
                }
            }
            item { SectionHeader(tr("半屏面板", "Half-screen panel")) }
            item {
                GroupCard {
                    ArrowPreference(
                        title = tr("表单面板", "Form panel"),
                        summary = tr("确认胶囊在工具栏里，键盘弹出时仍可见", "The confirm capsule sits in the toolbar, above the keyboard"),
                        cardListPosition = cardListPositionOf(0, 2),
                        onClick = { form = true },
                    )
                    ArrowPreference(
                        title = tr("决策面板", "Decision panel"),
                        summary = tr("底部按钮栏：并排两个，三个则纵向排列", "A button bar: two side by side, three stacked"),
                        cardListPosition = cardListPositionOf(1, 2),
                        onClick = { decision = true },
                    )
                }
            }
            item { SectionHeader(tr("菜单", "Menu")) }
            item {
                GroupCard {
                    BasicComponent(
                        title = tr("排序方式", "Sort"),
                        summary = sorts[sort],
                        endActions = {
                            Box {
                                SmallButton(tr("更改", "Change"), { rowMenu = true })
                                GlassMenu(
                                    show = rowMenu,
                                    onDismiss = { rowMenu = false },
                                    entries = sorts.mapIndexed { index, label -> MenuEntry(label, selected = sort == index) { sort = index } },
                                )
                            }
                        },
                    )
                }
            }
            item { Note(tr("菜单开在自己的窗口里，下面垫的是当前页面的模糊快照；右上角的“更多”也是同一个菜单。", "A menu opens in its own window over a blurred snapshot of the page; the More key in the title bar opens the same menu.")) }
            item { SectionHeader(tr("全屏页面", "Full-screen page")) }
            item {
                GroupCard {
                    ArrowPreference(
                        title = tr("全屏对话框", "Full-screen dialog"),
                        summary = tr("自己的窗口，延伸到系统栏之下", "Its own window, drawn under the system bars"),
                        onClick = { fullScreen = true },
                    )
                }
            }
        }
    }

    ConfirmDialog(
        show = confirm,
        title = tr("删除这 3 个项目？", "Delete these 3 items?"),
        message = tr("删除后可以在“最近删除”里保留 30 天。", "They stay in Recently deleted for 30 days."),
        confirmText = tr("删除", "Delete"),
        onConfirm = {
            confirm = false
            last = deleted
        },
        onDismiss = { confirm = false },
    )

    FormDialog(
        show = rename,
        title = tr("重命名", "Rename"),
        confirmText = tr("确定", "OK"),
        confirmEnabled = newName.isNotBlank(),
        onConfirm = {
            rename = false
            last = renamed
        },
        onDismiss = { rename = false },
    ) {
        TextInput(value = newName, onValueChange = { newName = it }, label = tr("名称", "Name"), showClearButton = true)
    }

    var name by rememberSaveable { mutableStateOf("") }
    var kind by rememberSaveable { mutableIntStateOf(0) }
    val created = tr("已创建“$name”", "Created \"$name\"")
    PanelSheet(
        show = form,
        title = tr("新建相册", "New album"),
        onDismissRequest = { form = false },
        confirm = SheetConfirm(tr("创建", "Create"), enabled = name.isNotBlank()) {
            form = false
            last = created
            name = ""
        },
    ) {
        Column(Modifier.fillMaxWidth()) {
            TextInput(
                value = name,
                onValueChange = { name = it },
                label = tr("相册名称", "Album name"),
                showClearButton = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = PageDefaults.CardGutter),
            )
            SectionHeader(tr("类型", "Type"))
            GroupCard {
                val kinds = listOf(tr("普通相册", "Album"), tr("共享相册", "Shared album"), tr("私密相册", "Private album"))
                kinds.forEachIndexed { index, label ->
                    RadioButtonPreference(
                        title = label,
                        selected = kind == index,
                        onClick = { kind = index },
                        cardListPosition = cardListPositionOf(index, kinds.size),
                    )
                }
            }
            Note(tr("面板画在页面底色上，卡片和页面里一样距边缘 16dp。", "The panel is drawn on the page colour; cards sit 16dp from the edge exactly as on a page."))
        }
    }

    PanelSheet(show = decision, title = tr("在手机上继续", "Continue on phone"), onDismissRequest = { decision = false }) {
        Column(Modifier.fillMaxWidth()) {
            Text(
                tr(
                    "对话会带着同样的上下文继续。你也可以先建一个副本，保持原来的对话不变。",
                    "The conversation continues with the same context. Or continue in a copy and leave the original untouched.",
                ),
                style = COUITheme.textStyles.body1,
                color = COUITheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = PageDefaults.TextGutter),
            )
            Spacer(Modifier.height(20.dp))
            SheetButtons(
                primary = DialogButtonBarAction(tr("继续", "Continue"), onClick = {
                    decision = false
                    last = continued
                }),
                secondary = DialogButtonBarAction(tr("以副本继续", "Continue in a copy"), onClick = {
                    decision = false
                    last = copied
                }),
                modifier = Modifier.padding(horizontal = PageDefaults.CardGutter),
            )
            Spacer(Modifier.height(16.dp))
        }
    }

    if (fullScreen) {
        FullScreenDialog(onDismissRequest = { fullScreen = false }) {
            Box(Modifier.fillMaxSize().background(pageContainerColor()).systemBarsPadding(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = PageDefaults.TextGutter)) {
                    Text(tr("全屏页面", "Full-screen page"), style = COUITheme.textStyles.title2, color = COUITheme.colorScheme.onSurface)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        tr("没有压暗，也没有窗口动画，系统栏保持页面的颜色。", "No dim, no window animation, and the system bars keep the page's colours."),
                        style = COUITheme.textStyles.body2,
                        color = COUITheme.colorScheme.onSurfaceVariantSummary,
                    )
                    Spacer(Modifier.height(20.dp))
                    SmallButton(tr("关闭", "Close"), { fullScreen = false }, tone = ActionTone.Primary)
                }
            }
        }
    }
}
