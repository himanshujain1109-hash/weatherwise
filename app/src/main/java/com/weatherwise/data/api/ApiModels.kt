package com.weatherwise.data.api

import com.google.gson.annotations.SerializedName

data class CurrentWeatherResponse(
    @SerializedName("coord") val coord: CoordDto?,
    @SerializedName("weather") val weather: List<WeatherDto>?,
    @SerializedName("base") val base: String?,
    @SerializedName("main") val main: MainDto?,
    @SerializedName("visibility") val visibility: Int?,
    @SerializedName("wind") val wind: WindDto?,
    @SerializedName("clouds") val clouds: CloudsDto?,
    @SerializedName("dt") val dt: Long?,
    @SerializedName("sys") val sys: SysDto?,
    @SerializedName("timezone") val timezone: Int?,
    @SerializedName("id") val id: Long?,
    @SerializedName("name") val name: String?,
    @SerializedName("cod") val cod: Int?
)

data class ForecastResponse(
    @SerializedName("cod") val cod: String?,
    @SerializedName("message") val message: Int?,
    @SerializedName("cnt") val cnt: Int?,
    @SerializedName("list") val list: List<ForecastItemDto>?,
    @SerializedName("city") val city: CityDto?
)

data class ForecastItemDto(
    @SerializedName("dt") val dt: Long?,
    @SerializedName("main") val main: MainDto?,
    @SerializedName("weather") val weather: List<WeatherDto>?,
    @SerializedName("clouds") val clouds: CloudsDto?,
    @SerializedName("wind") val wind: WindDto?,
    @SerializedName("visibility") val visibility: Int?,
    @SerializedName("pop") val pop: Double?, // Probability of precipitation (0.0 to 1.0)
    @SerializedName("dt_txt") val dtTxt: String?
)

data class CoordDto(
    @SerializedName("lon") val lon: Double?,
    @SerializedName("lat") val lat: Double?
)

data class WeatherDto(
    @SerializedName("id") val id: Int?,
    @SerializedName("main") val main: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("icon") val icon: String?
)

data class MainDto(
    @SerializedName("temp") val temp: Double?,
    @SerializedName("feels_like") val feelsLike: Double?,
    @SerializedName("temp_min") val tempMin: Double?,
    @SerializedName("temp_max") val tempMax: Double?,
    @SerializedName("pressure") val pressure: Int?,
    @SerializedName("sea_level") val seaLevel: Int?,
    @SerializedName("grnd_level") val grndLevel: Int?,
    @SerializedName("humidity") val humidity: Int?,
    @SerializedName("temp_kf") val tempKf: Double?
)

data class WindDto(
    @SerializedName("speed") val speed: Double?,
    @SerializedName("deg") val deg: Int?,
    @SerializedName("gust") val gust: Double?
)

data class CloudsDto(
    @SerializedName("all") val all: Int?
)

data class SysDto(
    @SerializedName("type") val type: Int?,
    @SerializedName("id") val id: Long?,
    @SerializedName("country") val country: String?,
    @SerializedName("sunrise") val sunrise: Long?,
    @SerializedName("sunset") val sunset: Long?
)

data class CityDto(
    @SerializedName("id") val id: Long?,
    @SerializedName("name") val name: String?,
    @SerializedName("coord") val coord: CoordDto?,
    @SerializedName("country") val country: String?,
    @SerializedName("population") val population: Long?,
    @SerializedName("timezone") val timezone: Int?,
    @SerializedName("sunrise") val sunrise: Long?,
    @SerializedName("sunset") val sunset: Long?
)

data class GeocodingItem(
    @SerializedName("name") val name: String,
    @SerializedName("lat") val lat: Double,
    @SerializedName("lon") val lon: Double,
    @SerializedName("country") val country: String,
    @SerializedName("state") val state: String?
)

data class AirPollutionResponse(
    @SerializedName("coord") val coord: CoordDto?,
    @SerializedName("list") val list: List<AirPollutionItemDto>?
)

data class AirPollutionItemDto(
    @SerializedName("main") val main: AirPollutionMainDto?,
    @SerializedName("components") val components: Map<String, Double>?,
    @SerializedName("dt") val dt: Long?
)

data class AirPollutionMainDto(
    @SerializedName("aqi") val aqi: Int? // Air Quality Index: 1 = Good, 2 = Fair, 3 = Moderate, 4 = Poor, 5 = Very Poor
)
