// Copyright 2026, compose-coui-ui contributors
// SPDX-License-Identifier: Apache-2.0
@file:OptIn(InternalCouiBlurApi::class)

package io.github.suqi8.coui.kmp.utils

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
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntSize
import io.github.suqi8.coui.kmp.blur.Backdrop
import io.github.suqi8.coui.kmp.blur.ImageBackdrop
import io.github.suqi8.coui.kmp.blur.InternalCouiBlurApi
import io.github.suqi8.coui.kmp.blur.isBlurEnabled

/**
 * Downscale of the capture. The panels blur it by 200–400 device pixels, far below what a quarter
 * resolution still resolves, and a quarter keeps a 1240 x 2772 page under 1 MB.
 */
private const val CAPTURE_DOWNSCALE = 4

/** Shortest gap between two captures: a page that redraws every frame is sampled at about 30 fps. */
private const val MIN_CAPTURE_INTERVAL_MS = 33L

/** Captures rotate through this many bitmaps, so the one being written is never the one on screen. */
private const val CAPTURE_BUFFERS = 3

/**
 * Captures the host window while [active] and keeps the capture current: the first copy is taken
 * at once, then a new one whenever the host's view tree draws again (at most every
 * [MIN_CAPTURE_INTERVAL_MS], one copy in flight), into a small ring of reused bitmaps. A page that
 * does not change is not copied again, so an idle panel costs nothing; one that streams text or
 * scrolls under the panel shows through it live instead of as the still taken when it opened.
 */
@Composable
internal actual fun rememberHostWindowBackdrop(active: Boolean): State<Backdrop?> {
    val state = remember { mutableStateOf<Backdrop?>(null) }
    val view = LocalView.current
    // The glass needs runtime shaders (API 33); below that, or with blur off, the panel keeps its
    // flat fill and a capture would only cost memory.
    val capture = active && isBlurEnabled() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    DisposableEffect(capture, view) {
        var disposed = false
        val window = if (capture) view.context.findActivity()?.window else null
        val decor = window?.peekDecorView()
        val main = Handler(Looper.getMainLooper())
        if (window == null || decor == null || decor.width <= 0 || decor.height <= 0) {
            return@DisposableEffect onDispose { state.value = null }
        }
        val buffers = arrayOfNulls<Bitmap>(CAPTURE_BUFFERS)
        var next = 0
        var inFlight = false
        var pending = false
        var lastAt = 0L

        fun bufferFor(w: Int, h: Int): Bitmap {
            val b = buffers[next]
            val bmp = if (b != null && b.width == w && b.height == h) b else Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).also { buffers[next] = it }
            next = (next + 1) % CAPTURE_BUFFERS
            return bmp
        }

        lateinit var request: () -> Unit
        val runRequest = Runnable { request() }
        request = request@{
            if (disposed || inFlight) return@request
            if (decor.width <= 0 || decor.height <= 0) return@request
            val now = android.os.SystemClock.uptimeMillis()
            val wait = lastAt + MIN_CAPTURE_INTERVAL_MS - now
            if (wait > 0) {
                if (!pending) {
                    pending = true
                    main.postDelayed({ pending = false; request() }, wait)
                }
                return@request
            }
            val sourceSize = IntSize(decor.width, decor.height)
            val origin = IntArray(2).also(decor::getLocationOnScreen)
            val bitmap = bufferFor(
                (sourceSize.width / CAPTURE_DOWNSCALE).coerceAtLeast(1),
                (sourceSize.height / CAPTURE_DOWNSCALE).coerceAtLeast(1),
            )
            inFlight = true
            lastAt = now
            try {
                PixelCopy.request(
                    window,
                    bitmap,
                    { result ->
                        inFlight = false
                        if (!disposed && result == PixelCopy.SUCCESS) {
                            val originOffset = Offset(origin[0].toFloat(), origin[1].toFloat())
                            when (val current = state.value) {
                                is ImageBackdrop -> current.update(bitmap.asImageBitmap(), sourceSize, originOffset)
                                else -> state.value = ImageBackdrop(bitmap.asImageBitmap(), sourceSize, originOffset)
                            }
                        }
                    },
                    main,
                )
            } catch (_: IllegalArgumentException) {
                // The window has no surface yet; the panel keeps the inherited backdrop.
                inFlight = false
            }
        }
        // The host drew a new frame: sample it (coalesced and rate-limited above). Posted, because
        // a copy must not be requested from inside the host's own draw pass.
        val onDraw = android.view.ViewTreeObserver.OnDrawListener { if (!disposed) main.post(runRequest) }
        val observer = decor.viewTreeObserver
        observer.addOnDrawListener(onDraw)
        request()
        onDispose {
            disposed = true
            main.removeCallbacks(runRequest)
            // An OnDrawListener cannot be removed while the tree dispatches it.
            main.post { runCatching { if (decor.viewTreeObserver.isAlive) decor.viewTreeObserver.removeOnDrawListener(onDraw) } }
            state.value = null
        }
    }
    return state
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
