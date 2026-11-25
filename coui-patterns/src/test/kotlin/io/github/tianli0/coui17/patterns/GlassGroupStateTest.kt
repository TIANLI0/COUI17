package io.github.tianli0.coui17.patterns

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class GlassGroupStateTest {
    private val density = Density(3f)
    private val ltr = LayoutDirection.Ltr

    @Test
    fun anUnmovedMemberKeepsTheSameUnion() {
        val state = GlassGroupState()
        val key = Any()
        state.place(key, CircleShape, Offset(10f, 20f), Size(120f, 120f), ltr, density)
        val first = state.union
        // A positioning callback with the same geometry (the IME moved the whole group, say).
        state.place(key, CircleShape, Offset(10f, 20f), Size(120f, 120f), ltr, density)
        assertSame(first, state.union)
    }

    @Test
    fun aMovedMemberRebuildsTheUnion() {
        val state = GlassGroupState()
        val key = Any()
        state.place(key, CircleShape, Offset(10f, 20f), Size(120f, 120f), ltr, density)
        val first = state.union
        state.place(key, CircleShape, Offset(40f, 20f), Size(120f, 120f), ltr, density)
        val second = state.union
        assertNotSame(first, second)
        val outline = (second as UnionShape).outlines.single() as Outline.Rounded
        assertEquals(Rect(40f, 20f, 160f, 140f), outline.roundRect.let { Rect(it.left, it.top, it.right, it.bottom) })
    }

    @Test
    fun outlinesAreInTheGroupsCoordinates() {
        val state = GlassGroupState()
        state.place(Any(), RoundedCornerShape(20.dp), Offset(0f, 0f), Size(300f, 120f), ltr, density)
        state.place(Any(), CircleShape, Offset(330f, 0f), Size(120f, 120f), ltr, density)
        val rects = (state.union as UnionShape).outlines.map { it.bounds }.sortedBy { it.left }
        assertEquals(listOf(Rect(0f, 0f, 300f, 120f), Rect(330f, 0f, 450f, 120f)), rects)
    }

    @Test
    fun aRemovedMemberLeavesTheUnion() {
        val state = GlassGroupState()
        val a = Any()
        val b = Any()
        state.place(a, CircleShape, Offset.Zero, Size(120f, 120f), ltr, density)
        state.place(b, CircleShape, Offset(130f, 0f), Size(120f, 120f), ltr, density)
        state.remove(a)
        val outlines = (state.union as UnionShape).outlines
        assertEquals(1, outlines.size)
        assertTrue(outlines.single().bounds.left == 130f)
        // Placing it again after removal registers it again (the geometry cache was cleared).
        state.place(a, CircleShape, Offset.Zero, Size(120f, 120f), ltr, density)
        assertEquals(2, (state.union as UnionShape).outlines.size)
    }
}
