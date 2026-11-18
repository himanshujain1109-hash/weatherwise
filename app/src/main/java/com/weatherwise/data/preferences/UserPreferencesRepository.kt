package com.weatherwise.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.weatherwise.utils.TemperatureUnit
import com.weatherwise.utils.WindSpeedUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

enum class ThemeMode(val displayName: String) {
    SYSTEM("System Default"),
    LIGHT("Light"),
    DARK("Dark")
}

data class UserPreferences(
    val temperatureUnit: TemperatureUnit,
    val windSpeedUnit: WindSpeedUnit,
    val themeMode: ThemeMode,
    val animationsEnabled: Boolean,
    val autoRefreshMinutes: Int,
    val locationWeatherEnabled: Boolean,
    val customApiKey: String,
    val defaultCityName: String?,
    val defaultCityLat: Double?,
    val defaultCityLon: Double?
)

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val TEMPERATURE_UNIT = stringPreferencesKey("temperature_unit")
        val WIND_SPEED_UNIT = stringPreferencesKey("wind_speed_unit")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val ANIMATIONS_ENABLED = booleanPreferencesKey("animations_enabled")
        val AUTO_REFRESH_MINUTES = intPreferencesKey("auto_refresh_minutes")
        val LOCATION_WEATHER_ENABLED = booleanPreferencesKey("location_weather_enabled")
        val CUSTOM_API_KEY = stringPreferencesKey("custom_api_key")
        val DEFAULT_CITY_NAME = stringPreferencesKey("default_city_name")
        val DEFAULT_CITY_LAT = doublePreferencesKey("default_city_lat")
        val DEFAULT_CITY_LON = doublePreferencesKey("default_city_lon")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val tempUnitStr = preferences[PreferencesKeys.TEMPERATURE_UNIT] ?: TemperatureUnit.CELSIUS.name
            val tempUnit = try {
                TemperatureUnit.valueOf(tempUnitStr)
            } catch (e: Exception) {
                TemperatureUnit.CELSIUS
            }

            val windUnitStr = preferences[PreferencesKeys.WIND_SPEED_UNIT] ?: WindSpeedUnit.KM_H.name
            val windUnit = try {
                WindSpeedUnit.valueOf(windUnitStr)
            } catch (e: Exception) {
                WindSpeedUnit.KM_H
            }

            val themeStr = preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.SYSTEM.name
            val themeMode = try {
                ThemeMode.valueOf(themeStr)
            } catch (e: Exception) {
                ThemeMode.SYSTEM
            }

            val animations = preferences[PreferencesKeys.ANIMATIONS_ENABLED] ?: true
            val refreshMinutes = preferences[PreferencesKeys.AUTO_REFRESH_MINUTES] ?: 60
            val locationEnabled = preferences[PreferencesKeys.LOCATION_WEATHER_ENABLED] ?: true
            val customApiKey = preferences[PreferencesKeys.CUSTOM_API_KEY] ?: ""
            val defaultCity = preferences[PreferencesKeys.DEFAULT_CITY_NAME]
            val defaultLat = preferences[PreferencesKeys.DEFAULT_CITY_LAT]
            val defaultLon = preferences[PreferencesKeys.DEFAULT_CITY_LON]

            UserPreferences(
                temperatureUnit = tempUnit,
                windSpeedUnit = windUnit,
                themeMode = themeMode,
                animationsEnabled = animations,
                autoRefreshMinutes = refreshMinutes,
                locationWeatherEnabled = locationEnabled,
                customApiKey = customApiKey,
                defaultCityName = defaultCity,
                defaultCityLat = defaultLat,
                defaultCityLon = defaultLon
            )
        }

    suspend fun setTemperatureUnit(unit: TemperatureUnit) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.TEMPERATURE_UNIT] = unit.name
        }
    }

    suspend fun setWindSpeedUnit(unit: WindSpeedUnit) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WIND_SPEED_UNIT] = unit.name
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode.name
        }
    }

    suspend fun setAnimationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ANIMATIONS_ENABLED] = enabled
        }
    }

    suspend fun setAutoRefreshMinutes(minutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_REFRESH_MINUTES] = minutes
        }
    }

    suspend fun setLocationWeatherEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LOCATION_WEATHER_ENABLED] = enabled
        }
    }

    suspend fun setCustomApiKey(key: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CUSTOM_API_KEY] = key.trim()
        }
    }

    suspend fun setDefaultCity(name: String?, lat: Double?, lon: Double?) {
        context.dataStore.edit { preferences ->
            if (name != null && lat != null && lon != null) {
                preferences[PreferencesKeys.DEFAULT_CITY_NAME] = name
                preferences[PreferencesKeys.DEFAULT_CITY_LAT] = lat
                preferences[PreferencesKeys.DEFAULT_CITY_LON] = lon
            } else {
                preferences.remove(PreferencesKeys.DEFAULT_CITY_NAME)
                preferences.remove(PreferencesKeys.DEFAULT_CITY_LAT)
                preferences.remove(PreferencesKeys.DEFAULT_CITY_LON)
            }
        }
    }
}
