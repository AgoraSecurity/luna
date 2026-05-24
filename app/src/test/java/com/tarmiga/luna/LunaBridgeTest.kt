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

    private lateinit var lunaBridge: LunaBridge

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        `when`(mockContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mockPrefs)
        `when`(mockPrefs.edit()).thenReturn(mockEditor)
        `when`(mockEditor.putString(anyString(), anyString())).thenReturn(mockEditor)
        
        // Use a mock string for Log.d to avoid RuntimeException in unit tests
        // or just rely on the fact that we are testing the logic.
        // Actually, LunaBridge calls Log.d which is a stub in unit tests.
        
        lunaBridge = LunaBridge(mockContext)
    }

    @Test
    fun testSaveData() {
        val testData = "{\"key\": \"value\"}"
        lunaBridge.saveData(testData)
        
        verify(mockEditor).putString("luna_state", testData)
        verify(mockEditor).apply()
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
