package com.tarmiga.luna.ui.onboarding

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tarmiga.luna.data.CycleEntry
import com.tarmiga.luna.data.LunaDatabase
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class OnboardingViewModel(private val database: LunaDatabase, private val prefs: android.content.SharedPreferences) : ViewModel() {

    private val _selectedDate = mutableStateOf(LocalDate.now())
    val selectedDate: State<LocalDate> = _selectedDate

    private val _isDateValid = mutableStateOf(true)
    val isDateValid: State<Boolean> = _isDateValid

    private val cycleDao = database.cycleDao()

    fun onDateSelected(date: LocalDate) {
        _selectedDate.value = date
        validateDate(date)
    }

    private fun validateDate(date: LocalDate) {
        val now = LocalDate.now()
        val daysDiff = ChronoUnit.DAYS.between(date, now)
        _isDateValid.value = daysDiff in 0..35
    }

    fun completeOnboarding(onComplete: () -> Unit) {
        if (_isDateValid.value) {
            viewModelScope.launch {
                val dateString = _selectedDate.value.format(DateTimeFormatter.ISO_LOCAL_DATE)
                cycleDao.insertCycle(CycleEntry(dateString))
                
                // Save onboarding completed flag
                prefs.edit().putBoolean("onboarding_completed", true).apply()
                
                onComplete()
            }
        }
    }

    companion object {
        fun provideFactory(database: LunaDatabase, prefs: android.content.SharedPreferences): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                OnboardingViewModel(database, prefs)
            }
        }
    }
}
