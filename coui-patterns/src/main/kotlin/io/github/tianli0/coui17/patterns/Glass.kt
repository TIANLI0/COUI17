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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
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
 * The blur radius and tint layers come from COUI17's `BottomFloatingToolbarDefaults` recipe, not a translucent card
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
            blurRadius = BottomFloatingToolbarDefaults.blurRadiusDp(),
            blendColors = layers,
        )
        .clip(shape)
}

private fun glassLayers(dark: Boolean, tint: Color?): List<BlendColorEntry> {
    val base = if (dark) BottomFloatingToolbarDefaults.BlurColorsDark else BottomFloatingToolbarDefaults.BlurColorsLight
    return if (tint != null) base + BlendColorEntry(color = tint) else base
}

/** The outlines a [GlassGroup] blurs for its members, in the group's coordinates. */
@Stable
class GlassGroupState internal constructor() {
    internal val parts = mutableStateMapOf<Any, Outline>()

    /** What each member's outline was built from: a member that did not move is not rebuilt. */
    private val placed = HashMap<Any, MemberGeometry>()
    internal var origin: LayoutCoordinates? = null

    /**
     * The union of the members' outlines. The group's blur asks for its shape on every draw (every
     * frame while the page under it streams); this is one instance until a member's outline
     * changes, so the blur's outline cache holds instead of rebuilding a path per draw.
     */
    internal val union: Shape by derivedStateOf { UnionShape(parts.values.toList()) }

    /**
     * Records the outline of the member [key]: [shape] at [size], placed at [offset] in the group.
     * Nothing is written when the geometry is unchanged: positioning callbacks run on every layout
     * pass (an IME animation moves the whole row each frame), and an outline built from a path has
     * no value equality, so comparing outlines would rewrite the map, and redraw, every time.
     */
    internal fun place(key: Any, shape: Shape, offset: Offset, size: Size, layoutDirection: LayoutDirection, density: Density) {
        val geometry = MemberGeometry(shape, offset, size, layoutDirection, density.density, density.fontScale)
        if (placed[key] == geometry) return
        placed[key] = geometry
        parts[key] = shape.createOutline(size, layoutDirection, density).translated(offset)
    }

    internal fun remove(key: Any) {
        placed.remove(key)
        parts.remove(key)
    }

    /** Registers the outline of the node it is put on; removed when the node leaves. */
    @Composable
    internal fun member(shape: Shape): Modifier {
        val key = remember { Any() }
        DisposableEffect(key) { onDispose { remove(key) } }
        val layoutDirection = LocalLayoutDirection.current
        val density = LocalDensity.current
        return Modifier.onGloballyPositioned { coords ->
            val group = origin ?: return@onGloballyPositioned
            if (!coords.isAttached || !group.isAttached) return@onGloballyPositioned
            val offset = group.localPositionOf(coords, Offset.Zero)
            place(key, shape, offset, Size(coords.size.width.toFloat(), coords.size.height.toFloat()), layoutDirection, density)
        }
    }
}

private data class MemberGeometry(
    val shape: Shape,
    val offset: Offset,
    val size: Size,
    val layoutDirection: LayoutDirection,
    val density: Float,
    val fontScale: Float,
)

internal fun Outline.translated(offset: Offset): Outline = when (this) {
    is Outline.Rectangle -> Outline.Rectangle(rect.translate(offset))
    is Outline.Rounded -> Outline.Rounded(roundRect.translate(offset))
    is Outline.Generic -> Outline.Generic(Path().apply { addPath(path, offset) })
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
    Box(modifier.onGloballyPositioned { state.origin = it }) {
        // The blur is its own layer behind the content: drawn on the group itself it clipped
        // everything inside to the members' outlines, and anything else there (a picked photo's
        // thumbnail, a note) disappeared.
        Box(
            Modifier
                .matchParentSize()
                .couiGlassSurface(
                    backdrop = LocalGlassBackdrop.current,
                    shape = { state.union },
                    containerColor = fill,
                    fallback = Modifier,
                    blurRadius = BottomFloatingToolbarDefaults.blurRadiusDp(),
                    blendColors = layers,
                ),
        )
        CompositionLocalProvider(LocalGlassGroup provides state) { content() }
    }
}

/** The union of a group's member outlines (already in the group's coordinates). */
internal data class UnionShape(val outlines: List<Outline>) : Shape {
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
        val fill = remember(containerColor, hold) {
            Brush.verticalGradient(0f to containerColor, hold to containerColor, 1f to containerColor.copy(alpha = 0f))
        }
        return background(fill)
    }
    val material = remember(backdrop, containerColor, hold) {
        val tint = if (containerColor.luminance() < 0.5f) 0.6f else 0.8f
        val tail = 1f - hold
        Modifier.progressiveTextureBlur(
            backdrop = backdrop,
            shape = RectangleShape,
            blurRadius = 26f,
            gradient = ProgressiveBlur.Top.copy(startFraction = hold, endFraction = 1f, curve = 1.4f),
        ).background(
            Brush.verticalGradient(
                0f to containerColor.copy(alpha = (tint + 0.25f).coerceAtMost(1f)),
                hold to containerColor.copy(alpha = tint),
                hold + tail * 0.5f to containerColor.copy(alpha = tint * 0.6f),
                1f to containerColor.copy(alpha = 0f),
            ),
        )
    }
    return then(material)
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
