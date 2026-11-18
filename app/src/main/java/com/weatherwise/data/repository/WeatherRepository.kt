package com.weatherwise.data.repository

import android.content.Context
import android.location.Geocoder
import com.google.gson.Gson
import com.weatherwise.BuildConfig
import com.weatherwise.data.api.CurrentWeatherResponse
import com.weatherwise.data.api.ForecastResponse
import com.weatherwise.data.api.OpenMeteoApiService
import com.weatherwise.data.api.OpenMeteoGeocodingApiService
import com.weatherwise.data.api.OpenMeteoWeatherResponse
import com.weatherwise.data.api.RetrofitClient
import com.weatherwise.data.api.WeatherApiService
import com.weatherwise.data.database.CachedForecastEntity
import com.weatherwise.data.database.CachedWeatherEntity
import com.weatherwise.data.database.FavoriteCityEntity
import com.weatherwise.data.database.SearchHistoryEntity
import com.weatherwise.data.database.WeatherDao
import com.weatherwise.data.preferences.UserPreferencesRepository
import com.weatherwise.domain.model.CitySearchResult
import com.weatherwise.domain.model.DailyForecast
import com.weatherwise.domain.model.ForecastBundle
import com.weatherwise.domain.model.HourlyForecast
import com.weatherwise.domain.model.WeatherConditionType
import com.weatherwise.domain.model.WeatherData
import com.weatherwise.utils.DateFormatter
import com.weatherwise.utils.NetworkUtils
import com.weatherwise.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class WeatherRepository(
    private val context: Context,
    private val weatherDao: WeatherDao,
    private val preferencesRepository: UserPreferencesRepository,
    private val apiService: WeatherApiService = RetrofitClient.weatherApiService,
    private val openMeteoApiService: OpenMeteoApiService = RetrofitClient.openMeteoApiService,
    private val openMeteoGeocodingApiService: OpenMeteoGeocodingApiService = RetrofitClient.openMeteoGeocodingApiService,
    private val gson: Gson = Gson()
) {

    private suspend fun getEffectiveApiKey(): String {
        val userPrefs = preferencesRepository.userPreferencesFlow.first()
        return when {
            userPrefs.customApiKey.isNotBlank() -> userPrefs.customApiKey
            BuildConfig.OPENWEATHER_API_KEY.isNotBlank() -> BuildConfig.OPENWEATHER_API_KEY
            else -> ""
        }
    }

    suspend fun getCurrentWeather(
        lat: Double,
        lon: Double
    ): Resource<WeatherData> {
        val isOnline = NetworkUtils.isNetworkAvailable(context)
        val apiKey = getEffectiveApiKey()

        if (!isOnline) {
            val cached = weatherDao.getCachedWeather()
            return if (cached != null) {
                Resource.Success(mapEntityToWeatherData(cached, isOffline = true), isOffline = true)
            } else {
                Resource.Error("No internet connection and no cached weather data available.", isNetworkError = true)
            }
        }

        if (apiKey.isBlank()) {
            return fetchWeatherFromOpenMeteo(lat, lon)
        }

        return try {
            val response = apiService.getCurrentWeatherByCoordinates(
                lat = lat,
                lon = lon,
                units = "metric",
                apiKey = apiKey
            )

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val weatherData = mapResponseToWeatherData(body, isOffline = false)

                // Save to Room cache
                val entity = mapWeatherDataToEntity(weatherData)
                weatherDao.insertCachedWeather(entity)

                Resource.Success(weatherData, isOffline = false)
            } else {
                // Fallback to Open-Meteo if OpenWeatherMap fails
                fetchWeatherFromOpenMeteo(lat, lon)
            }
        } catch (e: Exception) {
            fetchWeatherFromOpenMeteo(lat, lon)
        }
    }

    suspend fun getCurrentWeatherByCity(cityName: String): Resource<WeatherData> {
        val isOnline = NetworkUtils.isNetworkAvailable(context)
        val apiKey = getEffectiveApiKey()

        if (!isOnline) {
            val cached = weatherDao.getCachedWeather()
            return if (cached != null) {
                Resource.Success(mapEntityToWeatherData(cached, isOffline = true), isOffline = true)
            } else {
                Resource.Error("No internet connection and no cached weather data available.", isNetworkError = true)
            }
        }

        if (apiKey.isBlank()) {
            return fetchWeatherFromOpenMeteoByCity(cityName)
        }

        return try {
            val response = apiService.getCurrentWeatherByCity(
                cityName = cityName,
                units = "metric",
                apiKey = apiKey
            )

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val weatherData = mapResponseToWeatherData(body, isOffline = false)

                // Save to Room cache
                val entity = mapWeatherDataToEntity(weatherData)
                weatherDao.insertCachedWeather(entity)

                Resource.Success(weatherData, isOffline = false)
            } else {
                fetchWeatherFromOpenMeteoByCity(cityName)
            }
        } catch (e: Exception) {
            fetchWeatherFromOpenMeteoByCity(cityName)
        }
    }

    suspend fun getForecast(lat: Double, lon: Double): Resource<ForecastBundle> {
        val isOnline = NetworkUtils.isNetworkAvailable(context)
        val apiKey = getEffectiveApiKey()

        if (!isOnline) {
            val cached = weatherDao.getCachedForecast()
            return if (cached != null) {
                try {
                    val cachedResponse = gson.fromJson(cached.forecastJson, ForecastResponse::class.java)
                    val bundle = parseForecastResponse(cachedResponse, isOffline = true, cached.lastUpdatedMillis)
                    Resource.Success(bundle, isOffline = true)
                } catch (e: Exception) {
                    Resource.Error("Failed to parse cached forecast data.")
                }
            } else {
                Resource.Error("No internet connection and no cached forecast data available.", isNetworkError = true)
            }
        }

        if (apiKey.isBlank()) {
            return fetchForecastFromOpenMeteo(lat, lon)
        }

        return try {
            val response = apiService.getFiveDayForecastByCoordinates(
                lat = lat,
                lon = lon,
                units = "metric",
                apiKey = apiKey
            )

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val bundle = parseForecastResponse(body, isOffline = false, System.currentTimeMillis())

                // Cache JSON in Room
                val json = gson.toJson(body)
                val entity = CachedForecastEntity(
                    id = 1,
                    cityName = bundle.cityName,
                    country = bundle.country,
                    forecastJson = json,
                    lastUpdatedMillis = System.currentTimeMillis()
                )
                weatherDao.insertCachedForecast(entity)

                Resource.Success(bundle, isOffline = false)
            } else {
                fetchForecastFromOpenMeteo(lat, lon)
            }
        } catch (e: Exception) {
            fetchForecastFromOpenMeteo(lat, lon)
        }
    }

    suspend fun searchCities(query: String): Resource<List<CitySearchResult>> {
        val trimmed = query.trim()
        if (trimmed.length < 2) {
            return Resource.Success(emptyList())
        }

        if (!NetworkUtils.isNetworkAvailable(context)) {
            return Resource.Error("Search requires an active internet connection.", isNetworkError = true)
        }

        val apiKey = getEffectiveApiKey()
        if (apiKey.isBlank()) {
            return searchCitiesFromOpenMeteo(trimmed)
        }

        return try {
            val response = apiService.searchCityDirect(
                query = trimmed,
                limit = 10,
                apiKey = apiKey
            )

            if (response.isSuccessful && response.body() != null) {
                val list = response.body()!!.map { item ->
                    CitySearchResult(
                        name = item.name,
                        state = item.state,
                        country = item.country,
                        latitude = item.lat,
                        longitude = item.lon
                    )
                }
                Resource.Success(list)
            } else {
                searchCitiesFromOpenMeteo(trimmed)
            }
        } catch (e: Exception) {
            searchCitiesFromOpenMeteo(trimmed)
        }
    }

    suspend fun reverseGeocode(lat: Double, lon: Double): CitySearchResult? {
        if (!NetworkUtils.isNetworkAvailable(context)) return null

        val apiKey = getEffectiveApiKey()
        if (apiKey.isNotBlank()) {
            try {
                val response = apiService.reverseGeocode(lat = lat, lon = lon, limit = 1, apiKey = apiKey)
                if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                    val item = response.body()!!.first()
                    return CitySearchResult(
                        name = item.name,
                        state = item.state,
                        country = item.country,
                        latitude = item.lat,
                        longitude = item.lon
                    )
                }
            } catch (e: Exception) {
                // Ignore and fallback to Geocoder
            }
        }

        val (city, country) = getCityFromCoordinates(lat, lon)
        return CitySearchResult(
            name = city,
            state = null,
            country = country,
            latitude = lat,
            longitude = lon
        )
    }

    // Open-Meteo Integration Methods
    private suspend fun fetchWeatherFromOpenMeteo(lat: Double, lon: Double): Resource<WeatherData> {
        return try {
            val response = openMeteoApiService.getWeatherAndForecast(lat = lat, lon = lon)
            if (response.isSuccessful && response.body() != null) {
                val (city, country) = getCityFromCoordinates(lat, lon)
                val weatherData = mapOpenMeteoToWeatherData(response.body()!!, city, country, lat, lon)

                // Save to Room cache
                val entity = mapWeatherDataToEntity(weatherData)
                weatherDao.insertCachedWeather(entity)

                Resource.Success(weatherData, isOffline = false)
            } else {
                val cached = weatherDao.getCachedWeather()
                if (cached != null) {
                    Resource.Success(mapEntityToWeatherData(cached, isOffline = true), isOffline = true)
                } else {
                    Resource.Error("Failed to fetch weather data from Open-Meteo.")
                }
            }
        } catch (e: Exception) {
            val cached = weatherDao.getCachedWeather()
            if (cached != null) {
                Resource.Success(mapEntityToWeatherData(cached, isOffline = true), isOffline = true)
            } else {
                Resource.Error(e.localizedMessage ?: "Network error occurred while fetching weather.", cause = e, isNetworkError = true)
            }
        }
    }

    private suspend fun fetchWeatherFromOpenMeteoByCity(cityName: String): Resource<WeatherData> {
        return try {
            val searchRes = openMeteoGeocodingApiService.searchCity(name = cityName, count = 1)
            if (searchRes.isSuccessful && !searchRes.body()?.results.isNullOrEmpty()) {
                val cityObj = searchRes.body()!!.results!!.first()
                val response = openMeteoApiService.getWeatherAndForecast(lat = cityObj.latitude, lon = cityObj.longitude)
                if (response.isSuccessful && response.body() != null) {
                    val weatherData = mapOpenMeteoToWeatherData(
                        response.body()!!,
                        cityObj.name,
                        cityObj.country ?: "",
                        cityObj.latitude,
                        cityObj.longitude
                    )

                    // Save to Room cache
                    val entity = mapWeatherDataToEntity(weatherData)
                    weatherDao.insertCachedWeather(entity)

                    Resource.Success(weatherData, isOffline = false)
                } else {
                    Resource.Error("Failed to fetch weather for $cityName from Open-Meteo.")
                }
            } else {
                Resource.Error("City '$cityName' not found.")
            }
        } catch (e: Exception) {
            val cached = weatherDao.getCachedWeather()
            if (cached != null) {
                Resource.Success(mapEntityToWeatherData(cached, isOffline = true), isOffline = true)
            } else {
                Resource.Error(e.localizedMessage ?: "Failed to find city weather.", cause = e, isNetworkError = true)
            }
        }
    }

    private suspend fun fetchForecastFromOpenMeteo(lat: Double, lon: Double): Resource<ForecastBundle> {
        return try {
            val response = openMeteoApiService.getWeatherAndForecast(lat = lat, lon = lon)
            if (response.isSuccessful && response.body() != null) {
                val (city, country) = getCityFromCoordinates(lat, lon)
                val bundle = parseOpenMeteoForecastResponse(response.body()!!, city, country)

                Resource.Success(bundle, isOffline = false)
            } else {
                val cached = weatherDao.getCachedForecast()
                if (cached != null) {
                    val cachedResponse = gson.fromJson(cached.forecastJson, ForecastResponse::class.java)
                    val bundle = parseForecastResponse(cachedResponse, isOffline = true, cached.lastUpdatedMillis)
                    Resource.Success(bundle, isOffline = true)
                } else {
                    Resource.Error("Failed to fetch forecast from Open-Meteo.")
                }
            }
        } catch (e: Exception) {
            val cached = weatherDao.getCachedForecast()
            if (cached != null) {
                try {
                    val cachedResponse = gson.fromJson(cached.forecastJson, ForecastResponse::class.java)
                    val bundle = parseForecastResponse(cachedResponse, isOffline = true, cached.lastUpdatedMillis)
                    Resource.Success(bundle, isOffline = true)
                } catch (pe: Exception) {
                    Resource.Error(e.localizedMessage ?: "Failed to parse cached forecast.")
                }
            } else {
                Resource.Error(e.localizedMessage ?: "Failed to fetch forecast.", cause = e, isNetworkError = true)
            }
        }
    }

    private suspend fun searchCitiesFromOpenMeteo(query: String): Resource<List<CitySearchResult>> {
        return try {
            val response = openMeteoGeocodingApiService.searchCity(name = query, count = 10)
            if (response.isSuccessful && response.body()?.results != null) {
                val list = response.body()!!.results!!.map { item ->
                    CitySearchResult(
                        name = item.name,
                        state = item.admin1,
                        country = item.country ?: item.countryCode ?: "",
                        latitude = item.latitude,
                        longitude = item.longitude
                    )
                }
                Resource.Success(list)
            } else {
                Resource.Success(emptyList())
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to search cities.", cause = e, isNetworkError = true)
        }
    }

    // Favorites & History
    fun getFavoriteCities(): Flow<List<FavoriteCityEntity>> = weatherDao.getAllFavoriteCities()

    fun isCityFavorite(lat: Double, lon: Double): Flow<Boolean> = weatherDao.isCityFavorite(lat, lon)

    suspend fun addFavoriteCity(name: String, state: String?, country: String, lat: Double, lon: Double) {
        val entity = FavoriteCityEntity(
            name = name,
            state = state,
            country = country,
            latitude = lat,
            longitude = lon
        )
        weatherDao.insertFavoriteCity(entity)
    }

    suspend fun removeFavoriteCity(lat: Double, lon: Double) {
        weatherDao.deleteFavoriteCityByCoords(lat, lon)
    }

    suspend fun removeFavoriteCityById(id: Long) {
        weatherDao.deleteFavoriteCityById(id)
    }

    suspend fun setDefaultCity(id: Long, name: String, lat: Double, lon: Double) {
        weatherDao.clearAllDefaultCities()
        weatherDao.setDefaultCity(id)
        preferencesRepository.setDefaultCity(name, lat, lon)
    }

    fun getSearchHistory(): Flow<List<SearchHistoryEntity>> = weatherDao.getRecentSearchHistory()

    suspend fun addSearchHistory(query: String, name: String, state: String?, country: String, lat: Double, lon: Double) {
        val entity = SearchHistoryEntity(
            query = query,
            name = name,
            state = state,
            country = country,
            latitude = lat,
            longitude = lon
        )
        weatherDao.insertSearchHistory(entity)
    }

    suspend fun deleteSearchHistoryItem(id: Long) {
        weatherDao.deleteSearchHistoryById(id)
    }

    suspend fun clearSearchHistory() {
        weatherDao.clearSearchHistory()
    }

    suspend fun clearCachedData() {
        weatherDao.clearCachedWeather()
        weatherDao.clearCachedForecast()
    }

    // Mappers & Helpers
    private fun mapOpenMeteoToWeatherData(
        body: OpenMeteoWeatherResponse,
        cityName: String,
        country: String,
        lat: Double,
        lon: Double
    ): WeatherData {
        val current = body.current
        val daily = body.daily
        val isNight = current?.isDay == 0
        val wmoCode = current?.weatherCode ?: 0
        val conditionType = WeatherConditionType.fromWmoCode(wmoCode, isNight)
        val (title, description) = WeatherConditionType.getWmoTitleAndDescription(wmoCode)

        val sunriseIso = daily?.sunrise?.firstOrNull()
        val sunsetIso = daily?.sunset?.firstOrNull()
        val sunriseTs = parseIsoToEpochSeconds(sunriseIso)
        val sunsetTs = parseIsoToEpochSeconds(sunsetIso)

        val temp = current?.temperature2m ?: 0.0
        val feelsLike = current?.apparentTemperature ?: temp
        val minTemp = daily?.tempMin?.firstOrNull() ?: temp
        val maxTemp = daily?.tempMax?.firstOrNull() ?: temp
        val uv = daily?.uvIndexMax?.firstOrNull()

        return WeatherData(
            cityName = cityName.ifBlank { "Current Location" },
            country = country,
            latitude = lat,
            longitude = lon,
            temperatureCelsius = temp,
            feelsLikeCelsius = feelsLike,
            tempMinCelsius = minTemp,
            tempMaxCelsius = maxTemp,
            conditionType = conditionType,
            conditionTitle = title,
            conditionDescription = description,
            iconCode = if (isNight) "01n" else "01d",
            humidityPercent = current?.humidity ?: 0,
            windSpeedMeterSec = current?.windSpeed10m ?: 0.0,
            windDirectionDegrees = current?.windDirection10m ?: 0,
            pressureHpa = (current?.pressureMsl ?: 1013.0).toInt(),
            visibilityMeters = 10000,
            cloudinessPercent = current?.cloudCover ?: 0,
            sunriseTimestamp = sunriseTs,
            sunsetTimestamp = sunsetTs,
            uvIndex = uv,
            observationTimestamp = System.currentTimeMillis() / 1000L,
            timezoneOffsetSeconds = body.utcOffsetSeconds ?: 0,
            isOffline = false,
            lastUpdatedMillis = System.currentTimeMillis()
        )
    }

    private fun parseOpenMeteoForecastResponse(
        body: OpenMeteoWeatherResponse,
        cityName: String,
        country: String
    ): ForecastBundle {
        val timezoneOffset = body.utcOffsetSeconds ?: 0

        // 1. Hourly Forecast (up to 24 hours)
        val hourlyTimes = body.hourly?.time ?: emptyList()
        val hourlyTemps = body.hourly?.temperature2m ?: emptyList()
        val hourlyCodes = body.hourly?.weatherCode ?: emptyList()
        val hourlyPops = body.hourly?.precipitationProbability ?: emptyList()
        val hourlyWinds = body.hourly?.windSpeed10m ?: emptyList()

        val countHourly = minOf(24, hourlyTimes.size)
        val hourlyList = (0 until countHourly).map { i ->
            val dt = parseIsoToEpochSeconds(hourlyTimes[i])
            val wmo = hourlyCodes.getOrNull(i) ?: 0
            val isNight = false
            val condType = WeatherConditionType.fromWmoCode(wmo, isNight)
            val (_, desc) = WeatherConditionType.getWmoTitleAndDescription(wmo)

            HourlyForecast(
                timestampSeconds = dt,
                timeLabel = DateFormatter.formatHour(dt, timezoneOffset),
                temperatureCelsius = hourlyTemps.getOrNull(i) ?: 0.0,
                conditionType = condType,
                conditionDescription = desc,
                iconCode = "01d",
                precipitationProbabilityPercent = hourlyPops.getOrNull(i) ?: 0,
                windSpeedMeterSec = hourlyWinds.getOrNull(i) ?: 0.0
            )
        }

        // 2. 5-Day Daily Forecast
        val dailyTimes = body.daily?.time ?: emptyList()
        val dailyMinTemps = body.daily?.tempMin ?: emptyList()
        val dailyMaxTemps = body.daily?.tempMax ?: emptyList()
        val dailyCodes = body.daily?.weatherCode ?: emptyList()
        val dailyWinds = body.daily?.windSpeedMax ?: emptyList()
        val dailyPops = body.daily?.precipitationProbabilityMax ?: emptyList()

        val countDaily = minOf(5, dailyTimes.size)
        val dailyList = (0 until countDaily).map { i ->
            val dt = parseIsoToEpochSeconds(dailyTimes[i], pattern = "yyyy-MM-dd")
            val wmo = dailyCodes.getOrNull(i) ?: 0
            val condType = WeatherConditionType.fromWmoCode(wmo, isNight = false)
            val (_, desc) = WeatherConditionType.getWmoTitleAndDescription(wmo)

            DailyForecast(
                timestampSeconds = dt,
                dayLabel = DateFormatter.formatDayName(dt, timezoneOffset),
                dateLabel = DateFormatter.formatShortDayAndDate(dt, timezoneOffset),
                minTempCelsius = dailyMinTemps.getOrNull(i) ?: 0.0,
                maxTempCelsius = dailyMaxTemps.getOrNull(i) ?: 0.0,
                conditionType = condType,
                conditionDescription = desc,
                iconCode = "01d",
                windSpeedMeterSec = dailyWinds.getOrNull(i) ?: 0.0,
                humidityPercent = 50
            )
        }

        return ForecastBundle(
            hourlyList = hourlyList,
            dailyList = dailyList,
            cityName = cityName.ifBlank { "Current Location" },
            country = country,
            isOffline = false,
            lastUpdatedMillis = System.currentTimeMillis()
        )
    }

    private fun parseIsoToEpochSeconds(isoString: String?, pattern: String = "yyyy-MM-dd'T'HH:mm"): Long {
        if (isoString.isNullOrBlank()) return System.currentTimeMillis() / 1000L
        return try {
            val sdf = SimpleDateFormat(pattern, Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val date = sdf.parse(isoString)
            (date?.time ?: System.currentTimeMillis()) / 1000L
        } catch (e: Exception) {
            System.currentTimeMillis() / 1000L
        }
    }

    private fun getCityFromCoordinates(lat: Double, lon: Double): Pair<String, String> {
        return try {
            @Suppress("DEPRECATION")
            val geocoder = Geocoder(context, Locale.getDefault())
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            if (!addresses.isNullOrEmpty()) {
                val addr = addresses.first()
                val city = addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: "Selected Location"
                val country = addr.countryName ?: addr.countryCode ?: ""
                city to country
            } else {
                "Selected Location" to ""
            }
        } catch (e: Exception) {
            "Selected Location" to ""
        }
    }

    private fun mapResponseToWeatherData(response: CurrentWeatherResponse, isOffline: Boolean): WeatherData {
        val weatherItem = response.weather?.firstOrNull()
        val conditionId = weatherItem?.id ?: 800
        val iconCode = weatherItem?.icon ?: "01d"
        val conditionType = WeatherConditionType.fromOpenWeather(conditionId, iconCode)

        return WeatherData(
            cityName = response.name ?: "Unknown Location",
            country = response.sys?.country ?: "",
            latitude = response.coord?.lat ?: 0.0,
            longitude = response.coord?.lon ?: 0.0,
            temperatureCelsius = response.main?.temp ?: 0.0,
            feelsLikeCelsius = response.main?.feelsLike ?: (response.main?.temp ?: 0.0),
            tempMinCelsius = response.main?.tempMin ?: 0.0,
            tempMaxCelsius = response.main?.tempMax ?: 0.0,
            conditionType = conditionType,
            conditionTitle = weatherItem?.main ?: "Clear",
            conditionDescription = weatherItem?.description?.replaceFirstChar { it.uppercase() } ?: "Clear sky",
            iconCode = iconCode,
            humidityPercent = response.main?.humidity ?: 0,
            windSpeedMeterSec = response.wind?.speed ?: 0.0,
            windDirectionDegrees = response.wind?.deg ?: 0,
            pressureHpa = response.main?.pressure ?: 1013,
            visibilityMeters = response.visibility ?: 10000,
            cloudinessPercent = response.clouds?.all ?: 0,
            sunriseTimestamp = response.sys?.sunrise ?: 0L,
            sunsetTimestamp = response.sys?.sunset ?: 0L,
            uvIndex = null,
            observationTimestamp = response.dt ?: (System.currentTimeMillis() / 1000L),
            timezoneOffsetSeconds = response.timezone ?: 0,
            isOffline = isOffline,
            lastUpdatedMillis = System.currentTimeMillis()
        )
    }

    private fun mapEntityToWeatherData(entity: CachedWeatherEntity, isOffline: Boolean): WeatherData {
        val conditionType = WeatherConditionType.fromOpenWeather(entity.conditionId, entity.iconCode)
        return WeatherData(
            cityName = entity.cityName,
            country = entity.country,
            latitude = entity.latitude,
            longitude = entity.longitude,
            temperatureCelsius = entity.temperatureCelsius,
            feelsLikeCelsius = entity.feelsLikeCelsius,
            tempMinCelsius = entity.tempMinCelsius,
            tempMaxCelsius = entity.tempMaxCelsius,
            conditionType = conditionType,
            conditionTitle = entity.conditionTitle,
            conditionDescription = entity.conditionDescription,
            iconCode = entity.iconCode,
            humidityPercent = entity.humidityPercent,
            windSpeedMeterSec = entity.windSpeedMeterSec,
            windDirectionDegrees = entity.windDirectionDegrees,
            pressureHpa = entity.pressureHpa,
            visibilityMeters = entity.visibilityMeters,
            cloudinessPercent = entity.cloudinessPercent,
            sunriseTimestamp = entity.sunriseTimestamp,
            sunsetTimestamp = entity.sunsetTimestamp,
            uvIndex = entity.uvIndex,
            observationTimestamp = entity.observationTimestamp,
            timezoneOffsetSeconds = entity.timezoneOffsetSeconds,
            isOffline = isOffline,
            lastUpdatedMillis = entity.lastUpdatedMillis
        )
    }

    private fun mapWeatherDataToEntity(data: WeatherData): CachedWeatherEntity {
        return CachedWeatherEntity(
            id = 1,
            cityName = data.cityName,
            country = data.country,
            latitude = data.latitude,
            longitude = data.longitude,
            temperatureCelsius = data.temperatureCelsius,
            feelsLikeCelsius = data.feelsLikeCelsius,
            tempMinCelsius = data.tempMinCelsius,
            tempMaxCelsius = data.tempMaxCelsius,
            conditionId = when (data.conditionType) {
                WeatherConditionType.CLEAR_DAY, WeatherConditionType.CLEAR_NIGHT -> 800
                WeatherConditionType.CLOUDY_DAY, WeatherConditionType.CLOUDY_NIGHT -> 802
                WeatherConditionType.RAINY -> 500
                WeatherConditionType.DRIZZLE -> 300
                WeatherConditionType.SNOWY -> 600
                WeatherConditionType.THUNDERSTORM -> 200
                WeatherConditionType.ATMOSPHERE -> 701
            },
            conditionTitle = data.conditionTitle,
            conditionDescription = data.conditionDescription,
            iconCode = data.iconCode,
            humidityPercent = data.humidityPercent,
            windSpeedMeterSec = data.windSpeedMeterSec,
            windDirectionDegrees = data.windDirectionDegrees,
            pressureHpa = data.pressureHpa,
            visibilityMeters = data.visibilityMeters,
            cloudinessPercent = data.cloudinessPercent,
            sunriseTimestamp = data.sunriseTimestamp,
            sunsetTimestamp = data.sunsetTimestamp,
            uvIndex = data.uvIndex,
            observationTimestamp = data.observationTimestamp,
            timezoneOffsetSeconds = data.timezoneOffsetSeconds,
            lastUpdatedMillis = data.lastUpdatedMillis
        )
    }

    private fun parseForecastResponse(
        response: ForecastResponse,
        isOffline: Boolean,
        lastUpdatedMillis: Long
    ): ForecastBundle {
        val list = response.list ?: emptyList()
        val timezoneOffset = response.city?.timezone ?: 0

        // 1. Hourly Forecast: next 8 slots (24 hours)
        val hourlyList = list.take(8).map { item ->
            val dt = item.dt ?: 0L
            val weatherItem = item.weather?.firstOrNull()
            val condId = weatherItem?.id ?: 800
            val icon = weatherItem?.icon ?: "01d"
            val condType = WeatherConditionType.fromOpenWeather(condId, icon)

            HourlyForecast(
                timestampSeconds = dt,
                timeLabel = DateFormatter.formatHour(dt, timezoneOffset),
                temperatureCelsius = item.main?.temp ?: 0.0,
                conditionType = condType,
                conditionDescription = weatherItem?.description ?: "",
                iconCode = icon,
                precipitationProbabilityPercent = ((item.pop ?: 0.0) * 100).toInt(),
                windSpeedMeterSec = item.wind?.speed ?: 0.0
            )
        }

        // 2. 5-Day Daily Forecast: Group by calendar day in the local timezone
        val dayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getTimeZone(String.format(Locale.US, "GMT%+d", timezoneOffset / 3600))
        }

        val groupedByDay = list.groupBy { item ->
            dayFormat.format(Date((item.dt ?: 0L) * 1000L))
        }

        val dailyList = groupedByDay.entries.map { (_, items) ->
            val first = items.first()
            val dt = first.dt ?: 0L
            val minTemp = items.minOfOrNull { it.main?.tempMin ?: 0.0 } ?: 0.0
            val maxTemp = items.maxOfOrNull { it.main?.tempMax ?: 0.0 } ?: 0.0

            val representativeItem = items.find {
                it.dtTxt?.contains("12:00:00") == true || it.dtTxt?.contains("15:00:00") == true
            } ?: items[items.size / 2]

            val weatherItem = representativeItem.weather?.firstOrNull()
            val condId = weatherItem?.id ?: 800
            val icon = weatherItem?.icon ?: "01d"
            val condType = WeatherConditionType.fromOpenWeather(condId, icon)
            val avgWind = items.map { it.wind?.speed ?: 0.0 }.average()
            val avgHumidity = items.map { it.main?.humidity ?: 0 }.average().toInt()

            DailyForecast(
                timestampSeconds = dt,
                dayLabel = DateFormatter.formatDayName(dt, timezoneOffset),
                dateLabel = DateFormatter.formatShortDayAndDate(dt, timezoneOffset),
                minTempCelsius = minTemp,
                maxTempCelsius = maxTemp,
                conditionType = condType,
                conditionDescription = weatherItem?.description?.replaceFirstChar { it.uppercase() } ?: "Clear",
                iconCode = icon,
                windSpeedMeterSec = avgWind,
                humidityPercent = avgHumidity
            )
        }.take(5)

        return ForecastBundle(
            hourlyList = hourlyList,
            dailyList = dailyList,
            cityName = response.city?.name ?: "Unknown",
            country = response.city?.country ?: "",
            isOffline = isOffline,
            lastUpdatedMillis = lastUpdatedMillis
        )
    }

    private suspend fun <T> handleApiError(
        response: Response<*>,
        fallback: (suspend () -> Resource<T>?)? = null
    ): Resource<T> {
        val fallbackResult = fallback?.invoke()
        if (fallbackResult != null) {
            return fallbackResult
        }

        val code = response.code()
        val errorMsg = when (code) {
            401 -> "Invalid API key. Please check your OpenWeatherMap API key in Settings."
            404 -> "City or location not found. Please try another search term."
            429 -> "API rate limit reached. Please wait a moment and try again."
            in 500..599 -> "Weather service server error ($code). Please try again shortly."
            else -> "Weather service error ($code: ${response.message()})."
        }
        return Resource.Error(errorMsg)
    }
}
