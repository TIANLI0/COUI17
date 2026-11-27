// Copyright 2026, compose-coui-ui contributors
// SPDX-License-Identifier: Apache-2.0
package io.github.suqi8.coui.kmp.utils

import androidx.compose.runtime.Composable

/**
 * The strength of a COUI wheel-picker tick.
 *
 * Mirrors the `couiVibrateLevel` attribute of `COUINumberPicker`: [Crisp] is the default and
 * [Soft] is the gentler waveform.
 */
enum class CouiVibrateLevel {
    /** Crisp, higher-amplitude tick (`VIBRATE_LEVEL_CRISP`, 0). */
    Crisp,

    /** Soft, lower-amplitude tick (`VIBRATE_LEVEL_SOFT`, 1). */
    Soft,
}

/**
 * A wheel-picker tick whose strength follows the scroll velocity.
 *
 * Android uses the platform's segment feedback where available and a clock tick on older versions.
 *
 * @param velocity The absolute scroll velocity in pixels per second.
 * @param level The configured vibrate level.
 */
@Composable
expect fun rememberCouiPickerHaptic(): (velocity: Float, level: CouiVibrateLevel) -> Unit

/**
 * The mechanical click a COUI wheel picker plays when it steps onto a new item.
 *
 * Android uses the system click sound and follows the user's sound-effect setting.
 */
@Composable
expect fun rememberCouiPickerClick(): () -> Unit
