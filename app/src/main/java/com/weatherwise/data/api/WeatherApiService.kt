package com.weatherwise.data.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {

    @GET("data/2.5/weather")
    suspend fun getCurrentWeatherByCoordinates(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("units") units: String = "metric",
        @Query("appid") apiKey: String
    ): Response<CurrentWeatherResponse>

    @GET("data/2.5/weather")
    suspend fun getCurrentWeatherByCity(
        @Query("q") cityName: String,
        @Query("units") units: String = "metric",
        @Query("appid") apiKey: String
    ): Response<CurrentWeatherResponse>

    @GET("data/2.5/forecast")
    suspend fun getFiveDayForecastByCoordinates(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("units") units: String = "metric",
        @Query("appid") apiKey: String
    ): Response<ForecastResponse>

    @GET("data/2.5/forecast")
    suspend fun getFiveDayForecastByCity(
        @Query("q") cityName: String,
        @Query("units") units: String = "metric",
        @Query("appid") apiKey: String
    ): Response<ForecastResponse>

    @GET("geo/1.0/direct")
    suspend fun searchCityDirect(
        @Query("q") query: String,
        @Query("limit") limit: Int = 10,
        @Query("appid") apiKey: String
    ): Response<List<GeocodingItem>>

    @GET("geo/1.0/reverse")
    suspend fun reverseGeocode(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("limit") limit: Int = 1,
        @Query("appid") apiKey: String
    ): Response<List<GeocodingItem>>

    @GET("data/2.5/air_pollution")
    suspend fun getAirPollution(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String
    ): Response<AirPollutionResponse>
}
