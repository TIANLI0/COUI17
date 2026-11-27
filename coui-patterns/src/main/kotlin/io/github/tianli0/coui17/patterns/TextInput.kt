// Copyright 2026, COUI17 contributors
// SPDX-License-Identifier: Apache-2.0

package io.github.tianli0.coui17.patterns

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import io.github.suqi8.coui.kmp.basic.TextField
import io.github.suqi8.coui.kmp.basic.TextFieldDefaults
import io.github.suqi8.coui.kmp.basic.TextFieldMode
import io.github.suqi8.coui.kmp.theme.COUITheme

/**
 * Whether [TextInput] uses the filled, rounded variant shared by [Page], [PanelSheet] and
 * [FormDialog]. [GroupCard] uses the compact outlined variant against its own fill.
 */
val LocalOnPageColor = staticCompositionLocalOf { false }

/**
 * A `String`-based COUI text field that keeps selection and IME composition (which CJK input
 * depends on) while the owner only stores the plain text. A field driven directly by a `String`
 * state loses the composing region whenever the owner writes the text back a frame late.
 */
@Composable
fun TextInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else 6,
    showClearButton: Boolean = false,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    /** The fill of the field; by default the card colour on the page colour, transparent inside a card. */
    containerColor: Color = if (LocalOnPageColor.current) COUITheme.colorScheme.surfaceContainer else Color.Transparent,
    /**
     * On the page colour the field is a card among the group cards and takes their corner radius;
     * inside a card it keeps COUI's own field radius.
     */
    cornerRadius: Dp = if (LocalOnPageColor.current) PageDefaults.FieldCornerRadius else TextFieldDefaults.CornerRadius,
    /** A password field: dots, with the eye key that reveals the text. */
    showPasswordToggle: Boolean = false,
) {
    var tfv by remember { mutableStateOf(TextFieldValue(value, TextRange(value.length))) }
    // Only adopt the external value when the OWNER changed it (e.g. cleared after send); our own
    // edits reach the owner asynchronously and must not reset selection/composition meanwhile.
    var lastExternal by remember { mutableStateOf(value) }
    if (value != lastExternal) {
        lastExternal = value
        if (value != tfv.text) tfv = TextFieldValue(value, TextRange(value.length))
    }
    TextField(
        value = tfv,
        onValueChange = {
            tfv = it
            if (it.text != value) onValueChange(it.text)
        },
        modifier = modifier,
        backgroundMode = TextFieldMode.Rectangle,
        colors = TextFieldDefaults.textFieldColors(backgroundColor = containerColor),
        cornerRadius = cornerRadius,
        showPasswordToggle = showPasswordToggle,
        label = label,
        singleLine = singleLine,
        maxLines = maxLines,
        showClearButton = showClearButton,
        enabled = enabled,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
    )
}
