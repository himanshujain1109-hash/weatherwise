package com.weatherwise.data.api

import com.google.gson.annotations.SerializedName

data class OpenMeteoWeatherResponse(
    @SerializedName("latitude") val latitude: Double?,
    @SerializedName("longitude") val longitude: Double?,
    @SerializedName("timezone") val timezone: String?,
    @SerializedName("utc_offset_seconds") val utcOffsetSeconds: Int?,
    @SerializedName("current") val current: OpenMeteoCurrent?,
    @SerializedName("hourly") val hourly: OpenMeteoHourly?,
    @SerializedName("daily") val daily: OpenMeteoDaily?
)

data class OpenMeteoCurrent(
    @SerializedName("time") val time: String?,
    @SerializedName("temperature_2m") val temperature2m: Double?,
    @SerializedName("relative_humidity_2m") val humidity: Int?,
    @SerializedName("apparent_temperature") val apparentTemperature: Double?,
    @SerializedName("is_day") val isDay: Int?, // 1 for day, 0 for night
    @SerializedName("precipitation") val precipitation: Double?,
    @SerializedName("weather_code") val weatherCode: Int?,
    @SerializedName("cloud_cover") val cloudCover: Int?,
    @SerializedName("pressure_msl") val pressureMsl: Double?,
    @SerializedName("wind_speed_10m") val windSpeed10m: Double?,
    @SerializedName("wind_direction_10m") val windDirection10m: Int?
)

data class OpenMeteoHourly(
    @SerializedName("time") val time: List<String>?,
    @SerializedName("temperature_2m") val temperature2m: List<Double>?,
    @SerializedName("relative_humidity_2m") val humidity: List<Int>?,
    @SerializedName("apparent_temperature") val apparentTemperature: List<Double>?,
    @SerializedName("precipitation_probability") val precipitationProbability: List<Int>?,
    @SerializedName("weather_code") val weatherCode: List<Int>?,
    @SerializedName("wind_speed_10m") val windSpeed10m: List<Double>?
)

data class OpenMeteoDaily(
    @SerializedName("time") val time: List<String>?,
    @SerializedName("weather_code") val weatherCode: List<Int>?,
    @SerializedName("temperature_2m_max") val tempMax: List<Double>?,
    @SerializedName("temperature_2m_min") val tempMin: List<Double>?,
    @SerializedName("sunrise") val sunrise: List<String>?,
    @SerializedName("sunset") val sunset: List<String>?,
    @SerializedName("uv_index_max") val uvIndexMax: List<Double>?,
    @SerializedName("precipitation_probability_max") val precipitationProbabilityMax: List<Int>?,
    @SerializedName("wind_speed_10m_max") val windSpeedMax: List<Double>?
)

data class OpenMeteoGeocodingResponse(
    @SerializedName("results") val results: List<OpenMeteoGeocodingResult>?
)

data class OpenMeteoGeocodingResult(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("country") val country: String?,
    @SerializedName("admin1") val admin1: String?,
    @SerializedName("country_code") val countryCode: String?
)
