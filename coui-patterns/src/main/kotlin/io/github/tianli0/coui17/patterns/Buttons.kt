// Copyright 2026, COUI17 contributors
// SPDX-License-Identifier: Apache-2.0

package io.github.tianli0.coui17.patterns

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.suqi8.coui.kmp.basic.Button
import io.github.suqi8.coui.kmp.basic.ButtonDefaults
import io.github.suqi8.coui.kmp.basic.ButtonSize
import io.github.suqi8.coui.kmp.basic.RotatingProgressIndicator
import io.github.suqi8.coui.kmp.basic.Text
import io.github.suqi8.coui.kmp.theme.COUITheme

/*
 * Button tiers (docs/DESIGN.md). A coui `Button` / `TextButton` with default arguments is the
 * Large tier: a 44dp filled capsule. That tier belongs to the bottom of a panel or of an onboarding
 * step only; everything inside a page, a card or a row uses one of the tiers below.
 */

/** How much weight a compact action carries. */
enum class ActionTone {
    /** Grey capsule, accent label: the ordinary action of a row or card (ColorOS list button). */
    Default,

    /** Accent capsule, white label: the one action a card is about (allow, submit, save). */
    Primary,

    /** Grey capsule, red label: stop, delete, unbind. */
    Danger,
}

/** 14sp medium, the label of the COUI small button tier. */
@Composable
fun compactButtonTextStyle(): TextStyle = COUITheme.textStyles.button.copy(fontSize = 14.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium)

/**
 * COUI Small tier capsule (28dp high): an action inside a row, a card or a toolbar. Never stretched
 * to the full width; several of them sit end-aligned, 8dp apart.
 *
 * While [loading] the label is replaced by a spinner of the same colour and the button takes no
 * taps; its width does not change, so the row does not jump.
 */
@Composable
fun SmallButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tone: ActionTone = ActionTone.Default,
    loading: Boolean = false,
) {
    val scheme = COUITheme.colorScheme
    val colors = when (tone) {
        ActionTone.Primary -> ButtonDefaults.buttonColorsPrimary()
        ActionTone.Default -> ButtonDefaults.buttonColors(contentColor = scheme.primary)
        ActionTone.Danger -> ButtonDefaults.buttonColors(contentColor = scheme.error)
    }
    val active = enabled && !loading
    val content = if (enabled) colors.contentColor else colors.disabledContentColor
    Button(onClick = { if (active) onClick() }, modifier = modifier, enabled = enabled, colors = colors, size = ButtonSize.Small) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text,
                style = compactButtonTextStyle(),
                color = content,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.alpha(if (loading) 0f else 1f),
            )
            if (loading) RotatingProgressIndicator(color = content, size = 16.dp, ringDiameter = 12.8.dp, strokeWidth = 2.dp)
        }
    }
}

/**
 * Borderless accent label: "load more", "see all", "retry" under a list or next to a line of
 * text. It has no fill, so its 36dp height is touch target, not visual weight.
 */
@Composable
fun LinkButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = COUITheme.colorScheme.primary,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            color = Color.Transparent,
            disabledColor = Color.Transparent,
            contentColor = color,
            disabledContentColor = COUITheme.colorScheme.disabledOnSecondaryVariant,
        ),
        minHeight = 36.dp,
        minWidth = 48.dp,
        insideMargin = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Text(
            text,
            style = compactButtonTextStyle(),
            color = if (enabled) color else COUITheme.colorScheme.disabledOnSecondaryVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
