// Copyright 2026, compose-coui-ui contributors
// SPDX-License-Identifier: Apache-2.0
package io.github.suqi8.coui.kmp.blur

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.platform.LocalContext

/** `COUIMaterialStateManager.MATERIAL_BLUR`, a `Settings.System` int. */
private const val MATERIAL_BLUR = "system_material_blur_enable"

/** `COUIMaterialStateManager.SUPER_POWER_SAVE_MODE`, a `Settings.System` int. */
private const val SUPER_POWER_SAVE_MODE = "super_powersave_mode_state"

/** `COUIMaterialStateManager.LOW_POWER_MODE`, a `Settings.Global` int. */
private const val LOW_POWER_MODE = "low_power"

/** `COUIMaterialStateManager.DISABLE_WINDOW_BLURS`, a `Settings.Global` int. */
private const val DISABLE_WINDOW_BLURS = "disable_window_blurs"

private val ObserverLock = Any()

@Volatile
private var platformBlurState: MutableState<Boolean>? = null

@Composable
@ReadOnlyComposable
internal actual fun isSystemBlurEnabled(): Boolean {
    SystemBlurState.overrideForTest?.let { return it }
    return obtainPlatformBlurState(LocalContext.current.applicationContext).value
}

internal actual fun CompositionLocalConsumerModifierNode.readSystemBlurEnabled(): Boolean {
    SystemBlurState.overrideForTest?.let { return it }
    return obtainPlatformBlurState(currentValueOf(LocalContext).applicationContext).value
}

private fun obtainPlatformBlurState(appContext: Context): MutableState<Boolean> {
    platformBlurState?.let { return it }
    return synchronized(ObserverLock) {
        platformBlurState ?: mutableStateOf(readBlurEnabled(appContext)).also { state ->
            platformBlurState = state
            installObserver(appContext, state)
        }
    }
}

private fun installObserver(appContext: Context, state: MutableState<Boolean>) {
    // The settings are process-wide, so the observer lives as long as the process does.
    val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            state.value = readBlurEnabled(appContext)
        }
    }
    val resolver = appContext.contentResolver
    for (uri in listOf(
        Settings.Global.getUriFor(LOW_POWER_MODE),
        Settings.Global.getUriFor(DISABLE_WINDOW_BLURS),
        Settings.System.getUriFor(MATERIAL_BLUR),
        Settings.System.getUriFor(SUPER_POWER_SAVE_MODE),
    )) {
        // An OEM setting may be unreadable to third-party apps. It must not prevent the
        // Android settings from being observed.
        runCatching { resolver.registerContentObserver(uri, false, observer) }
    }
}

private fun readBlurEnabled(context: Context): Boolean {
    val resolver = context.contentResolver
    val lowPower = runCatching { Settings.Global.getInt(resolver, LOW_POWER_MODE, 0) == 1 }.getOrDefault(false)
    val windowBlursDisabled = runCatching {
        Settings.Global.getInt(resolver, DISABLE_WINDOW_BLURS, 0) == 1
    }.getOrDefault(false)
    if (lowPower || windowBlursDisabled) return false

    val switch = runCatching { Settings.System.getString(resolver, MATERIAL_BLUR) }.getOrNull()
    val superPowerSave = runCatching {
        Settings.System.getInt(resolver, SUPER_POWER_SAVE_MODE, 0) == 1
    }.getOrDefault(false)
    return (switch == null || switch.trim() == "1") && !superPowerSave
}
