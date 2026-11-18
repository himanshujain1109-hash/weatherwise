package com.weatherwise.domain.model

data class WeatherData(
    val cityName: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val temperatureCelsius: Double,
    val feelsLikeCelsius: Double,
    val tempMinCelsius: Double,
    val tempMaxCelsius: Double,
    val conditionType: WeatherConditionType,
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
    val uvIndex: Double? = null,
    val observationTimestamp: Long,
    val timezoneOffsetSeconds: Int,
    val isOffline: Boolean = false,
    val lastUpdatedMillis: Long = System.currentTimeMillis()
)
