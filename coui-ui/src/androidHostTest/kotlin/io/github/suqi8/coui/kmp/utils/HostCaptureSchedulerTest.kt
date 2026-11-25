// Copyright 2026, COUI17 contributors
// SPDX-License-Identifier: Apache-2.0
package io.github.suqi8.coui.kmp.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** A main looper with a fake clock: posted tasks run when [advance] passes their time. */
private class FakeLooper {
    var time = 1_000L
    private val tasks = mutableListOf<Pair<Long, Runnable>>()

    val posted: Int get() = tasks.size

    fun postDelayed(task: Runnable, delay: Long) {
        tasks += (time + delay) to task
    }

    fun remove(task: Runnable) {
        tasks.removeAll { it.second === task }
    }

    fun advance(ms: Long) {
        val end = time + ms
        while (true) {
            val due = tasks.filter { it.first <= end }.minByOrNull { it.first } ?: break
            tasks.remove(due)
            time = maxOf(time, due.first)
            due.second.run()
        }
        time = end
    }
}

class HostCaptureSchedulerTest {
    private val looper = FakeLooper()
    private val copies = mutableListOf<Long>()
    private var pending: (() -> Unit)? = null

    /** Copies complete only when the test says so, like PixelCopy's asynchronous callback. */
    private val scheduler = HostCaptureScheduler(
        minIntervalMs = 33,
        now = { looper.time },
        postDelayed = looper::postDelayed,
        removeCallbacks = looper::remove,
    ) { done ->
        copies += looper.time
        pending = done
        true
    }

    private fun finishCopy() {
        val done = pending ?: error("no copy in flight")
        pending = null
        done()
    }

    @Test
    fun theFirstCopyIsTakenAtOnce() {
        scheduler.start()
        assertEquals(listOf(1_000L), copies)
    }

    @Test
    fun anIdleHostIsNotCopiedAgain() {
        scheduler.start()
        finishCopy()
        looper.advance(1_000)
        assertEquals(1, copies.size)
    }

    @Test
    fun aHostDrawingEveryFrameIsCopiedAtMostEveryInterval() {
        scheduler.start()
        repeat(60) {
            looper.advance(16)
            scheduler.onHostDraw()
            if (pending != null && looper.time - copies.last() >= 5) finishCopy()
        }
        looper.advance(100)
        if (pending != null) finishCopy()
        val gaps = copies.zipWithNext { a, b -> b - a }
        assertTrue("gaps $gaps", gaps.all { it >= 33 })
        // About 30 copies a second, not 60.
        assertTrue("copies ${copies.size}", copies.size in 25..32)
    }

    @Test
    fun manyDrawsBetweenCopiesPostOneTask() {
        scheduler.start()
        finishCopy()
        repeat(10) { scheduler.onHostDraw() }
        assertEquals(1, looper.posted)
    }

    @Test
    fun aDrawDuringACopyIsCapturedAfterIt() {
        scheduler.start()
        looper.advance(40)
        // The host draws its last frame while the copy is still in flight, then goes idle.
        scheduler.onHostDraw()
        assertEquals(1, copies.size)
        finishCopy()
        looper.advance(100)
        assertEquals("the last frame must be copied", 2, copies.size)
    }

    @Test
    fun nothingRunsAfterDispose() {
        scheduler.start()
        finishCopy()
        scheduler.onHostDraw()
        scheduler.dispose()
        looper.advance(1_000)
        assertEquals(1, copies.size)
        assertEquals(0, looper.posted)
    }

    @Test
    fun aCopyFinishingAfterDisposeSchedulesNothing() {
        scheduler.start()
        scheduler.onHostDraw()
        scheduler.dispose()
        finishCopy()
        looper.advance(1_000)
        assertEquals(1, copies.size)
        assertEquals(0, looper.posted)
    }

    @Test
    fun aCopyThatCannotStartDoesNotBlockLaterOnes() {
        var allow = false
        val copies = mutableListOf<Long>()
        val s = HostCaptureScheduler(33, { looper.time }, looper::postDelayed, looper::remove) { done ->
            if (!allow) return@HostCaptureScheduler false
            copies += looper.time
            done()
            true
        }
        s.start()
        allow = true
        looper.advance(50)
        s.onHostDraw()
        looper.advance(50)
        assertEquals(1, copies.size)
    }
}
