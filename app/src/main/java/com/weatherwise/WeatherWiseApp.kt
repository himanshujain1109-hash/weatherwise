package com.weatherwise

import android.app.Application
import com.weatherwise.data.database.WeatherDatabase
import com.weatherwise.data.preferences.UserPreferencesRepository
import com.weatherwise.data.repository.WeatherRepository
import com.weatherwise.location.AppLocationManager

class WeatherWiseApp : Application() {

    lateinit var database: WeatherDatabase
        private set

    lateinit var preferencesRepository: UserPreferencesRepository
        private set

    lateinit var weatherRepository: WeatherRepository
        private set

    lateinit var locationManager: AppLocationManager
        private set

    override fun onCreate() {
        super.onCreate()
        database = WeatherDatabase.getDatabase(this)
        preferencesRepository = UserPreferencesRepository(this)
        locationManager = AppLocationManager(this)
        weatherRepository = WeatherRepository(
            context = this,
            weatherDao = database.weatherDao(),
            preferencesRepository = preferencesRepository
        )
    }
}
