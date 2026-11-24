// Copyright 2026, COUI17 contributors
// SPDX-License-Identifier: Apache-2.0

package io.github.tianli0.coui17.patterns

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import io.github.suqi8.coui.kmp.basic.BottomFloatingToolbarDefaults
import io.github.suqi8.coui.kmp.basic.couiGlassSurface
import io.github.suqi8.coui.kmp.blur.Backdrop
import io.github.suqi8.coui.kmp.blur.BlendColorEntry
import io.github.suqi8.coui.kmp.blur.BlurDefaults
import io.github.suqi8.coui.kmp.blur.ProgressiveBlur
import io.github.suqi8.coui.kmp.blur.isBlurEnabled
import io.github.suqi8.coui.kmp.blur.progressiveTextureBlur
import io.github.suqi8.coui.kmp.theme.COUITheme

/**
 * The layer a floating control blurs: the content it floats over, recorded by its page as a coui
 * `LayerBackdrop` (`rememberLayerBackdrop` + `Modifier.layerBackdrop`). The floating control must
 * be a sibling of the recorded layer, never inside it. Null (nothing recorded, blur unsupported,
 * tests) means opaque fills.
 */
val LocalGlassBackdrop = staticCompositionLocalOf<Backdrop?> { null }

/**
 * The surface of a control floating over scrolling content (a composer, a capsule, a floating
 * key): ColorOS 17's floating-toolbar glass.
 *
 * The tint layers are coui's `BottomFloatingToolbarDefaults` recipe, not a translucent card
 * colour: its `Luminosity` layer pulls whatever is behind to the surface's own brightness, so the
 * glass stays dark over white text in the dark theme and light over dark text in the light one (a
 * plain alpha tint lets bright content through and reads as a light-grey slab in dark mode).
 * [tint] adds a state's colour on top. A hairline rim, clipped to [shape]. Without a
 * [LocalGlassBackdrop] it is the opaque card colour.
 */
@Composable
fun Modifier.glassSurface(shape: Shape, tint: Color? = null): Modifier {
    val scheme = COUITheme.colorScheme
    val dark = scheme.background.luminance() < 0.5f
    val fill = tint?.compositeOver(scheme.surfaceContainer) ?: scheme.surfaceContainer
    val rim = if (dark) Color.White.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.06f)
    val layers = remember(dark, tint) {
        val base = if (dark) BottomFloatingToolbarDefaults.BlurColorsDark else BottomFloatingToolbarDefaults.BlurColorsLight
        if (tint != null) base + BlendColorEntry(color = tint) else base
    }
    return this
        .couiGlassSurface(
            backdrop = LocalGlassBackdrop.current,
            shape = shape,
            containerColor = fill,
            fallback = Modifier.clip(shape).background(fill),
            blurRadius = 24f,
            blendColors = layers,
        )
        .border(0.5.dp, rim, shape)
        .clip(shape)
}

/**
 * The material behind a title bar when dense text scrolls beneath it: a progressive blur that
 * holds full strength down to [holdFraction] of the node's height (the bar's bottom edge) and
 * tapers to nothing at the node's bottom, under [containerColor] at the strength coui's plain bar
 * uses (80 % light, 60 % dark), fading out over the same stretch.
 *
 * coui's own gradient bar material is much thinner: right over list cards, but running text stays
 * sharp under the title. Without blur this is the opaque [containerColor] down to [holdFraction],
 * then a fade.
 */
@Composable
fun Modifier.glassHeader(containerColor: Color, holdFraction: Float): Modifier {
    val hold = holdFraction.coerceIn(0.05f, 0.9f)
    val backdrop = LocalGlassBackdrop.current
    if (backdrop == null || !isBlurEnabled()) {
        return background(Brush.verticalGradient(0f to containerColor, hold to containerColor, 1f to containerColor.copy(alpha = 0f)))
    }
    val tint = if (containerColor.luminance() < 0.5f) 0.6f else 0.8f
    val tail = 1f - hold
    return progressiveTextureBlur(
        backdrop = backdrop,
        shape = RectangleShape,
        blurRadius = 26f,
        gradient = ProgressiveBlur.Top.copy(startFraction = hold, endFraction = 1f, curve = 1.4f),
        colors = BlurDefaults.blurColors(),
    ).background(
        Brush.verticalGradient(
            0f to containerColor.copy(alpha = (tint + 0.25f).coerceAtMost(1f)),
            hold to containerColor.copy(alpha = tint),
            hold + tail * 0.5f to containerColor.copy(alpha = tint * 0.6f),
            1f to containerColor.copy(alpha = 0f),
        ),
    )
}
