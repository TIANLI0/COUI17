// Copyright 2026, COUI17 contributors
// SPDX-License-Identifier: Apache-2.0

package io.github.tianli0.coui17.patterns

import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import io.github.suqi8.coui.kmp.layout.DialogDefaults
import io.github.suqi8.coui.kmp.input.couiLongPressSelection
import io.github.suqi8.coui.kmp.input.couiLongPressSelectionItem
import io.github.suqi8.coui.kmp.input.rememberCOUILongPressSelectionState
import io.github.suqi8.coui.kmp.preference.RadioButtonPreference
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsIgnoringVisibility
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.github.suqi8.coui.kmp.basic.Button
import io.github.suqi8.coui.kmp.basic.ButtonDefaults
import io.github.suqi8.coui.kmp.basic.Text
import io.github.suqi8.coui.kmp.layout.BottomSheetDefaults
import io.github.suqi8.coui.kmp.layout.DialogButtonBar
import io.github.suqi8.coui.kmp.layout.DialogButtonBarAction
import io.github.suqi8.coui.kmp.layout.SheetCloseCapsule
import io.github.suqi8.coui.kmp.theme.COUITheme
import io.github.suqi8.coui.kmp.theme.LocalDismissState
import io.github.suqi8.coui.kmp.utils.RemovePlatformDialogDefaultEffects
import io.github.suqi8.coui.kmp.window.WindowAlertDialog
import io.github.suqi8.coui.kmp.window.WindowBottomSheet
import io.github.suqi8.coui.kmp.window.WindowDialog

/** The confirming action of a form panel ("create", "save"): the accent capsule at the end of its toolbar. */
data class SheetConfirm(val text: String, val enabled: Boolean = true, val testTag: String? = null, val onClick: () -> Unit)

/**
 * A ColorOS 17 half-screen panel: a coui `WindowBottomSheet` drawn on the **page colour** with the
 * page's own gutters (the sheet's inside margin is off), so a [GroupCard] reads as a card 16dp from
 * the edge exactly as it does on a page. On the default sheet colour, cards are white on white.
 *
 * Its toolbar is one row: the close capsule (wired through `LocalDismissState` so it animates
 * out), the centred [title], and — for a form — [confirm] as an accent capsule, which stays in view
 * while the keyboard is open. A panel that asks for a decision ends with [SheetButtons] instead.
 * Loose text inside uses [PageDefaults.TextGutter].
 */
@Composable
fun PanelSheet(
    show: Boolean,
    title: String?,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    confirm: SheetConfirm? = null,
    endAction: (@Composable () -> Unit)? = null,
    imePadding: Boolean = true,
    content: @Composable () -> Unit,
) {
    @OptIn(ExperimentalLayoutApi::class)
    val navBottom = WindowInsets.navigationBarsIgnoringVisibility.asPaddingValues().calculateBottomPadding()
    WindowBottomSheet(
        show = show,
        modifier = modifier,
        title = title,
        startAction = {
            val dismiss = LocalDismissState.current
            SheetCloseCapsule(
                onClick = { dismiss?.invoke() ?: onDismissRequest() },
                modifier = Modifier.padding(start = PageDefaults.CardGutter),
            )
        },
        endAction = endAction?.let { action ->
            { Box(Modifier.padding(end = PageDefaults.CardGutter)) { action() } }
        } ?: confirm?.let { action ->
            { Box(Modifier.padding(end = PageDefaults.CardGutter)) { SheetConfirmCapsule(action) } }
        },
        backgroundColor = pageContainerColor(),
        insideMargin = DpSize(0.dp, 0.dp),
        defaultWindowInsetsPadding = imePadding,
        panelPaddingBottom = navBottom,
        headerBottomPadding = 8.dp,
        onDismissRequest = onDismissRequest,
    ) {
        Column(Modifier.fillMaxWidth()) {
            CompositionLocalProvider(LocalOnPageColor provides true) { content() }
        }
    }
}

/** The toolbar's confirming capsule: the close capsule's 40dp height, filled with the accent. */
@Composable
private fun SheetConfirmCapsule(confirm: SheetConfirm) {
    val colors = ButtonDefaults.buttonColorsPrimary()
    Button(
        onClick = confirm.onClick,
        enabled = confirm.enabled,
        colors = colors,
        minHeight = BottomSheetDefaults.ActionButtonSize,
        minWidth = 60.dp,
        insideMargin = PaddingValues(horizontal = 16.dp),
        modifier = (if (confirm.testTag != null) Modifier.testTag(confirm.testTag) else Modifier)
            .then(if (confirm.enabled) Modifier.floatingHalo(CircleShape) else Modifier),
    ) {
        Text(
            confirm.text,
            style = compactButtonTextStyle(),
            color = if (confirm.enabled) colors.contentColor else colors.disabledContentColor,
            maxLines = 1,
        )
    }
}

/**
 * The actions under a panel that asks for a decision (not a form): the COUI17 button bar — two 44dp
 * capsules side by side, or stacked when there are three or the labels are long — 16dp in from the
 * panel's edges. [primary] is the filled one.
 */
@Composable
fun SheetButtons(
    primary: DialogButtonBarAction?,
    modifier: Modifier = Modifier,
    secondary: DialogButtonBarAction? = null,
    tertiary: DialogButtonBarAction? = null,
) {
    // Stacked, the bar reads neutral / positive / negative: the primary action goes first.
    val recommended = primary?.copy(recommended = true)
    DialogButtonBar(
        negative = if (tertiary != null) tertiary else secondary,
        positive = if (tertiary != null) secondary else recommended,
        neutral = if (tertiary != null) recommended else null,
        modifier = modifier.fillMaxWidth(),
    )
}

/**
 * A page shown in its own window over the current one (a viewer, an editor). The window runs edge
 * to edge like the activity's: [content] draws under the status and navigation bars and pads
 * itself with the insets, and there is no dim or window animation. A plain `Dialog` stops at the
 * bars and leaves a strip of its own colour there.
 */
@Composable
fun FullScreenDialog(onDismissRequest: () -> Unit, content: @Composable () -> Unit) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        RemovePlatformDialogDefaultEffects()
        content()
    }
}

/**
 * The centred COUI17 confirmation for a write: title, one message, cancel | confirm. [content] adds
 * detail under the message (what exactly will change).
 */
@Composable
fun ConfirmDialog(
    show: Boolean,
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    cancelText: String = stringResource(R.string.coui17_action_cancel),
    content: (@Composable () -> Unit)? = null,
) {
    WindowAlertDialog(
        show = show,
        onConfirm = onConfirm,
        onCancel = onDismiss,
        title = title,
        summary = message,
        confirmText = confirmText,
        cancelText = cancelText,
        content = content,
    )
}

/** Side gutter of fields and text inside a centred dialog. */
val DialogContentGutter = 24.dp

/**
 * A centred dialog that asks for a value (a name, a line number, a credential): the title,
 * [content] at the dialog gutter, then the COUI17 button bar with cancel | [confirmText]. The
 * confirm capsule is disabled while [confirmEnabled] is false. Put a [TextInput] and, if needed, a
 * line of help or error text in [content]; for more than two fields use a [PanelSheet].
 */
@Composable
fun FormDialog(
    show: Boolean,
    title: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    confirmEnabled: Boolean = true,
    cancelText: String = stringResource(R.string.coui17_action_cancel),
    content: @Composable ColumnScope.() -> Unit,
) {
    WindowDialog(show = show, modifier = modifier, title = title, onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth()) {
            Column(
                Modifier.fillMaxWidth().padding(horizontal = DialogContentGutter, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CompositionLocalProvider(LocalOnPageColor provides true) { content() }
            }
            DialogButtonBar(
                negative = DialogButtonBarAction(text = cancelText, onClick = onDismiss),
                positive = DialogButtonBarAction(text = confirmText, enabled = confirmEnabled, onClick = onConfirm),
            )
        }
    }
}

/** One option of a [ChoiceDialog]. [value] identifies it and must be unique within the dialog. */
data class ChoiceEntry<T>(
    val value: T,
    val text: String,
    val summary: String? = null,
    val enabled: Boolean = true,
)

/**
 * The centred COUI17 single-choice dialog: the title stays put, an optional [message] and the
 * options scroll together under it, and cancel stays at the bottom (COUI's single-choice alert:
 * `setSingleChoiceItems` with the tail mark of `COUIMarkPreference`). Tapping an option selects it
 * and closes the dialog, as the native one does; the current one carries the mark.
 *
 * For a choice that belongs on the page (a setting with a few values) list
 * `RadioButtonPreference` rows in a [GroupCard] instead; this is for a choice asked in passing.
 */
@Composable
fun <T> ChoiceDialog(
    show: Boolean,
    title: String,
    entries: List<ChoiceEntry<T>>,
    selected: T?,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    message: String? = null,
    cancelText: String = stringResource(R.string.coui17_action_cancel),
) {
    require(entries.map { it.value }.toSet().size == entries.size) { "ChoiceDialog entries need unique values" }
    WindowDialog(show = show, modifier = modifier, title = title, onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth()) {
            val selection = rememberCOUILongPressSelectionState()
            Column(
                Modifier
                    .weight(1f, fill = false)
                    .couiLongPressSelection(selection)
                    .verticalScroll(rememberScrollState())
                    .selectableGroup(),
            ) {
                if (!message.isNullOrBlank()) {
                    // COUI centres a one-line message and start-aligns it once it wraps.
                    var wraps by remember { mutableStateOf(false) }
                    Text(
                        message,
                        style = COUITheme.textStyles.footnote1,
                        color = DialogDefaults.summaryColor(),
                        textAlign = if (wraps) TextAlign.Start else TextAlign.Center,
                        onTextLayout = { wraps = it.lineCount > 1 },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = DialogContentGutter).padding(bottom = 8.dp),
                    )
                }
                CompositionLocalProvider(LocalOnPageColor provides false) {
                    entries.forEachIndexed { index, entry ->
                        val key = remember(entry.value) { Any() }
                        val choose = {
                            if (show && entry.enabled) {
                                onSelect(entry.value)
                                onDismiss()
                            }
                        }
                        RadioButtonPreference(
                            title = entry.text,
                            summary = entry.summary,
                            selected = selection.previewKey?.let { it === key } ?: (entry.value == selected),
                            enabled = entry.enabled,
                            insideMargin = PaddingValues(horizontal = DialogContentGutter, vertical = 14.dp),
                            onClick = choose,
                            modifier = Modifier.testTag("choice-$index")
                                .couiLongPressSelectionItem(selection, key, show && entry.enabled, choose),
                        )
                    }
                }
            }
            DialogButtonBar(negative = DialogButtonBarAction(text = cancelText, onClick = onDismiss), positive = null)
        }
    }
}
