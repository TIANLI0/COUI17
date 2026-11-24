// Copyright 2026, COUI17 contributors
// SPDX-License-Identifier: Apache-2.0

package io.github.tianli0.coui17.patterns

import androidx.compose.foundation.background
import androidx.compose.ui.unit.Density
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.geometry.translate
import androidx.compose.ui.geometry.Offset
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.Stable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.border
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.LayoutDirection
import io.github.suqi8.coui.kmp.basic.MaterialStrokeParams
import io.github.suqi8.coui.kmp.basic.materialShadowEdge
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
 * key): frosted glass, blur and tint only — no rim, inner fade or shadow (over running text they
 * read as borders and halos).
 *
 * The tint layers are coui's `BottomFloatingToolbarDefaults` recipe, not a translucent card
 * colour: its `Luminosity` layer pulls whatever is behind to the surface's own brightness, so the
 * glass stays dark over white text in the dark theme and light over dark text in the light one.
 * [tint] adds a state's colour on top. Without a [LocalGlassBackdrop] it is the opaque card colour.
 *
 * Inside a [GlassGroup] the control does not blur by itself: it hands its outline to the group,
 * which blurs once for all of its controls. [halo] and [circle] are kept for source compatibility.
 */
@Composable
fun Modifier.glassSurface(
    shape: Shape,
    tint: Color? = null,
    @Suppress("UNUSED_PARAMETER") halo: Boolean = false,
    @Suppress("UNUSED_PARAMETER") circle: Boolean = shape == CircleShape,
): Modifier {
    val scheme = COUITheme.colorScheme
    val dark = scheme.background.luminance() < 0.5f
    val fill = tint?.compositeOver(scheme.surfaceContainer) ?: scheme.surfaceContainer
    val backdrop = LocalGlassBackdrop.current
    val group = LocalGlassGroup.current
    if (group != null && tint == null && backdrop != null && isBlurEnabled()) {
        return this.then(group.member(shape)).clip(shape)
    }
    val layers = remember(dark, tint) { glassLayers(dark, tint) }
    return this
        .couiGlassSurface(
            backdrop = backdrop,
            shape = shape,
            containerColor = fill,
            fallback = Modifier.clip(shape).background(fill),
            blurRadius = GLASS_BLUR_RADIUS,
            blendColors = layers,
        )
        .clip(shape)
}

private const val GLASS_BLUR_RADIUS = 24f

private fun glassLayers(dark: Boolean, tint: Color?): List<BlendColorEntry> {
    val base = if (dark) BottomFloatingToolbarDefaults.BlurColorsDark else BottomFloatingToolbarDefaults.BlurColorsLight
    return if (tint != null) base + BlendColorEntry(color = tint) else base
}

/** The outlines a [GlassGroup] blurs for its members, in the group's coordinates. */
@Stable
class GlassGroupState internal constructor() {
    internal val parts = mutableStateMapOf<Any, Outline>()
    internal var origin: LayoutCoordinates? = null

    /** Registers the outline of the node it is put on; removed when the node leaves. */
    @Composable
    internal fun member(shape: Shape): Modifier {
        val key = remember { Any() }
        DisposableEffect(key) { onDispose { parts.remove(key) } }
        val layoutDirection = LocalLayoutDirection.current
        val density = LocalDensity.current
        return Modifier.onGloballyPositioned { coords ->
            val group = origin ?: return@onGloballyPositioned
            if (!coords.isAttached || !group.isAttached) return@onGloballyPositioned
            val offset = group.localPositionOf(coords, Offset.Zero)
            val size = Size(coords.size.width.toFloat(), coords.size.height.toFloat())
            val outline = when (val o = shape.createOutline(size, layoutDirection, density)) {
                is Outline.Rectangle -> Outline.Rectangle(o.rect.translate(offset))
                is Outline.Rounded -> Outline.Rounded(o.roundRect.translate(offset))
                is Outline.Generic -> Outline.Generic(Path().apply { addPath(o.path, offset) })
            }
            if (parts[key] != outline) parts[key] = outline
        }
    }
}

val LocalGlassGroup = staticCompositionLocalOf<GlassGroupState?> { null }

/**
 * Blurs once for every [glassSurface] inside it: the group samples [LocalGlassBackdrop] a single
 * time, clipped to the union of its members' outlines, instead of each control running its own blur
 * over the same conversation (a composer row is a field, keys and capsules — five or six blurs per
 * frame while a session streams, which dropped frames and warmed the phone). Looks the same as
 * separate glass. A tinted member still blurs by itself.
 */
@Composable
fun GlassGroup(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val state = remember { GlassGroupState() }
    val dark = COUITheme.colorScheme.background.luminance() < 0.5f
    val layers = remember(dark) { glassLayers(dark, null) }
    val fill = COUITheme.colorScheme.surfaceContainer
    Box(
        modifier
            .onGloballyPositioned { state.origin = it }
            .couiGlassSurface(
                backdrop = LocalGlassBackdrop.current,
                shape = { UnionShape(state.parts.values.toList()) },
                containerColor = fill,
                fallback = Modifier,
                blurRadius = GLASS_BLUR_RADIUS,
                blendColors = layers,
            ),
    ) {
        CompositionLocalProvider(LocalGlassGroup provides state) { content() }
    }
}

/** The union of a group's member outlines (already in the group's coordinates). */
private data class UnionShape(val outlines: List<Outline>) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path()
        outlines.forEach { path.addOutline(it) }
        return Outline.Generic(path)
    }
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

/**
 * The one shadow of a floating control: the soft halo of ColorOS 17's toolbar keys (the title
 * bar's back key, the panel's close capsule) — 16dp of blur, 6dp down, 12 % black on a light page
 * and a faint 5 % white glow on a dark one. Use it for anything that floats over content instead
 * of an elevation shadow, so every floating control casts the same light. Apply it before the
 * modifier that clips the control.
 */
@Composable
fun Modifier.floatingHalo(shape: Shape): Modifier {
    val dark = COUITheme.colorScheme.background.luminance() < 0.5f
    val shadow = remember(dark) {
        Shadow(
            radius = 16.dp,
            color = if (dark) Color.White else Color.Black,
            offset = DpOffset(0.dp, 6.dp),
            alpha = if (dark) 0.05f else 0.12f,
        )
    }
    return dropShadow(shape, shadow)
}
