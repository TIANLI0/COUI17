// Copyright 2026, compose-coui-ui contributors
// SPDX-License-Identifier: Apache-2.0
package io.github.suqi8.coui.kmp.blur

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionOnScreen
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import io.github.suqi8.coui.kmp.blur.internal.InverseLayerScope

/**
 * A backdrop that replays a captured image of another window.
 *
 * A surface hosted in its own platform window (a `Dialog`) cannot sample the page it opens over:
 * that page is recorded in a different window, and a recorded layer cannot be read across windows.
 * The host captures the page instead and hands it over as this backdrop, so the surface runs the
 * same blur and tint pipeline an in-window surface does; the host re-captures whenever the page
 * draws again and passes the new image to [update].
 *
 * Positions are matched on screen, because the consumer and the captured window live in different
 * windows whose origins need not coincide.
 *
 * @property image The captured window, possibly downscaled.
 * @property sourceSize Size of the captured window in pixels; [image] is stretched back to it.
 * @property sourceOriginOnScreen Top-left corner of the captured window on screen, in pixels.
 * @param underlay Capture behind a translucent host window, in the same screen coordinate space.
 */
@InternalCouiBlurApi
@Stable
class ImageBackdrop(
    image: ImageBitmap,
    sourceSize: IntSize,
    sourceOriginOnScreen: Offset,
    private val underlay: ImageBackdrop?,
) : Backdrop {

    constructor(image: ImageBitmap, sourceSize: IntSize, sourceOriginOnScreen: Offset) :
        this(image, sourceSize, sourceOriginOnScreen, underlay = null)

    /** The current capture. Read while drawing, so replacing it redraws the consumers only. */
    var image: ImageBitmap by mutableStateOf(image)
        private set
    var sourceSize: IntSize by mutableStateOf(sourceSize)
        private set
    var sourceOriginOnScreen: Offset by mutableStateOf(sourceOriginOnScreen)
        private set

    /**
     * Replaces the capture with a newer one of the same window (the host keeps sampling while the
     * surface is open, so the frost follows what changes underneath: streaming text, a list that
     * scrolls on its own). Consumers redraw; nothing recomposes.
     */
    fun update(image: ImageBitmap, sourceSize: IntSize, sourceOriginOnScreen: Offset) {
        this.image = image
        this.sourceSize = sourceSize
        this.sourceOriginOnScreen = sourceOriginOnScreen
    }

    override val isCoordinatesDependent: Boolean = true

    override var offsetResidualX: Float = 0f
        private set
    override var offsetResidualY: Float = 0f
        private set

    private var inverseLayerScope: InverseLayerScope? = null

    override fun DrawScope.drawBackdrop(
        density: Density,
        coordinates: LayoutCoordinates?,
        layerBlock: (GraphicsLayerScope.() -> Unit)?,
        downscaleFactor: Int,
    ) {
        val coordinates = coordinates ?: return
        if (!coordinates.isAttached) return

        val screenPosition = runCatching { coordinates.positionOnScreen() }.getOrDefault(Offset.Unspecified)
        if (!screenPosition.isSpecified) return
        val captureOrigin = sourceOriginOnScreen
        val offset = screenPosition - captureOrigin

        val consumerSize = (density as? BackdropEffectScope)?.size ?: size

        withTransform({
            if (layerBlock != null) {
                with(obtainInverseLayerScope()) { inverseTransform(density, consumerSize, layerBlock) }
            }
            if (downscaleFactor > 1) {
                val inv = 1f / downscaleFactor
                val scaledX = offset.x * inv
                val scaledY = offset.y * inv
                val roundedX = kotlin.math.round(scaledX * 0.5f).toInt().toFloat() * 2f
                val roundedY = kotlin.math.round(scaledY * 0.5f).toInt().toFloat() * 2f
                offsetResidualX = (scaledX - roundedX) * downscaleFactor
                offsetResidualY = (scaledY - roundedY) * downscaleFactor
                translate(-roundedX, -roundedY)
                scale(inv, inv, Offset.Zero)
            } else {
                offsetResidualX = 0f
                offsetResidualY = 0f
                translate(-offset.x, -offset.y)
            }
        }) {
            drawCapture(captureOrigin)
        }
    }

    private fun DrawScope.drawCapture(relativeTo: Offset) {
        underlay?.let { lower -> with(lower) { drawCapture(relativeTo) } }
        val offset = sourceOriginOnScreen - relativeTo
        translate(offset.x, offset.y) {
            drawImage(
                image = image,
                srcOffset = IntOffset.Zero,
                srcSize = IntSize(image.width, image.height),
                dstOffset = IntOffset.Zero,
                dstSize = sourceSize,
                filterQuality = FilterQuality.Medium,
            )
        }
    }

    private fun obtainInverseLayerScope(): InverseLayerScope = inverseLayerScope?.apply { reset() }
        ?: InverseLayerScope().also { inverseLayerScope = it }
}
