package com.weatherwise.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "https://api.openweathermap.org/"
    private const val BASE_OPEN_METEO_URL = "https://api.open-meteo.com/"
    private const val BASE_OPEN_METEO_GEOCODING_URL = "https://geocoding-api.open-meteo.com/"

    private val loggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    val weatherApiService: WeatherApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApiService::class.java)
    }

    val openMeteoApiService: OpenMeteoApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_OPEN_METEO_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenMeteoApiService::class.java)
    }

    val openMeteoGeocodingApiService: OpenMeteoGeocodingApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_OPEN_METEO_GEOCODING_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenMeteoGeocodingApiService::class.java)
    }
}
