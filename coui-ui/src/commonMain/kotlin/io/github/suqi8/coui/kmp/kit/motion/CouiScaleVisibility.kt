// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
// SPDX-License-Identifier: MPL-2.0
// Ported from COUI-Kit (https://github.com/ItosEO/COUI-Kit) motion/CouiScaleVisibility.kt.
package io.github.suqi8.coui.kmp.kit.motion

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import io.github.suqi8.coui.kmp.blur.isBlurEnabled

/**
 * The ColorOS show / hide of a floating control in place (the floating navigation bar, a floating
 * toolbar): it never slides, it scales between 0.8 and 1 on a response 0.35 / bounce 0.35 spring,
 * fades on 0.2 / 0 and, while the system's material blur is on, blurs its foreground by up to 30px
 * on 0.3 / 0 (`COUIBottomFloatingNavigationBarMenuView.doMenuViewShowOrHideAnim`).
 *
 * Apply it with [couiScaleVisibility], or read [CouiScaleVisibilityState.scale],
 * [CouiScaleVisibilityState.alpha] and [CouiScaleVisibilityState.foregroundBlurEffect] in a custom
 * draw chain. The host decides when [visible] flips and stops input while hidden; keep the control
 * composed while [CouiScaleVisibilityState.isPresent] so the exit can finish.
 */
@Composable
fun rememberCouiScaleVisibility(visible: Boolean): CouiScaleVisibilityState {
    val blurEnabled = isBlurEnabled()
    val state = remember { CouiScaleVisibilityState(visible, blurEnabled) }
    DisposableEffect(state) {
        onDispose { state.dispose() }
    }
    SideEffect { state.updateTarget(visible, blurEnabled) }
    return state
}

/** Centred scale, alpha and the foreground blur of [state] on this layer. */
fun Modifier.couiScaleVisibility(state: CouiScaleVisibilityState): Modifier = graphicsLayer {
    val scale = state.scale.value
    scaleX = scale
    scaleY = scale
    alpha = state.alpha.value
    renderEffect = state.foregroundBlurEffect
}

/** Scale, alpha, foreground blur and exit presence of one floating control. */
@Stable
class CouiScaleVisibilityState internal constructor(initialVisible: Boolean, initialBlurEnabled: Boolean) {
    private val scaleSpring = CouiSpring(if (initialVisible) 1f else HIDDEN_SCALE, MINIMUM_VISIBLE_CHANGE)
    private val alphaSpring = CouiSpring(if (initialVisible) 1f else 0f, MINIMUM_VISIBLE_CHANGE)
    private val blurSpring = CouiSpring(
        if (!initialVisible && initialBlurEnabled) HIDDEN_BLUR_RADIUS_PX else 0f,
        MINIMUM_VISIBLE_CHANGE,
    )
    private var targetVisible = initialVisible
    private var blurEnabled = initialBlurEnabled

    val scale: State<Float> get() = scaleSpring.state
    val alpha: State<Float> get() = alphaSpring.state

    /** The foreground blur to apply to the control's layer; null while it is sharp. */
    val foregroundBlurEffect: RenderEffect? by derivedStateOf {
        val radius = blurSpring.state.value
        if (radius > 0f) BlurEffect(radius, radius, TileMode.Clamp) else null
    }

    /** True from the moment the control shows until every exit spring has settled. */
    var isPresent by mutableStateOf(initialVisible)
        private set

    internal fun updateTarget(visible: Boolean, systemBlurEnabled: Boolean) {
        if (visible == targetVisible && systemBlurEnabled == blurEnabled) return
        if (visible != targetVisible) {
            targetVisible = visible
            isPresent = true
            alphaSpring.animateTo(if (visible) 1f else 0f, response = ALPHA_RESPONSE, onEnd = ::finishIfHidden)
            scaleSpring.animateTo(
                if (visible) 1f else HIDDEN_SCALE,
                response = SCALE_RESPONSE,
                bounce = SCALE_BOUNCE,
                onEnd = ::finishIfHidden,
            )
        }
        blurEnabled = systemBlurEnabled
        val targetRadius = if (blurEnabled && !visible) HIDDEN_BLUR_RADIUS_PX else 0f
        if (blurEnabled && isPresent && (blurSpring.isRunning || blurSpring.value != targetRadius)) {
            blurSpring.animateTo(targetRadius, response = BLUR_RESPONSE, onEnd = ::finishIfHidden)
        } else {
            blurSpring.snapTo(targetRadius)
            finishIfHidden()
        }
    }

    internal fun dispose() {
        scaleSpring.cancel()
        alphaSpring.cancel()
        blurSpring.cancel()
    }

    private fun finishIfHidden() {
        if (!targetVisible && !alphaSpring.isRunning && !scaleSpring.isRunning && !blurSpring.isRunning) {
            isPresent = false
        }
    }

    private companion object {
        const val HIDDEN_SCALE = 0.8f
        const val SCALE_RESPONSE = 0.35f
        const val SCALE_BOUNCE = 0.35f
        const val ALPHA_RESPONSE = 0.2f
        const val BLUR_RESPONSE = 0.3f
        const val HIDDEN_BLUR_RADIUS_PX = 30f

        // COUI feeds FloatValueHolder values scaled by 10000; this is that threshold in real units.
        const val MINIMUM_VISIBLE_CHANGE = 1f / 10000f
    }
}
