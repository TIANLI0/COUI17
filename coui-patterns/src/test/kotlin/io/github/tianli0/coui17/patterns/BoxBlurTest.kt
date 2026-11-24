package io.github.tianli0.coui17.patterns

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BoxBlurTest {
    private fun solid(w: Int, h: Int, argb: Int) = IntArray(w * h) { argb }

    @Test
    fun aFlatImageStaysFlat() {
        val grey = 0xFF808080.toInt()
        val out = boxBlur(solid(16, 12, grey), 16, 12, radius = 4, passes = 3)
        assertArrayEquals(solid(16, 12, grey), out)
    }

    @Test
    fun aHardEdgeBecomesAMonotonicRamp() {
        val w = 32
        val h = 4
        val pixels = IntArray(w * h) { i -> if (i % w < w / 2) 0xFF000000.toInt() else 0xFFFFFFFF.toInt() }
        val out = boxBlur(pixels, w, h, radius = 4, passes = 3)
        val row = (0 until w).map { out[it] and 0xFF }
        assertEquals(0, row.first())
        assertEquals(255, row.last())
        assertTrue("ramp must not decrease", row.zipWithNext().all { (a, b) -> b >= a })
        assertTrue("the edge is softened", row[w / 2 - 1] in 1..254 && row[w / 2] in 1..254)
    }

    @Test
    fun theResultIsOpaqueAndChannelsStayIndependent() {
        val w = 8
        val h = 8
        val pixels = IntArray(w * h) { i -> if ((i % w + i / w) % 2 == 0) 0xFFFF0000.toInt() else 0xFF000000.toInt() }
        val out = boxBlur(pixels, w, h, radius = 2, passes = 2)
        for (p in out) {
            assertEquals(0xFF, p ushr 24)
            assertEquals(0, (p shr 8) and 0xFF)
            assertEquals(0, p and 0xFF)
        }
    }

    @Test
    fun degenerateInputsAreReturnedUntouched() {
        val one = intArrayOf(0xFF123456.toInt())
        assertArrayEquals(intArrayOf(0xFF123456.toInt()), boxBlur(one, 1, 1, radius = 4, passes = 3))
        val px = solid(4, 4, 0xFF0A0B0C.toInt())
        assertArrayEquals(solid(4, 4, 0xFF0A0B0C.toInt()), boxBlur(px, 4, 4, radius = 0, passes = 3))
    }
}
