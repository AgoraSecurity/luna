package com.tarmiga.luna

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.webkit.JavascriptInterface

class LunaBridge(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("luna_prefs", Context.MODE_PRIVATE)

    @JavascriptInterface
    fun saveData(data: String) {
        Log.d("LunaBridge", "Saving data: $data")
        prefs.edit().putString("luna_state", data).apply()
    }

    @JavascriptInterface
    fun loadData(): String? {
        val data = prefs.getString("luna_state", null)
        Log.d("LunaBridge", "Loading data: $data")
        return data
    }
}
