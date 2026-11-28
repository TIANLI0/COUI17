// Copyright 2026, COUI17 contributors
// SPDX-License-Identifier: Apache-2.0

package io.github.tianli0.coui17.patterns

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.suqi8.coui.kmp.basic.Text
import io.github.suqi8.coui.kmp.theme.COUITheme
import kotlin.math.abs
import kotlin.math.min

/**
 * ColorOS 17's underlined tab bar (`COUITabLayout`): text tabs over a 2dp track, the active one
 * marked by a 2dp bar as wide as its label. Use it to switch between the different views of one
 * page (a repository's changes / history / branches). To narrow one list (live / history / all),
 * use the capsule segmented control (`TabRowWithContour`) instead.
 *
 * Metrics from Files 17.10.4: 40dp tall, 16dp labels (medium when selected, regular otherwise,
 * `couiColorLabelPrimary` / `couiColorLabelSecondary`), the track `#0F000000` (night `#19FFFFFF`)
 * inset 24dp from both sides, the bar in `couiColorPrimaryNeutral`. The bar and the label colours
 * move together over `min(300, 50 × |Δ| + 150)` ms on (0.33, 0, 0.67, 1).
 */
@Composable
fun TabBar(
    tabs: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val count = tabs.size.coerceAtLeast(1)
    val selected = selectedIndex.coerceIn(0, count - 1)
    val position = remember { Animatable(selected.toFloat()) }
    LaunchedEffect(selected) {
        val distance = abs(position.value - selected)
        val duration = min(300f, distance * 50f + 150f).toInt()
        position.animateTo(selected.toFloat(), tween(duration, easing = TabEasing))
    }
    val labelWidths = remember(tabs) { mutableStateListOf<Float>().apply { repeat(tabs.size) { add(0f) } } }
    val dark = COUITheme.colorScheme.background.luminance() < 0.5f
    val trackColor = if (dark) Color(0x19FFFFFF) else Color(0x0F000000)
    val barColor = COUITheme.colorScheme.onSurface
    val density = LocalDensity.current
    BoxWithConstraints(modifier.fillMaxWidth().height(TabBarHeight)) {
        val insetPx = with(density) { TrackInset.toPx() }
        val cellPx = (constraints.maxWidth - insetPx * 2) / count
        Row(Modifier.fillMaxSize().selectableGroup()) {
            Box(Modifier.fillMaxHeight().width(TrackInset))
            tabs.forEachIndexed { index, label ->
                val active = index == selected
                val color by animateColorAsState(
                    if (active) COUITheme.colorScheme.onSurface else COUITheme.colorScheme.onSurfaceVariantSummary,
                    tween(300, easing = TabEasing),
                    label = "tab label",
                )
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .selectable(
                            selected = active,
                            onClick = { onSelect(index) },
                            role = Role.Tab,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        label,
                        style = COUITheme.textStyles.headline1.copy(fontWeight = if (active) FontWeight.Medium else FontWeight.Normal),
                        color = color,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        onTextLayout = { if (index < labelWidths.size) labelWidths[index] = it.size.width.toFloat() },
                    )
                }
            }
            Box(Modifier.fillMaxHeight().width(TrackInset))
        }
        Canvas(Modifier.fillMaxSize()) {
            val h = 2.dp.toPx()
            val y = size.height - h
            drawRect(trackColor, Offset(insetPx, y), Size(size.width - insetPx * 2, h))
            val p = position.value.coerceIn(0f, (count - 1).toFloat())
            val lo = p.toInt()
            val hi = (lo + 1).coerceAtMost(count - 1)
            val f = p - lo
            val width = labelWidths.getOrElse(lo) { 0f } * (1 - f) + labelWidths.getOrElse(hi) { 0f } * f
            val center = insetPx + cellPx * (p + 0.5f)
            if (width > 0f) drawRect(barColor, Offset(center - width / 2, y), Size(width, h))
        }
    }
}

private val TabBarHeight = 40.dp
private val TrackInset = 24.dp
private val TabEasing = CubicBezierEasing(0.33f, 0f, 0.67f, 1f)
