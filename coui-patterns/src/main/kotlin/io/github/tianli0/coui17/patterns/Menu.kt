// Copyright 2026, COUI17 contributors
// SPDX-License-Identifier: Apache-2.0

package io.github.tianli0.coui17.patterns

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.IntSize
import androidx.compose.runtime.CompositionLocalProvider
import io.github.suqi8.coui.kmp.basic.DropdownImpl
import io.github.suqi8.coui.kmp.basic.LocalDropdownCheckIcon
import androidx.compose.ui.graphics.vector.ImageVector
import io.github.suqi8.coui.kmp.basic.DropdownItem
import io.github.suqi8.coui.kmp.basic.ListPopupColumn
import io.github.suqi8.coui.kmp.basic.PreciseClickState
import io.github.suqi8.coui.kmp.blur.isBlurEnabled
import io.github.suqi8.coui.kmp.window.WindowListPopup

/**
 * One row of a [GlassMenu]. [selected] shows the check mark; [danger] paints the label red.
 * [trailingIcon] replaces the check at the row's end and shows whenever it is set, e.g. the ↑ / ↓
 * that ColorOS Files puts on the active sort key instead of a check.
 */
data class MenuEntry(
    val text: String,
    val selected: Boolean = false,
    val danger: Boolean = false,
    val enabled: Boolean = true,
    val trailingIcon: ImageVector? = null,
    val onClick: () -> Unit,
)

/**
 * A ColorOS 17 dropdown menu: a coui list popup whose rows form ONE group, so the list has no
 * divider bars between rows, on frosted glass. Place it inside the anchor's `Box`, next to the key
 * that opens it.
 *
 * Opened by a touch on a row (a long press) instead of a key, pass [touch]: a
 * `rememberPreciseClickState()` whose `Modifier.preciseClickAnchor` sits on the row or on the list
 * holding it. The menu then opens at the finger, centred on it horizontally and kept inside the
 * window's side margins (COUI `PreciseClickHelper`); keyboard and accessibility opens, which record
 * no point, fall back to the anchor.
 *
 * The popup samples its host window while open and during its transition. It uses the opaque
 * surface until the first frame arrives or when backdrop blur is unavailable.
 */
@Composable
fun GlassMenu(show: Boolean, entries: List<MenuEntry>, onDismiss: () -> Unit, touch: PreciseClickState? = null) {
    WindowListPopup(
        show = show,
        onDismissRequest = onDismiss,
        preciseClickState = touch,
    ) {
        ListPopupColumn {
            entries.forEachIndexed { index, entry ->
                val trailing = entry.trailingIcon
                val item = remember(entry.text, entry.enabled, entry.danger) {
                    DropdownItem(text = entry.text, enabled = entry.enabled, alert = entry.danger)
                }
                CompositionLocalProvider(LocalDropdownCheckIcon provides trailing) {
                    DropdownImpl(
                        item = item,
                        optionSize = entries.size,
                        isSelected = entry.selected || trailing != null,
                        index = index,
                        // Rows of one group stay contiguous: COUI reserves a divider band only where the group changes.
                        groupKey = MenuGroup,
                        role = Role.Button,
                        onSelectedIndexChange = {
                            onDismiss()
                            entry.onClick()
                        },
                    )
                }
            }
        }
    }
}

private object MenuGroup

/** The page under a popup, already blurred, and the size of the window it was captured from. */
class PopupGlass(val image: ImageBitmap, val windowSize: IntSize)

/**
 * While [active], a blurred, downscaled snapshot of this activity's window, for a popup window to
 * paint under its translucent surface (`staticBackdrop` of the coui `Window*` popups). Null until
 * the capture lands (one frame, hidden by the popup's fade-in), when blur is off, or below
 * Android 8. Nothing is captured while inactive.
 */
@Composable
fun rememberPopupGlass(active: Boolean): PopupGlass? {
    val view = LocalView.current
    val enabled = isBlurEnabled() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    var glass by remember { mutableStateOf<PopupGlass?>(null) }
    DisposableEffect(active, enabled, view) {
        var disposed = false
        if (active && enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // A snapshot from the last time the popup was open would show the wrong page.
            glass = null
            val window = view.context.findActivity()?.window
            val decor = window?.peekDecorView()
            if (window != null && decor != null && decor.width > 0 && decor.height > 0) {
                val size = IntSize(decor.width, decor.height)
                runCatching {
                    val bitmap = Bitmap.createBitmap(
                        (size.width / CAPTURE_DOWNSCALE).coerceAtLeast(1),
                        (size.height / CAPTURE_DOWNSCALE).coerceAtLeast(1),
                        Bitmap.Config.ARGB_8888,
                    )
                    PixelCopy.request(
                        window,
                        bitmap,
                        { result ->
                            if (!disposed && result == PixelCopy.SUCCESS) {
                                glass = runCatching { PopupGlass(blurBitmap(bitmap).asImageBitmap(), size) }.getOrNull()
                            }
                        },
                        Handler(Looper.getMainLooper()),
                    )
                }
            }
        }
        onDispose { disposed = true }
    }
    return glass.takeIf { enabled }
}

/** An eighth of the window is plenty under a blur and keeps the bitmap around 200 KB. */
private const val CAPTURE_DOWNSCALE = 8

/**
 * Three box passes approximate a Gaussian. The popup material blurs at 25px
 * (`RoundFrameLayout`); three passes of radius 3 at 1/8 scale give σ ≈ 3.2 × 8 ≈ 26px (radius 4
 * gave ≈ 36px, 1.4 times the device).
 */
private const val BLUR_RADIUS = 3
private const val BLUR_PASSES = 3

private fun Context.findActivity(): Activity? {
    var c: Context? = this
    while (c is ContextWrapper) {
        if (c is Activity) return c
        c = c.baseContext
    }
    return null
}

private fun blurBitmap(source: Bitmap): Bitmap {
    val w = source.width
    val h = source.height
    val pixels = IntArray(w * h)
    source.getPixels(pixels, 0, w, 0, 0, w, h)
    return Bitmap.createBitmap(boxBlur(pixels, w, h, BLUR_RADIUS, BLUR_PASSES), w, h, Bitmap.Config.ARGB_8888)
}

/**
 * Separable box blur run [passes] times over opaque ARGB [pixels] of a [w] × [h] image; returns the
 * blurred pixels (the input array is used as scratch space). Edges repeat the border pixel.
 */
internal fun boxBlur(pixels: IntArray, w: Int, h: Int, radius: Int, passes: Int): IntArray {
    if (radius <= 0 || w < 2 || h < 2) return pixels
    val scratch = IntArray(w * h)
    repeat(passes) {
        blurPass(pixels, scratch, w, h, radius, horizontal = true)
        blurPass(scratch, pixels, w, h, radius, horizontal = false)
    }
    return pixels
}

/** One running-sum pass along rows or columns. */
private fun blurPass(src: IntArray, dst: IntArray, w: Int, h: Int, radius: Int, horizontal: Boolean) {
    val lines = if (horizontal) h else w
    val length = if (horizontal) w else h
    val window = radius * 2 + 1
    for (line in 0 until lines) {
        var r = 0
        var g = 0
        var b = 0
        fun at(i: Int): Int {
            val k = i.coerceIn(0, length - 1)
            return if (horizontal) src[line * w + k] else src[k * w + line]
        }
        for (i in -radius..radius) {
            val p = at(i)
            r += (p shr 16) and 0xFF
            g += (p shr 8) and 0xFF
            b += p and 0xFF
        }
        for (i in 0 until length) {
            val out = (0xFF shl 24) or ((r / window) shl 16) or ((g / window) shl 8) or (b / window)
            if (horizontal) dst[line * w + i] = out else dst[i * w + line] = out
            val leaving = at(i - radius)
            val entering = at(i + radius + 1)
            r += ((entering shr 16) and 0xFF) - ((leaving shr 16) and 0xFF)
            g += ((entering shr 8) and 0xFF) - ((leaving shr 8) and 0xFF)
            b += (entering and 0xFF) - (leaving and 0xFF)
        }
    }
}
