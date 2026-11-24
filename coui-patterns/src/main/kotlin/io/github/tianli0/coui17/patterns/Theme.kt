// Copyright 2026, COUI17 contributors
// SPDX-License-Identifier: Apache-2.0

package io.github.tianli0.coui17.patterns

import android.app.Activity
import android.content.ContextWrapper
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.core.view.WindowCompat
import io.github.suqi8.coui.kmp.basic.LocalPlatformAppBarMaterialEnabled
import io.github.suqi8.coui.kmp.basic.LocalPlatformBottomBarMaterialEnabled
import io.github.suqi8.coui.kmp.basic.LocalTopAppBarTitleMarquee
import io.github.suqi8.coui.kmp.layout.LocalSheetCloseLabel
import io.github.suqi8.coui.kmp.theme.COUITheme
import io.github.suqi8.coui.kmp.theme.ColorSchemeMode
import io.github.suqi8.coui.kmp.theme.ThemeController
import io.github.suqi8.coui.kmp.theme.darkColorScheme
import io.github.suqi8.coui.kmp.theme.lightColorScheme

/** Appearance of a [COUI17Theme]: follow the system, or force one. */
enum class ThemeMode { System, Light, Dark }

/**
 * The accent of an app: the colour of its filled buttons, switches, radios, links and selection.
 * [dark] is the same hue made lighter for contrast on near-black pages.
 */
data class Accent(val light: Color, val dark: Color) {
    companion object {
        /** ColorOS 17's system blue. */
        val Blue = Accent(Color(0xFF0066FF), Color(0xFF3D8BFF))
        val Green = Accent(Color(0xFF12B36B), Color(0xFF3CCB86))
        val Orange = Accent(Color(0xFFFF7A1A), Color(0xFFFF9547))
        val Red = Accent(Color(0xFFE5483B), Color(0xFFFF6B5E))
        val Purple = Accent(Color(0xFF7A5CFA), Color(0xFF9A84FF))
        val Cyan = Accent(Color(0xFF00A8C6), Color(0xFF36C6E0))

        /** The presets, in the order a colour picker shows them. */
        val Presets = listOf(Blue, Green, Orange, Red, Purple, Cyan)
    }
}

/** Whether the current [COUI17Theme] is dark (resolved from its mode and the system setting). */
val LocalIsDarkTheme = staticCompositionLocalOf { false }

/**
 * The entry point of a COUI17 app: `COUITheme` with one [accent] applied to every accent slot of
 * the colour scheme, the system bars matched to the theme, and the library's portable defaults.
 *
 * - **One accent.** COUI's scheme has several accent slots (`primary`, `primaryContainer`,
 *   `containerTheme` — the fill of a filled button — and the tinted `tertiaryContainer`). Setting
 *   only `primary` leaves filled buttons in the stock blue next to switches in the app's colour.
 * - **System bars.** The status / navigation bar icons follow this theme, not the system's (an app
 *   forced to dark on a light system would otherwise get dark icons on a dark page), and the
 *   contrast scrim over the gesture bar is switched off. Call `enableEdgeToEdge()` in the activity.
 * - **Portable materials.** With [platformMaterials] false (the default) glass is always drawn by
 *   the library's own Compose blur engine. True lets it use the Oplus framework's native bar
 *   materials on ColorOS devices, which reflects into private `com.oplus.*` classes.
 * - **Localized chrome.** The sheet close capsule is labelled from this module's string resources
 *   (English / 简体中文), and long small-bar titles scroll instead of being cut ([titleMarquee]).
 */
@Composable
fun COUI17Theme(
    mode: ThemeMode = ThemeMode.System,
    accent: Accent = Accent.Blue,
    platformMaterials: Boolean = false,
    titleMarquee: Boolean = true,
    content: @Composable () -> Unit,
) {
    val dark = when (mode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }
    val controller = remember(mode, accent) {
        ThemeController(
            colorSchemeMode = when (mode) {
                ThemeMode.System -> ColorSchemeMode.System
                ThemeMode.Light -> ColorSchemeMode.Light
                ThemeMode.Dark -> ColorSchemeMode.Dark
            },
            lightColors = lightColorScheme(
                primary = accent.light,
                primaryVariant = accent.light,
                primaryContainer = accent.light,
                tertiaryContainer = accent.light.copy(alpha = 0.15f),
                onTertiaryContainer = accent.light,
                containerTheme = accent.light,
            ),
            darkColors = darkColorScheme(
                primary = accent.dark,
                primaryVariant = accent.dark,
                primaryContainer = accent.dark,
                tertiaryContainer = accent.dark.copy(alpha = 0.2f),
                onTertiaryContainer = accent.dark,
                containerTheme = accent.dark,
            ),
        )
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = generateSequence(view.context) { (it as? ContextWrapper)?.baseContext }
                .filterIsInstance<Activity>().firstOrNull()?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !dark
                isAppearanceLightNavigationBars = !dark
            }
            if (Build.VERSION.SDK_INT >= 29) {
                window.isNavigationBarContrastEnforced = false
                window.isStatusBarContrastEnforced = false
            }
        }
    }
    COUITheme(controller = controller) {
        CompositionLocalProvider(
            LocalIsDarkTheme provides dark,
            LocalPlatformAppBarMaterialEnabled provides platformMaterials,
            LocalPlatformBottomBarMaterialEnabled provides platformMaterials,
            LocalSheetCloseLabel provides stringResource(R.string.coui17_action_close),
            LocalTopAppBarTitleMarquee provides titleMarquee,
            content = content,
        )
    }
}
