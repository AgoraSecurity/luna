package com.tarmiga.luna

import android.content.SharedPreferences
import com.tarmiga.luna.data.CycleDao
import com.tarmiga.luna.data.CycleEntry
import com.tarmiga.luna.data.LogEntry
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.Mockito.times
import org.mockito.Mockito.any
import org.mockito.MockitoAnnotations

class LunaBridgeTest {

    @Mock
    private lateinit var mockCycleDao: CycleDao

    @Mock
    private lateinit var mockPrefs: SharedPreferences

    @Mock
    private lateinit var mockEditor: SharedPreferences.Editor

    @Mock
    private lateinit var mockNotificationHelper: NotificationHelper

    private lateinit var lunaBridge: LunaBridge

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        `when`(mockPrefs.edit()).thenReturn(mockEditor)
        `when`(mockEditor.putString(anyString(), anyString())).thenReturn(mockEditor)
        `when`(mockEditor.putInt(anyString(), anyInt())).thenReturn(mockEditor)
        
        lunaBridge = LunaBridge(mockCycleDao, mockPrefs, mockNotificationHelper)
    }

    private fun <T> anyNotNull(): T {
        any<T>()
        return uninitialized()
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> uninitialized(): T = null as T

    @Test
    fun testSaveData() = runBlocking {
        val testData = "{\"cycleStarts\": [\"2026-03-20\"], \"avgCycleLength\": 28, \"logs\": {\"2026-03-21\": {\"energy\": \"High\", \"mood\": \"Great\", \"symptoms\": [\"Cramps\"], \"note\": \"Test\", \"periodStart\": false}}}"
        
        lunaBridge.saveData(testData)
        
        verify(mockEditor).putString("luna_state", testData)
        verify(mockEditor, times(2)).apply()
        verify(mockCycleDao).insertCycle(anyNotNull())
        verify(mockCycleDao).insertLog(anyNotNull())
        verify(mockNotificationHelper).syncNotificationsFromState(testData)
    }

    @Test
    fun testLoadData_Existing() = runBlocking {
        `when`(mockCycleDao.getAllCycles()).thenReturn(listOf(CycleEntry("2026-03-20")))
        `when`(mockCycleDao.getAllLogs()).thenReturn(listOf(LogEntry("2026-03-21", "High", "Great", "Cramps", "Test", false)))
        `when`(mockPrefs.getInt("avg_cycle_length", 28)).thenReturn(28)
        
        val result = lunaBridge.loadData()
        val expected = "{\"cycleStarts\":[\"2026-03-20\"],\"logs\":{\"2026-03-21\":{\"energy\":\"High\",\"mood\":\"Great\",\"note\":\"Test\",\"symptoms\":[\"Cramps\"],\"periodStart\":false}},\"avgCycleLength\":28}"
        
        assertEquals(JSONObject(expected).toString(), JSONObject(result).toString())
    }

    @Test
    fun testLoadData_Empty() = runBlocking {
        `when`(mockCycleDao.getAllCycles()).thenReturn(emptyList())
        `when`(mockCycleDao.getAllLogs()).thenReturn(emptyList())
        `when`(mockPrefs.getInt("avg_cycle_length", 28)).thenReturn(28)
        
        val result = lunaBridge.loadData()
        val expected = "{\"cycleStarts\":[],\"logs\":{},\"avgCycleLength\":28}"
        
        assertEquals(JSONObject(expected).toString(), JSONObject(result).toString())
    }
}
