package com.weatherwise.domain.model

data class HourlyForecast(
    val timestampSeconds: Long,
    val timeLabel: String,
    val temperatureCelsius: Double,
    val conditionType: WeatherConditionType,
    val conditionDescription: String,
    val iconCode: String,
    val precipitationProbabilityPercent: Int,
    val windSpeedMeterSec: Double
)

data class DailyForecast(
    val timestampSeconds: Long,
    val dayLabel: String,
    val dateLabel: String,
    val minTempCelsius: Double,
    val maxTempCelsius: Double,
    val conditionType: WeatherConditionType,
    val conditionDescription: String,
    val iconCode: String,
    val windSpeedMeterSec: Double,
    val humidityPercent: Int
)

data class ForecastBundle(
    val hourlyList: List<HourlyForecast>,
    val dailyList: List<DailyForecast>,
    val cityName: String,
    val country: String,
    val isOffline: Boolean = false,
    val lastUpdatedMillis: Long = System.currentTimeMillis()
)
