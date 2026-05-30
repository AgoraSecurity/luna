package com.tarmiga.luna

import android.content.Context
import android.content.SharedPreferences
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations

class LunaBridgeTest {

    @Mock
    private lateinit var mockContext: Context

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
        `when`(mockContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mockPrefs)
        `when`(mockPrefs.edit()).thenReturn(mockEditor)
        `when`(mockEditor.putString(anyString(), anyString())).thenReturn(mockEditor)
        
        lunaBridge = LunaBridge(mockContext, mockNotificationHelper)
    }

    @Test
    fun testSaveData() {
        val testData = "{\"cycleStarts\": [\"2026-03-20\"], \"avgCycleLength\": 28}"
        lunaBridge.saveData(testData)
        
        verify(mockEditor).putString("luna_state", testData)
        verify(mockEditor).apply()
        verify(mockNotificationHelper).syncNotificationsFromState(testData)
    }

    @Test
    fun testLoadData_Existing() {
        val testData = "{\"key\": \"value\"}"
        `when`(mockPrefs.getString("luna_state", "")).thenReturn(testData)
        
        val result = lunaBridge.loadData()
        assertEquals(testData, result)
    }

    @Test
    fun testLoadData_Empty() {
        `when`(mockPrefs.getString("luna_state", "")).thenReturn("")
        
        val result = lunaBridge.loadData()
        assertEquals("", result)
    }
}
