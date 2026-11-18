package com.weatherwise.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_weather")
data class CachedWeatherEntity(
    @PrimaryKey val id: Int = 1,
    val cityName: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val temperatureCelsius: Double,
    val feelsLikeCelsius: Double,
    val tempMinCelsius: Double,
    val tempMaxCelsius: Double,
    val conditionId: Int,
    val conditionTitle: String,
    val conditionDescription: String,
    val iconCode: String,
    val humidityPercent: Int,
    val windSpeedMeterSec: Double,
    val windDirectionDegrees: Int,
    val pressureHpa: Int,
    val visibilityMeters: Int,
    val cloudinessPercent: Int,
    val sunriseTimestamp: Long,
    val sunsetTimestamp: Long,
    val uvIndex: Double?,
    val observationTimestamp: Long,
    val timezoneOffsetSeconds: Int,
    val lastUpdatedMillis: Long
)

@Entity(tableName = "cached_forecast")
data class CachedForecastEntity(
    @PrimaryKey val id: Int = 1,
    val cityName: String,
    val country: String,
    val forecastJson: String,
    val lastUpdatedMillis: Long
)

@Entity(tableName = "favorite_cities")
data class FavoriteCityEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val state: String? = null,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val isDefault: Boolean = false,
    val addedAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val query: String,
    val name: String,
    val state: String? = null,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val searchedAtMillis: Long = System.currentTimeMillis()
)
