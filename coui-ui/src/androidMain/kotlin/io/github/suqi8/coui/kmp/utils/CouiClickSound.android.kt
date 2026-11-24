// Copyright 2026, compose-coui-ui contributors
// SPDX-License-Identifier: Apache-2.0
package io.github.suqi8.coui.kmp.utils

import android.view.SoundEffectConstants
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView

@Composable
actual fun rememberCouiClickSound(): () -> Unit {
    val view = LocalView.current
    // View.playSoundEffect honours Settings.System.SOUND_EFFECTS_ENABLED by itself.
    return remember(view) { { view.playSoundEffect(SoundEffectConstants.CLICK) } }
}
