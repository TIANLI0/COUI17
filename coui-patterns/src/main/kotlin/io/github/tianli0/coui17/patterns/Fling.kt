// Copyright 2026, COUI17 contributors
// SPDX-License-Identifier: Apache-2.0

package io.github.tianli0.coui17.patterns

import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.FloatDecayAnimationSpec
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.core.generateDecayAnimationSpec
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.sign

/**
 * The fling of a ColorOS 17 list: `COUISpringOverScroller` replaces the platform's spline fling
 * with a friction-only one, v(t) = v0 · e^(−f·t), where f depends on the current speed in raw
 * pixels per second: 1.6/s at or above 10,000 px/s, 2.5/s below.
 *
 * Measured on a ColorOS 17.0.1 phone (Settings home, frame-by-frame profile correlation of a
 * screen recording): a ~7,000 px/s fling decays at 2.4–2.7/s and glides for well over two
 * seconds, much further than Compose's default spline decay. The thresholds stay in pixels, as in
 * the native scroller.
 */
internal class CouiFlingDecaySpec(
    private val fastFriction: Float = 1.6f,
    private val slowFriction: Float = 2.5f,
    private val fastThreshold: Float = 10_000f,
    private val stopVelocity: Float = 30f,
) : FloatDecayAnimationSpec {

    override val absVelocityThreshold: Float get() = stopVelocity

    /** Seconds spent above [fastThreshold], 0 when the fling starts below it. */
    private fun fastPhase(v0: Float): Float {
        val s = abs(v0)
        return if (s > fastThreshold) ln(s / fastThreshold) / fastFriction else 0f
    }

    private fun velocityAt(t: Float, v0: Float): Float {
        val t1 = fastPhase(v0)
        return if (t <= t1) {
            v0 * exp(-fastFriction * t)
        } else {
            val vStart = if (t1 > 0f) sign(v0) * fastThreshold else v0
            vStart * exp(-slowFriction * (t - t1))
        }
    }

    private fun distanceAt(t: Float, v0: Float): Float {
        val t1 = fastPhase(v0)
        if (t <= t1) return v0 / fastFriction * (1f - exp(-fastFriction * t))
        val d1 = if (t1 > 0f) v0 / fastFriction * (1f - exp(-fastFriction * t1)) else 0f
        val vStart = if (t1 > 0f) sign(v0) * fastThreshold else v0
        return d1 + vStart / slowFriction * (1f - exp(-slowFriction * (t - t1)))
    }

    private fun durationSeconds(v0: Float): Float {
        val s = abs(v0)
        if (s <= stopVelocity) return 0f
        val t1 = fastPhase(v0)
        val slowStart = if (t1 > 0f) fastThreshold else s
        return t1 + ln(slowStart / stopVelocity) / slowFriction
    }

    override fun getValueFromNanos(playTimeNanos: Long, initialValue: Float, initialVelocity: Float): Float {
        val t = (playTimeNanos / 1e9f).coerceAtMost(durationSeconds(initialVelocity))
        return initialValue + distanceAt(t, initialVelocity)
    }

    override fun getVelocityFromNanos(playTimeNanos: Long, initialValue: Float, initialVelocity: Float): Float {
        val t = playTimeNanos / 1e9f
        return if (t >= durationSeconds(initialVelocity)) 0f else velocityAt(t, initialVelocity)
    }

    override fun getDurationNanos(initialValue: Float, initialVelocity: Float): Long =
        (durationSeconds(initialVelocity) * 1e9f).toLong()

    override fun getTargetValue(initialValue: Float, initialVelocity: Float): Float =
        initialValue + distanceAt(durationSeconds(initialVelocity), initialVelocity)
}

private class CouiFlingBehavior : FlingBehavior {
    private val decay = CouiFlingDecaySpec().generateDecayAnimationSpec<Float>()

    override suspend fun ScrollScope.performFling(initialVelocity: Float): Float {
        if (abs(initialVelocity) <= 1f) return initialVelocity
        var velocityLeft = initialVelocity
        var lastValue = 0f
        AnimationState(initialValue = 0f, initialVelocity = initialVelocity).animateDecay(decay) {
            val delta = value - lastValue
            val consumed = scrollBy(delta)
            lastValue = value
            velocityLeft = velocity
            // The list hit an edge: hand the rest to the overscroll effect.
            if (abs(delta - consumed) > 0.5f) cancelAnimation()
        }
        return velocityLeft
    }
}

/**
 * The ColorOS 17 list fling (see [CouiFlingDecaySpec]). Pass it as `flingBehavior` to the page's
 * `LazyColumn` / `verticalScroll` next to [rememberPageListState] and [Modifier.pageScroll], so a
 * flung list glides like the system's.
 */
@Composable
fun pageFlingBehavior(): FlingBehavior = remember { CouiFlingBehavior() }
