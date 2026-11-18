package com.weatherwise.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weatherwise.data.preferences.ThemeMode
import com.weatherwise.data.preferences.UserPreferences
import com.weatherwise.data.preferences.UserPreferencesRepository
import com.weatherwise.data.repository.WeatherRepository
import com.weatherwise.utils.TemperatureUnit
import com.weatherwise.utils.WindSpeedUnit
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferencesRepository: UserPreferencesRepository,
    private val weatherRepository: WeatherRepository
) : ViewModel() {

    val preferences: StateFlow<UserPreferences?> = preferencesRepository.userPreferencesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun setTemperatureUnit(unit: TemperatureUnit) {
        viewModelScope.launch {
            preferencesRepository.setTemperatureUnit(unit)
        }
    }

    fun setWindSpeedUnit(unit: WindSpeedUnit) {
        viewModelScope.launch {
            preferencesRepository.setWindSpeedUnit(unit)
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            preferencesRepository.setThemeMode(mode)
        }
    }

    fun setAnimationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setAnimationsEnabled(enabled)
        }
    }

    fun setAutoRefreshMinutes(minutes: Int) {
        viewModelScope.launch {
            preferencesRepository.setAutoRefreshMinutes(minutes)
        }
    }

    fun setLocationWeatherEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setLocationWeatherEnabled(enabled)
        }
    }

    fun setCustomApiKey(key: String) {
        viewModelScope.launch {
            preferencesRepository.setCustomApiKey(key)
        }
    }

    fun clearSearchHistory() {
        viewModelScope.launch {
            weatherRepository.clearSearchHistory()
        }
    }

    fun clearCachedData() {
        viewModelScope.launch {
            weatherRepository.clearCachedData()
        }
    }
}
