package io.github.tianli0.coui17.gallery

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import io.github.suqi8.coui.kmp.basic.cardListPositionOf
import io.github.suqi8.coui.kmp.core.ExperimentalCOUIApi
import io.github.suqi8.coui.kmp.layout.DialogButtonBarAction
import io.github.suqi8.coui.kmp.overlay.OverlayLoadingDialog
import io.github.suqi8.coui.kmp.overlay.OverlayProgressDialog
import io.github.suqi8.coui.kmp.overlay.OverlaySecurityDialog
import io.github.suqi8.coui.kmp.preference.ArrowPreference
import io.github.suqi8.coui.kmp.preference.SwitchPreference
import io.github.suqi8.coui.kmp.window.GuidePage
import io.github.suqi8.coui.kmp.window.WindowGuideDialog
import io.github.suqi8.coui.kmp.window.WindowLoadingDialog
import io.github.suqi8.coui.kmp.window.WindowProgressDialog
import io.github.suqi8.coui.kmp.window.WindowSecurityDialog
import io.github.tianli0.coui17.patterns.GroupCard
import io.github.tianli0.coui17.patterns.KeyValueRow
import io.github.tianli0.coui17.patterns.Page
import io.github.tianli0.coui17.patterns.SectionHeader
import kotlinx.coroutines.delay

private enum class DialogDemo { Loading, Progress, Security, Guide }

@OptIn(ExperimentalCOUIApi::class)
@Composable
internal fun DialogDemoPage(onBack: () -> Unit) {
    var inWindow by rememberSaveable { mutableStateOf(true) }
    var shown by rememberSaveable { mutableStateOf<DialogDemo?>(null) }
    var progress by rememberSaveable { mutableIntStateOf(0) }
    var last by rememberSaveable { mutableStateOf("") }
    val completed = tr("已完成", "Completed")
    val canceled = tr("已取消", "Canceled")
    val confirmed = tr("已确认", "Confirmed")
    val confirmedWithCheckbox = tr("已确认并勾选不再提醒", "Confirmed with “Don’t remind me again” selected")

    fun dismiss() {
        shown = null
        last = canceled
    }

    LaunchedEffect(shown) {
        when (shown) {
            DialogDemo.Loading -> {
                delay(2400)
                last = completed
                shown = null
            }
            DialogDemo.Progress -> {
                progress = 0
                while (progress < 100) {
                    delay(125)
                    progress += 5
                }
                delay(400)
                last = completed
                shown = null
            }
            else -> Unit
        }
    }

    Page(title = tr("专用对话框", "Specialized dialogs"), onBack = onBack) {
        PageList {
            item {
                GroupCard {
                    SwitchPreference(
                        title = tr("独立窗口", "Separate window"),
                        summary = tr("关掉后在当前页面中显示对话框", "Turn off to show dialogs in the current page"),
                        checked = inWindow,
                        onCheckedChange = { inWindow = it },
                    )
                    if (last.isNotEmpty()) KeyValueRow(tr("上次操作", "Last action"), last)
                }
            }
            item { SectionHeader(tr("加载与安全确认", "Loading and security confirmation")) }
            item {
                GroupCard {
                    val labels = listOf(tr("加载对话框", "Loading dialog"), tr("进度对话框", "Progress dialog"), tr("安全确认", "Security confirmation"))
                    val demos = listOf(DialogDemo.Loading, DialogDemo.Progress, DialogDemo.Security)
                    labels.forEachIndexed { index, label ->
                        ArrowPreference(
                            title = label,
                            cardListPosition = cardListPositionOf(index, labels.size),
                            onClick = { shown = demos[index] },
                        )
                    }
                }
            }
            item { SectionHeader(tr("引导", "Guide")) }
            item {
                GroupCard {
                    ArrowPreference(
                        title = tr("多页引导面板", "Multi-page guide"),
                        onClick = { shown = DialogDemo.Guide },
                    )
                }
            }
        }

        val cancel = tr("取消", "Cancel")
        val loading = tr("正在读取文件…", "Reading files…")
        val cancelAction = DialogButtonBarAction(cancel, onClick = ::dismiss)
        val title = tr("允许删除演示文件？", "Allow deleting demo files?")
        val summary = tr("示例仅用于演示确认动作。", "This example previews a confirmation action.")
        val rememberChoice = tr("不再提醒", "Don’t remind me again")
        val allow = tr("允许", "Allow")
        val onConfirm: (Boolean) -> Unit = { checked ->
            last = if (checked) confirmedWithCheckbox else confirmed
            shown = null
        }

        if (inWindow) {
            WindowLoadingDialog(show = shown == DialogDemo.Loading, text = loading, cancelAction = cancelAction, onDismissRequest = ::dismiss)
            WindowProgressDialog(show = shown == DialogDemo.Progress, progress = progress / 100f, text = "$progress%", onDismissRequest = ::dismiss)
            WindowSecurityDialog(
                show = shown == DialogDemo.Security,
                title = title,
                summary = summary,
                checkboxText = rememberChoice,
                confirmText = allow,
                cancelText = cancel,
                onConfirm = onConfirm,
                onCancel = ::dismiss,
            )
        } else {
            OverlayLoadingDialog(show = shown == DialogDemo.Loading, text = loading, cancelAction = cancelAction, onDismissRequest = ::dismiss)
            OverlayProgressDialog(show = shown == DialogDemo.Progress, progress = progress / 100f, text = "$progress%", onDismissRequest = ::dismiss)
            OverlaySecurityDialog(
                show = shown == DialogDemo.Security,
                title = title,
                summary = summary,
                checkboxText = rememberChoice,
                confirmText = allow,
                cancelText = cancel,
                onConfirm = onConfirm,
                onCancel = ::dismiss,
            )
        }

        WindowGuideDialog(
            show = shown == DialogDemo.Guide,
            pages = listOf(
                GuidePage(tr("选择文件", "Choose a file"), tr("点按文件查看详情。", "Tap a file to see its details.")),
                GuidePage(tr("预览所选内容", "Preview your selection"), tr("滑动可查看其他页面。", "Swipe to move between pages.")),
                GuidePage(tr("完成", "All set"), tr("可以返回组件列表。", "Return to the component list.")),
            ),
            skipText = tr("跳过", "Skip"),
            nextText = tr("下一页", "Next"),
            knownText = tr("完成", "Done"),
            onDismissRequest = { shown = null },
        )
    }
}
