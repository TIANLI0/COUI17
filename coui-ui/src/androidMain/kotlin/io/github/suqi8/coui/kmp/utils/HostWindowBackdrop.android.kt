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
import android.os.SystemClock
import android.view.PixelCopy
import android.view.ViewTreeObserver
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
 * at once, then a new one after the host's view tree draws again (at most every
 * [MIN_CAPTURE_INTERVAL_MS], one copy in flight; see [HostCaptureScheduler]), into a small ring of
 * reused bitmaps. A page that does not change is not copied again, so an idle panel costs nothing;
 * one that streams text or scrolls under the panel shows through it live instead of as the still
 * taken when it opened.
 */
@Composable
internal actual fun rememberHostWindowBackdrop(active: Boolean): State<Backdrop?> {
    val state = remember { mutableStateOf<Backdrop?>(null) }
    val view = LocalView.current
    // The glass needs runtime shaders (API 33); below that, or with blur off, the panel keeps its
    // flat fill and a capture would only cost memory.
    val capture = active && isBlurEnabled() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    DisposableEffect(capture, view) {
        val window = if (capture) view.context.findActivity()?.window else null
        val decor = window?.peekDecorView()
        if (window == null || decor == null || decor.width <= 0 || decor.height <= 0) {
            return@DisposableEffect onDispose { state.value = null }
        }
        val main = Handler(Looper.getMainLooper())
        // The bitmap being written is never the one on screen: a copy starts at least two copies
        // (and 2 x MIN_CAPTURE_INTERVAL_MS) after the frame that drew the bitmap it overwrites.
        val buffers = arrayOfNulls<Bitmap>(CAPTURE_BUFFERS)
        var next = 0

        fun bufferFor(w: Int, h: Int): Bitmap {
            val b = buffers[next]
            val bmp = if (b != null && b.width == w && b.height == h) b else Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).also { buffers[next] = it }
            next = (next + 1) % CAPTURE_BUFFERS
            return bmp
        }

        lateinit var scheduler: HostCaptureScheduler
        scheduler = HostCaptureScheduler(
            minIntervalMs = MIN_CAPTURE_INTERVAL_MS,
            now = SystemClock::uptimeMillis,
            postDelayed = { task, delay -> main.postDelayed(task, delay) },
            removeCallbacks = { task -> main.removeCallbacks(task) },
        ) copy@{ done ->
            if (decor.width <= 0 || decor.height <= 0) return@copy false
            val sourceSize = IntSize(decor.width, decor.height)
            val origin = IntArray(2).also(decor::getLocationOnScreen)
            val bitmap = bufferFor(
                (sourceSize.width / CAPTURE_DOWNSCALE).coerceAtLeast(1),
                (sourceSize.height / CAPTURE_DOWNSCALE).coerceAtLeast(1),
            )
            try {
                PixelCopy.request(
                    window,
                    bitmap,
                    { result ->
                        if (!scheduler.isDisposed && result == PixelCopy.SUCCESS) {
                            val originOffset = Offset(origin[0].toFloat(), origin[1].toFloat())
                            when (val current = state.value) {
                                is ImageBackdrop -> current.update(bitmap.asImageBitmap(), sourceSize, originOffset)
                                else -> state.value = ImageBackdrop(bitmap.asImageBitmap(), sourceSize, originOffset)
                            }
                        }
                        done()
                    },
                    main,
                )
                true
            } catch (_: IllegalArgumentException) {
                // The window has no surface yet; the panel keeps the inherited backdrop.
                false
            }
        }
        // The host drew a new frame: sample it. Only marks and posts (a copy must not be requested
        // from inside the host's own draw pass); the scheduler coalesces and rate-limits.
        val onDraw = ViewTreeObserver.OnDrawListener { scheduler.onHostDraw() }
        decor.viewTreeObserver.addOnDrawListener(onDraw)
        scheduler.start()
        onDispose {
            scheduler.dispose()
            // An OnDrawListener cannot be removed while the tree dispatches it.
            main.post { runCatching { if (decor.viewTreeObserver.isAlive) decor.viewTreeObserver.removeOnDrawListener(onDraw) } }
            state.value = null
        }
    }
    return state
}

/**
 * When to copy the host window again: after the host has drawn ([onHostDraw]), no sooner than
 * `minIntervalMs` after the previous copy started, and never while a copy is in flight. A draw
 * that lands while a copy is in flight is not lost: the copy's completion schedules the next one,
 * so the panel always ends on the host's last frame. At most one task is posted at a time.
 *
 * `copy` starts a copy and returns false when it could not; otherwise it must call its argument
 * once the copy has finished (successfully or not). Main thread only.
 */
internal class HostCaptureScheduler(
    private val minIntervalMs: Long,
    private val now: () -> Long,
    private val postDelayed: (Runnable, Long) -> Unit,
    private val removeCallbacks: (Runnable) -> Unit,
    private val copy: (done: () -> Unit) -> Boolean,
) {
    var isDisposed = false
        private set
    private var dirty = false
    private var inFlight = false
    private var scheduled = false
    private var hasStarted = false
    private var lastStart = 0L

    private val tick = Runnable {
        scheduled = false
        run()
    }

    /** Takes the first copy at once. */
    fun start() {
        dirty = true
        run()
    }

    /** The host drew a frame. Called from its draw pass: only marks and posts. */
    fun onHostDraw() {
        if (isDisposed) return
        dirty = true
        schedule()
    }

    fun dispose() {
        isDisposed = true
        removeCallbacks(tick)
        scheduled = false
    }

    private fun schedule() {
        if (scheduled || inFlight || isDisposed) return
        scheduled = true
        val wait = if (hasStarted) (lastStart + minIntervalMs - now()).coerceAtLeast(0L) else 0L
        postDelayed(tick, wait)
    }

    private fun run() {
        if (isDisposed || inFlight || !dirty) return
        val t = now()
        if (hasStarted && t - lastStart < minIntervalMs) {
            schedule()
            return
        }
        dirty = false
        hasStarted = true
        lastStart = t
        inFlight = true
        val started = copy {
            inFlight = false
            if (dirty) schedule()
        }
        if (!started) inFlight = false
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
