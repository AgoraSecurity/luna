package com.tarmiga.luna

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class CycleLogicTest {

    @Test
    fun testCycleStateLatestStart() {
        val starts = listOf("2026-03-20", "2026-02-20")
        val state = CycleState(starts, 28)
        assertEquals(LocalDate.of(2026, 3, 20), state.getLatestStart())
    }

    @Test
    fun testCycleStateLatestStart_Empty() {
        val state = CycleState(emptyList(), 28)
        assertEquals(null, state.getLatestStart())
    }

    @Test
    fun testCycleStateLatestStart_Invalid() {
        val state = CycleState(listOf("invalid-date"), 28)
        assertEquals(null, state.getLatestStart())
    }
}
