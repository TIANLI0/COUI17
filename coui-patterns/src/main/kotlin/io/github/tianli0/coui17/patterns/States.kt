// Copyright 2026, COUI17 contributors
// SPDX-License-Identifier: Apache-2.0

package io.github.tianli0.coui17.patterns

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.suqi8.coui.kmp.basic.BasicComponent
import io.github.suqi8.coui.kmp.basic.CardListPosition
import io.github.suqi8.coui.kmp.basic.EmptyState
import io.github.suqi8.coui.kmp.basic.InfiniteProgressIndicator
import io.github.suqi8.coui.kmp.basic.SmallTitle
import io.github.suqi8.coui.kmp.basic.TagView
import io.github.suqi8.coui.kmp.basic.TagViewDefaults
import io.github.suqi8.coui.kmp.basic.Text
import io.github.suqi8.coui.kmp.basic.TopTips
import io.github.suqi8.coui.kmp.theme.COUITheme

/** The heading of a group: coui `SmallTitle`, announced as a heading by screen readers. */
@Composable
fun SectionHeader(text: String, modifier: Modifier = Modifier) {
    SmallTitle(text = text, modifier = modifier.semantics { heading() })
}

/**
 * A small status label (LIVE, BLOCKED, 3 new): the COUI17 tag with [color] as its text and a soft
 * tint of it as its fill. The text is always present; state is never colour alone.
 */
@Composable
fun StatusPill(text: String, color: Color, modifier: Modifier = Modifier, contentDescription: String? = null) {
    TagView(
        text = text,
        modifier = modifier.then(if (contentDescription != null) Modifier.semantics { this.contentDescription = contentDescription } else Modifier),
        cornerRadius = 8.dp,
        colors = TagViewDefaults.tagViewColors(containerColor = color.copy(alpha = 0.14f), contentColor = color),
    )
}

/** A full-size loading state: the COUI spinner over [label], announced politely. */
@Composable
fun LoadingState(label: String, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxSize().padding(32.dp).semantics { liveRegion = LiveRegionMode.Polite; contentDescription = label },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        InfiniteProgressIndicator(size = 28.dp)
        Spacer(Modifier.height(12.dp))
        Text(label, color = COUITheme.colorScheme.onSurfaceVariantSummary, style = COUITheme.textStyles.body2)
    }
}

/** An error state: what happened, optional technical detail, and a retry action (a Small button). */
@Composable
fun ErrorState(
    title: String,
    modifier: Modifier = Modifier,
    message: String? = null,
    retryLabel: String? = null,
    onRetry: (() -> Unit)? = null,
) {
    Column(
        modifier.fillMaxWidth().padding(24.dp).semantics { liveRegion = LiveRegionMode.Polite },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, style = COUITheme.textStyles.title4, color = COUITheme.colorScheme.onSurface, textAlign = TextAlign.Center)
        if (!message.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                message,
                style = COUITheme.textStyles.body2,
                color = COUITheme.colorScheme.onSurfaceVariantSummary,
                textAlign = TextAlign.Center,
                maxLines = 6,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 480.dp),
            )
        }
        if (retryLabel != null && onRetry != null) {
            Spacer(Modifier.height(16.dp))
            SmallButton(retryLabel, onRetry)
        }
    }
}

/** An empty list or section: the COUI17 empty state, full width with the list's vertical rhythm. */
@Composable
fun EmptyMessage(title: String, modifier: Modifier = Modifier, subtitle: String? = null) {
    EmptyState(title = title, subtitle = subtitle, modifier = modifier.fillMaxWidth().padding(vertical = 24.dp))
}

/**
 * A notice above content (not a dialog): the COUI17 top tip with a status dot in [color], aligned
 * to the page's card gutter and announced politely when it appears.
 */
@Composable
fun NoticeBanner(text: String, color: Color, modifier: Modifier = Modifier, action: String? = null, onAction: (() -> Unit)? = null) {
    TopTips(
        text = text,
        modifier = modifier.fillMaxWidth().padding(horizontal = PageDefaults.CardGutter, vertical = 4.dp).semantics { liveRegion = LiveRegionMode.Polite },
        startIcon = { Box(Modifier.size(8.dp).clip(CircleShape).background(color)) },
        actionText = action,
        onAction = onAction,
    )
}

/**
 * A label / value row of a detail group: a COUI17 list row (title = label, summary = value). Pass
 * its [cardListPosition] like any other row of a group, so it gets the divider between rows.
 */
@Composable
fun KeyValueRow(label: String, value: String, modifier: Modifier = Modifier, cardListPosition: CardListPosition = CardListPosition.None) {
    BasicComponent(title = label, summary = value, modifier = modifier, cardListPosition = cardListPosition)
}

/**
 * The body under a collapsible row: it grows out of the row's bottom edge and fades in, and folds
 * back the same way. COUI's expand motion (`ExpandableRecyclerConnector`): 400ms on
 * (0.3, 0, 0.1, 1) both ways, alpha equal to the shown fraction of the height — size and fade run
 * on the same curve, so they stay in step with the row's 400ms `ExpandArrow` turn.
 */
@Composable
fun ExpandBody(expanded: Boolean, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    AnimatedVisibility(
        visible = expanded,
        modifier = modifier,
        enter = expandVertically(animationSpec = tween(EXPAND_MS, easing = ExpandEasing), expandFrom = Alignment.Top) +
            fadeIn(animationSpec = tween(EXPAND_MS, easing = ExpandEasing)),
        exit = shrinkVertically(animationSpec = tween(EXPAND_MS, easing = ExpandEasing), shrinkTowards = Alignment.Top) +
            fadeOut(animationSpec = tween(EXPAND_MS, easing = ExpandEasing)),
    ) {
        Column(Modifier.fillMaxWidth(), content = content)
    }
}

/**
 * COUI's expandable list takes 400ms; on a phone with many rows to open that read as slow, so the
 * same curve runs in 300ms here, and the row's arrow turns in the same time.
 */
internal const val EXPAND_MS = 300
private val ExpandEasing = androidx.compose.animation.core.CubicBezierEasing(0.3f, 0f, 0.1f, 1f)
