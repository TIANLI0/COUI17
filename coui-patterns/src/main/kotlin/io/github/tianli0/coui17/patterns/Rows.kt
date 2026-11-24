// Copyright 2026, COUI17 contributors
// SPDX-License-Identifier: Apache-2.0

package io.github.tianli0.coui17.patterns

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.suqi8.coui.kmp.basic.Button
import io.github.suqi8.coui.kmp.basic.ButtonDefaults
import io.github.suqi8.coui.kmp.basic.ExpandArrow
import io.github.suqi8.coui.kmp.basic.Icon
import io.github.suqi8.coui.kmp.basic.Text
import io.github.suqi8.coui.kmp.basic.TextButton
import io.github.suqi8.coui.kmp.squircle.SquircleShape
import io.github.suqi8.coui.kmp.theme.COUITheme

/** Accessibility state words (expanded, collapsed) of collapsible rows; apps provide localized ones at their root. */
val LocalExpandStateLabels = staticCompositionLocalOf { "expanded" to "collapsed" }

/** Where a row sits inside a visually grouped card that spans several lazy-list items. */
enum class SegmentPosition { None, Single, First, Middle, Last }

/**
 * Card background for one segment of a group that spans several lazy-list items: each row stays
 * its own lazy item (so it can animate and recompose alone) while the rows read as one card.
 */
@Composable
fun Modifier.segment(position: SegmentPosition, radius: Dp = 16.dp, color: Color = COUITheme.colorScheme.surfaceContainer): Modifier {
    if (position == SegmentPosition.None) return this
    val shape = when (position) {
        SegmentPosition.Single -> SquircleShape(radius)
        SegmentPosition.First -> SquircleShape(topStart = radius, topEnd = radius, bottomEnd = 0.dp, bottomStart = 0.dp)
        SegmentPosition.Last -> SquircleShape(topStart = 0.dp, topEnd = 0.dp, bottomEnd = radius, bottomStart = radius)
        else -> RectangleShape
    }
    return this.clip(shape).background(color)
}

/** A small round status mark. Pair it with text: state is never colour alone. */
@Composable
fun StatusDot(color: Color, modifier: Modifier = Modifier, size: Dp = 8.dp) {
    Box(modifier.size(size).clip(CircleShape).background(color))
}

/**
 * A dense tappable row that expands something below it: a tinted squircle icon, a title, an
 * optional second line, a trailing slot and the COUI expand arrow. Denser than a list row (44dp);
 * for timelines and logs. Pair it with [ExpandBody].
 */
@Composable
fun CollapsibleRow(
    icon: ImageVector,
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    toggleLabel: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    iconTint: Color = COUITheme.colorScheme.onSurfaceSecondary,
    titleColor: Color = COUITheme.colorScheme.onSurface,
    titleMaxLines: Int = 1,
    subtitleMaxLines: Int = 1,
    showArrow: Boolean = true,
    trailing: @Composable RowScope.() -> Unit = {},
) {
    val labels = LocalExpandStateLabels.current
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = 44.dp)
            .clickable(role = Role.Button, onClickLabel = toggleLabel, onClick = onToggle)
            .semantics { stateDescription = if (expanded) labels.first else labels.second }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(28.dp).clip(SquircleShape(10.dp)).background(iconTint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) { Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp)) }
        Spacer(Modifier.size(10.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = COUITheme.textStyles.body2, color = titleColor, maxLines = titleMaxLines, overflow = TextOverflow.Ellipsis)
            if (!subtitle.isNullOrBlank()) {
                Text(subtitle, style = COUITheme.textStyles.footnote2, color = COUITheme.colorScheme.onSurfaceVariantSummary, maxLines = subtitleMaxLines, overflow = TextOverflow.Ellipsis)
            }
        }
        Spacer(Modifier.size(6.dp))
        trailing()
        if (showArrow) ExpandArrow(expanded = expanded, durationMillis = EXPAND_MS)
    }
}

/**
 * A header capsule: 30dp high, 13sp medium. A state capsule ([dot] given) carries its state's
 * colour as a soft tint with a coloured dot; an action capsule ([onClick]) has an accent icon.
 * Floating over content ([backdrop] non-null) it is frosted glass, see [glassSurface]; otherwise it
 * sits on the card colour.
 */
@Composable
fun InfoChip(
    text: String,
    modifier: Modifier = Modifier,
    dot: Color? = null,
    color: Color = COUITheme.colorScheme.onSurfaceSecondary,
    icon: ImageVector? = null,
    onClick: (() -> Unit)? = null,
    /** Non-null for a chip floating over content (the colour itself is not used: the chip is glass). */
    backdrop: Color? = null,
    /** Colour of an action capsule's icon. */
    accent: Color = COUITheme.colorScheme.primary,
) {
    val scheme = COUITheme.colorScheme
    val tint = dot?.copy(alpha = 0.16f)
    val label = if (dot != null || onClick != null) scheme.onSurface else color
    val shape = RoundedCornerShape(50)
    // A floating chip lies on its page's header material, which already blurs what scrolls beneath:
    // a second blur per chip only cost frames. It is a translucent card fill (plus the state tint).
    val surface = if (backdrop != null) {
        // The card colour is itself translucent in the dark theme (10 % white): composite it on the
        // page first, then make that translucent. Replacing its alpha turned the chips light grey.
        val card = scheme.surfaceContainer.compositeOver(pageContainerColor().compositeOver(scheme.background))
        Modifier.clip(shape).background(card.copy(alpha = FLOATING_CHIP_ALPHA)).then(if (tint != null) Modifier.background(tint) else Modifier)
    } else {
        Modifier.clip(shape).background(tint ?: scheme.surfaceContainer)
    }
    Row(
        modifier
            .heightIn(min = 30.dp)
            .then(surface)
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .padding(horizontal = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (dot != null) {
            StatusDot(dot, size = 7.dp)
            Spacer(Modifier.size(6.dp))
        }
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = if (onClick != null) accent else label, modifier = Modifier.size(15.dp))
            Spacer(Modifier.size(5.dp))
        }
        Text(text, style = COUITheme.textStyles.footnote1, fontWeight = FontWeight.Medium, color = label, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/** A quiet "icon + number" figure for a stats line (no outline, no fill). */
@Composable
fun StatFigure(icon: ImageVector, text: String, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = COUITheme.colorScheme.onSurfaceVariantSummary, modifier = Modifier.size(14.dp))
        Spacer(Modifier.size(4.dp))
        Text(text, style = COUITheme.textStyles.footnote1, color = COUITheme.colorScheme.onSurfaceVariantSummary, maxLines = 1)
    }
}

/** A right-aligned message bubble (the user's own turns), softly tinted with [color]. */
@Composable
fun ChatBubble(modifier: Modifier = Modifier, color: Color = COUITheme.colorScheme.primary, content: @Composable () -> Unit) {
    val dark = COUITheme.colorScheme.background.luminance() < 0.5f
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        Spacer(Modifier.size(40.dp))
        Box(
            Modifier
                .widthIn(max = 560.dp)
                .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 6.dp))
                .background(color.copy(alpha = if (dark) 0.30f else 0.12f))
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) { content() }
    }
}

/** A squircle card with an accent border while [active]: something that needs the user (a request, a question). */
@Composable
fun AttentionCard(modifier: Modifier = Modifier, color: Color = COUITheme.colorScheme.primary, active: Boolean = true, content: @Composable () -> Unit) {
    val shape = SquircleShape(20.dp)
    Column(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(COUITheme.colorScheme.surfaceContainer)
            .then(if (active) Modifier.border(1.5.dp, color.copy(alpha = 0.85f), shape) else Modifier)
            .padding(14.dp),
    ) { content() }
}

/** A plain squircle card for one item of a timeline or feed (smaller radius and padding than a group card). */
@Composable
fun TimelineCard(modifier: Modifier = Modifier, border: Color? = null, padding: Dp = 12.dp, content: @Composable () -> Unit) {
    val shape = SquircleShape(18.dp)
    Column(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(COUITheme.colorScheme.surfaceContainer)
            .then(if (border != null) Modifier.border(1.dp, border, shape) else Modifier)
            .padding(padding),
    ) { content() }
}

/**
 * The actions that end a step of a flow (onboarding, pairing), as the COUI17 button bar draws
 * them: two 44dp capsules side by side, the step's own action filled and on the right. A long or
 * destructive way out is a link under the filled button instead ([secondaryAsLink]).
 */
@Composable
fun StepButtons(
    primary: String,
    onPrimary: () -> Unit,
    modifier: Modifier = Modifier,
    primaryEnabled: Boolean = true,
    primaryTag: String? = null,
    secondary: String? = null,
    onSecondary: () -> Unit = {},
    secondaryAsLink: Boolean = false,
    linkColor: Color = COUITheme.colorScheme.error,
) {
    val tag = if (primaryTag != null) Modifier.testTag(primaryTag) else Modifier
    val colors = ButtonDefaults.buttonColorsPrimary()
    if (secondary == null || secondaryAsLink) {
        Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = onPrimary, enabled = primaryEnabled, modifier = Modifier.fillMaxWidth().then(tag), colors = colors) {
                Text(primary, color = if (primaryEnabled) colors.contentColor else colors.disabledContentColor)
            }
            if (secondary != null) {
                Spacer(Modifier.size(4.dp))
                LinkButton(secondary, onSecondary, color = linkColor)
            }
        }
        return
    }
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TextButton(text = secondary, onClick = onSecondary, modifier = Modifier.weight(1f))
        Button(onClick = onPrimary, enabled = primaryEnabled, modifier = Modifier.weight(1f).then(tag), colors = colors) {
            Text(primary, color = if (primaryEnabled) colors.contentColor else colors.disabledContentColor, maxLines = 1)
        }
    }
}

/** Opacity of a header chip over the page header material (see [InfoChip]). */
private const val FLOATING_CHIP_ALPHA = 0.72f
