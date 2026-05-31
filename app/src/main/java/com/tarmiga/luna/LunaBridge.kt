package com.tarmiga.luna

import android.content.SharedPreferences
import android.util.Log
import android.webkit.JavascriptInterface
import com.tarmiga.luna.data.CycleDao
import com.tarmiga.luna.data.CycleEntry
import com.tarmiga.luna.data.LogEntry
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject

class LunaBridge(
    private val cycleDao: CycleDao,
    private val prefs: SharedPreferences,
    private val notificationHelper: NotificationHelper
) {

    @JavascriptInterface
    fun saveData(data: String) {
        Log.d("LunaBridge", "Saving data: $data")
        
        // Preserve legacy storage for compatibility
        prefs.edit().putString("luna_state", data).apply()

        // Upsert into Room
        runBlocking {
            try {
                val stateObj = JSONObject(data)
                
                // Save Cycles
                val cycleStartsArr = stateObj.optJSONArray("cycleStarts")
                if (cycleStartsArr != null) {
                    for (i in 0 until cycleStartsArr.length()) {
                        val date = cycleStartsArr.getString(i)
                        cycleDao.insertCycle(CycleEntry(date))
                    }
                }

                // Save Logs
                val logsObj = stateObj.optJSONObject("logs")
                if (logsObj != null) {
                    val keys = logsObj.keys()
                    while (keys.hasNext()) {
                        val date = keys.next()
                        val log = logsObj.getJSONObject(date)
                        val symptomsArr = log.optJSONArray("symptoms")
                        val symptomsList = mutableListOf<String>()
                        if (symptomsArr != null) {
                            for (i in 0 until symptomsArr.length()) {
                                symptomsList.add(symptomsArr.getString(i))
                            }
                        }

                        cycleDao.insertLog(
                            LogEntry(
                                date = date,
                                energy = log.optString("energy", "Medium"),
                                mood = log.optString("mood", "Neutral"),
                                symptoms = symptomsList.joinToString(","),
                                note = log.optString("note", ""),
                                periodStart = log.optBoolean("periodStart", false)
                            )
                        )
                    }
                }

                // Update avgCycleLength in prefs if needed
                val avgLen = stateObj.optInt("avgCycleLength", 28)
                prefs.edit().putInt("avg_cycle_length", avgLen).apply()

            } catch (e: org.json.JSONException) {
                Log.e("LunaBridge", "Error parsing/saving JSON to Room", e)
            }
        }
        
        // Sync notifications
        notificationHelper.syncNotificationsFromState(data)
    }

    @JavascriptInterface
    fun loadData(): String {
        return runBlocking {
            val cycles = cycleDao.getAllCycles()
            val logs = cycleDao.getAllLogs()
            
            val stateObj = JSONObject()
            
            val cycleStartsArr = JSONArray()
            cycles.forEach { cycleStartsArr.put(it.date) }
            stateObj.put("cycleStarts", cycleStartsArr)
            
            val logsObj = JSONObject()
            logs.forEach { log ->
                val logItem = JSONObject()
                logItem.put("energy", log.energy)
                logItem.put("mood", log.mood)
                logItem.put("symptoms", JSONArray(log.symptoms.split(",").filter { it.isNotBlank() }))
                logItem.put("note", log.note)
                logItem.put("periodStart", log.periodStart)
                logsObj.put(log.date, logItem)
            }
            stateObj.put("logs", logsObj)
            
            val avgLen = prefs.getInt("avg_cycle_length", 28)
            stateObj.put("avgCycleLength", avgLen)

            val data = stateObj.toString()
            Log.d("LunaBridge", "Loading data from Room: $data")
            data
        }
    }
}
