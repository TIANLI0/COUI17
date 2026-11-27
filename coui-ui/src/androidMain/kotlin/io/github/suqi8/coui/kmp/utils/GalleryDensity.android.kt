// Copyright 2026, compose-coui-ui contributors
// SPDX-License-Identifier: Apache-2.0
package io.github.suqi8.coui.kmp.utils

import android.util.DisplayMetrics
import androidx.compose.runtime.Composable

@Composable
internal actual fun platformGalleryDensityDpi(): Int? = DisplayMetrics.DENSITY_DEVICE_STABLE.takeIf { it > 0 }
