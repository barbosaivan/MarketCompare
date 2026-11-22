package com.barbosa.marketcompare.presentation.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ZoomPanStateTest {

    @Test
    fun `initial state starts at minScale and zero offset`() {
        val state = ZoomPanState(minScale = 1.0f, maxScale = 5.0f)
        assertEquals(1.0f, state.scale, 0.001f)
        assertEquals(Offset.Zero, state.offset)
    }

    @Test
    fun `zoomIn increases scale and respects maxScale`() {
        val state = ZoomPanState(minScale = 1.0f, maxScale = 3.0f)
        state.zoomIn()
        assertEquals(1.5f, state.scale, 0.001f)

        state.zoomIn()
        assertEquals(2.25f, state.scale, 0.001f)

        state.zoomIn()
        assertEquals(3.0f, state.scale, 0.001f) // Capped at maxScale

        state.zoomIn()
        assertEquals(3.0f, state.scale, 0.001f)
    }

    @Test
    fun `zoomOut decreases scale and resets offset at minScale`() {
        val state = ZoomPanState(minScale = 1.0f, maxScale = 4.0f)
        state.scale = 2.25f
        state.offset = Offset(50f, 50f)

        state.zoomOut()
        assertEquals(1.5f, state.scale, 0.001f)

        state.zoomOut()
        assertEquals(1.0f, state.scale, 0.001f)
        assertEquals(Offset.Zero, state.offset)
    }

    @Test
    fun `double tap toggles between default and 2_5x scale`() {
        val state = ZoomPanState(minScale = 1.0f, maxScale = 5.0f)
        state.containerSize = Size(1000f, 2000f)
        state.contentSize = Size(1000f, 2000f)

        // First double tap zooms in to 2.5f
        state.onDoubleTap(Offset(500f, 1000f))
        assertEquals(2.5f, state.scale, 0.001f)

        // Second double tap resets to 1.0f
        state.onDoubleTap(Offset(500f, 1000f))
        assertEquals(1.0f, state.scale, 0.001f)
        assertEquals(Offset.Zero, state.offset)
    }

    @Test
    fun `reset restores scale to minScale and offset to zero`() {
        val state = ZoomPanState(minScale = 1.0f, maxScale = 5.0f)
        state.scale = 3.5f
        state.offset = Offset(120f, -80f)

        state.reset()
        assertEquals(1.0f, state.scale, 0.001f)
        assertEquals(Offset.Zero, state.offset)
    }

    @Test
    fun `onTransform applies pan and zoom while clamping within bounds`() {
        val state = ZoomPanState(minScale = 1.0f, maxScale = 5.0f)
        state.containerSize = Size(400f, 800f)
        state.contentSize = Size(400f, 800f)

        state.onTransform(panChange = Offset(20f, 30f), zoomChange = 2.0f)
        assertEquals(2.0f, state.scale, 0.001f)
        // Scaled size is 800x1600, max pan is 200x400
        assertTrue(state.offset.x in -200f..200f)
        assertTrue(state.offset.y in -400f..400f)
    }

    @Test
    fun `clampOffset returns zero when scale is at minScale or sizes are zero`() {
        val state = ZoomPanState(minScale = 1.0f, maxScale = 5.0f)
        val clampedAtZeroSizes = state.clampOffset(Offset(100f, 100f), 2.0f)
        assertEquals(Offset.Zero, clampedAtZeroSizes)

        state.containerSize = Size(500f, 500f)
        state.contentSize = Size(500f, 500f)
        val clampedAtMinScale = state.clampOffset(Offset(100f, 100f), 1.0f)
        assertEquals(Offset.Zero, clampedAtMinScale)
    }
}
