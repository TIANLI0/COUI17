// Copyright 2026, COUI17 contributors
// SPDX-License-Identifier: Apache-2.0

package io.github.tianli0.coui17.patterns

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.graphics.luminance
import io.github.suqi8.coui.kmp.basic.Icon
import io.github.suqi8.coui.kmp.basic.RotatingProgressIndicator
import io.github.suqi8.coui.kmp.basic.Text
import io.github.suqi8.coui.kmp.icon.COUIIcons
import io.github.suqi8.coui.kmp.icon.extended.Send
import io.github.suqi8.coui.kmp.theme.COUITheme

/**
 * What an [InputPill] can do right now. [enabled] false: the field is not connected to anything
 * (callers may still keep the text as a local draft). [sendEnabled]: there is something to send.
 * [busy]: a send is in flight; the key shows a spinner.
 */
@Immutable
data class InputPillState(val enabled: Boolean, val sendEnabled: Boolean, val busy: Boolean)

/** The diameter of a composer key and the height of its one-line field: coui's toolbar key. */
val ComposerKeySize = 40.dp

/** The gap between toolbar keys (coui `TopAppBarDefaults.ActionMenuItemSpacing`). */
val ComposerKeyGap = 10.dp

/**
 * A message composer in ColorOS 17's toolbar vocabulary: a 40dp glass capsule that grows with the
 * text, and next to it the toolbar's own glass key ([ComposerKey], the same disc as the title bar's
 * back key) which fills with the accent when there is something to send.
 *
 * Floating over a list, both are frosted glass: record the list as a `LayerBackdrop` and provide it
 * as [LocalGlassBackdrop]. Without one they are the opaque card colour. Put further keys (attach,
 * voice) before it in the same `Row`, [ComposerKeyGap] apart.
 */
@Composable
fun InputPill(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    sendLabel: String,
    onSend: () -> Unit,
    state: InputPillState,
    modifier: Modifier = Modifier,
    fieldModifier: Modifier = Modifier,
    sendModifier: Modifier = Modifier,
    maxLines: Int = 5,
    sendIcon: ImageVector = COUIIcons.Send,
) {
    val scheme = COUITheme.colorScheme
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
        Box(
            Modifier
                .weight(1f)
                .heightIn(min = ComposerKeySize)
                .glassSurface(RoundedCornerShape(ComposerKeySize / 2), halo = true)
                .padding(horizontal = 16.dp, vertical = 9.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            if (value.isEmpty()) {
                Text(placeholder, style = COUITheme.textStyles.body1, color = scheme.onSurfaceVariantSummary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = COUITheme.textStyles.body1.copy(color = scheme.onSurface),
                cursorBrush = SolidColor(scheme.primary),
                maxLines = maxLines,
                modifier = fieldModifier.fillMaxWidth().semantics { contentDescription = placeholder },
            )
        }
        Spacer(Modifier.size(ComposerKeyGap))
        val canSend = state.sendEnabled && !state.busy
        // Ready: the disc fills with the accent and the arrow turns white.
        val ready by animateFloatAsState(if (canSend) 1f else 0f, animationSpec = spring(dampingRatio = 0.8f, stiffness = 500f), label = "send-ready")
        ComposerKey(
            label = sendLabel,
            onClick = onSend,
            enabled = canSend,
            modifier = sendModifier,
        ) {
            Box(Modifier.matchParentSize().graphicsLayer { alpha = ready }.clip(CircleShape).background(scheme.primary))
            if (state.busy) {
                RotatingProgressIndicator(color = scheme.onSurfaceSecondary, size = 18.dp, ringDiameter = 14.4.dp, strokeWidth = 2.dp)
            } else {
                // The arrow's colour follows `ready` while drawing: read here, in composition, it
                // recomposed the whole composer (field included) on every frame of the change.
                val arrow = rememberVectorPainter(sendIcon)
                val idle = scheme.onSurfaceVariantSummary
                val onAccent = scheme.onPrimary
                Box(
                    Modifier.size(20.dp).drawBehind {
                        with(arrow) { draw(size, colorFilter = ColorFilter.tint(lerp(idle, onAccent, ready))) }
                    },
                )
            }
        }
    }
}

/**
 * A round key of a composer row: ColorOS 17's glass toolbar key over the content, with its halo,
 * press light and touch motion. [content] is drawn inside the 40dp disc.
 */
@Composable
fun ComposerKey(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    // The same frosted glass as the field beside it: blur only, no rim, halo or press light. The
    // glass may be drawn by an enclosing GlassGroup, which does not follow a scaled key, so the press
    // is a light dim of the disc instead of a scale.
    val interaction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val dim by animateFloatAsState(if (pressed) 1f else 0f, animationSpec = spring(dampingRatio = 1f, stiffness = 1500f), label = "key-press")
    val dark = COUITheme.colorScheme.background.luminance() < 0.5f
    Box(
        modifier
            .size(ComposerKeySize)
            .glassSurface(CircleShape)
            .drawWithContent {
                drawContent()
                if (dim > 0f) drawRect((if (dark) Color.White else Color.Black).copy(alpha = 0.10f * dim))
            }
            .clickable(enabled = enabled, role = Role.Button, onClickLabel = label, indication = null, interactionSource = interaction, onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
        content = content,
    )
}

/** A 48dp round glass button floating over content (close a viewer, download), with the toolbar halo. */
@Composable
fun RoundIconButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = COUITheme.colorScheme.onSurface,
    enabled: Boolean = true,
) {
    Box(
        modifier
            .size(48.dp)
            .glassSurface(CircleShape, halo = true)
            .clickable(enabled = enabled, role = Role.Button, onClickLabel = label, onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) { Icon(icon, contentDescription = null, tint = if (enabled) tint else tint.copy(alpha = 0.4f), modifier = Modifier.size(22.dp)) }
}
