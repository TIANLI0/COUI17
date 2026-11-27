// Copyright 2026, compose-coui-ui contributors
// SPDX-License-Identifier: Apache-2.0
package io.github.suqi8.coui.kmp.utils

import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView

private const val StrongTickVelocity = 2000f

@Composable
actual fun rememberCouiPickerHaptic(): (velocity: Float, level: CouiVibrateLevel) -> Unit {
    val view = LocalView.current
    return remember(view) {
        { velocity, level ->
            val strongTick = level == CouiVibrateLevel.Crisp && velocity >= StrongTickVelocity
            val effect = if (Build.VERSION.SDK_INT >= 34) {
                if (strongTick) HapticFeedbackConstants.SEGMENT_TICK else HapticFeedbackConstants.SEGMENT_FREQUENT_TICK
            } else {
                HapticFeedbackConstants.CLOCK_TICK
            }
            if (!view.performHapticFeedback(effect) && effect != HapticFeedbackConstants.CLOCK_TICK) {
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            }
        }
    }
}

@Composable
actual fun rememberCouiPickerClick(): () -> Unit = rememberCouiClickSound()
