package com.weatherwise.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weatherwise.data.preferences.UserPreferences
import com.weatherwise.data.preferences.UserPreferencesRepository
import com.weatherwise.data.repository.WeatherRepository
import com.weatherwise.domain.model.ForecastBundle
import com.weatherwise.domain.model.WeatherData
import com.weatherwise.location.AppLocationManager
import com.weatherwise.location.LocationResult
import com.weatherwise.utils.Resource
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ActiveLocation(
    val name: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isGpsBased: Boolean = true
)

class WeatherViewModel(
    private val repository: WeatherRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val locationManager: AppLocationManager
) : ViewModel() {

    private val _currentWeather = MutableStateFlow<Resource<WeatherData>>(Resource.Loading)
    val currentWeather: StateFlow<Resource<WeatherData>> = _currentWeather.asStateFlow()

    private val _forecast = MutableStateFlow<Resource<ForecastBundle>>(Resource.Loading)
    val forecast: StateFlow<Resource<ForecastBundle>> = _forecast.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _activeLocation = MutableStateFlow(ActiveLocation())
    val activeLocation: StateFlow<ActiveLocation> = _activeLocation.asStateFlow()

    private val _locationMessage = MutableStateFlow<String?>(null)
    val locationMessage: StateFlow<String?> = _locationMessage.asStateFlow()

    val userPreferences: StateFlow<UserPreferences?> = preferencesRepository.userPreferencesFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val isCurrentCityFavorite: StateFlow<Boolean> = combine(
        _currentWeather,
        repository.getFavoriteCities()
    ) { weatherResource, favorites ->
        if (weatherResource is Resource.Success) {
            val weather = weatherResource.data
            favorites.any { fav ->
                (fav.latitude == weather.latitude && fav.longitude == weather.longitude) ||
                        fav.name.equals(weather.cityName, ignoreCase = true)
            }
        } else false
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private var loadJob: Job? = null

    init {
        initWeather()
    }

    private fun initWeather() {
        viewModelScope.launch {
            val prefs = preferencesRepository.userPreferencesFlow.first()
            if (prefs.defaultCityName != null && prefs.defaultCityLat != null && prefs.defaultCityLon != null) {
                loadWeatherForCoordinates(
                    lat = prefs.defaultCityLat,
                    lon = prefs.defaultCityLon,
                    cityName = prefs.defaultCityName,
                    isGps = false
                )
            } else if (prefs.locationWeatherEnabled && locationManager.hasLocationPermission()) {
                fetchLocationAndLoadWeather()
            } else {
                // Default fallback to London or cached weather
                loadWeatherForCity("London")
            }
        }
    }

    fun requestLocationWeather() {
        fetchLocationAndLoadWeather()
    }

    private fun fetchLocationAndLoadWeather() {
        viewModelScope.launch {
            _isRefreshing.value = true
            when (val locResult = locationManager.getCurrentLocation()) {
                is LocationResult.Success -> {
                    val lat = locResult.location.latitude
                    val lon = locResult.location.longitude
                    loadWeatherForCoordinates(lat, lon, cityName = null, isGps = true)
                }
                is LocationResult.PermissionDenied -> {
                    _isRefreshing.value = false
                    _locationMessage.value = "Location permission is needed to detect current location."
                    // Attempt cached weather
                    loadWeatherFromCoordinatesOrCache()
                }
                is LocationResult.GpsDisabled -> {
                    _isRefreshing.value = false
                    _locationMessage.value = "GPS / Location services are disabled. Please enable them or search manually."
                    loadWeatherFromCoordinatesOrCache()
                }
                is LocationResult.Error -> {
                    _isRefreshing.value = false
                    _locationMessage.value = locResult.message
                    loadWeatherFromCoordinatesOrCache()
                }
            }
        }
    }

    private fun loadWeatherFromCoordinatesOrCache() {
        val currentLoc = _activeLocation.value
        if (currentLoc.latitude != null && currentLoc.longitude != null) {
            loadWeatherForCoordinates(currentLoc.latitude, currentLoc.longitude, currentLoc.name, currentLoc.isGpsBased)
        } else if (!currentLoc.name.isNullOrBlank()) {
            loadWeatherForCity(currentLoc.name)
        } else {
            loadWeatherForCity("London")
        }
    }

    fun loadWeatherForCoordinates(lat: Double, lon: Double, cityName: String? = null, isGps: Boolean = false) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _isRefreshing.value = true
            _activeLocation.value = ActiveLocation(cityName, lat, lon, isGps)

            val weatherRes = repository.getCurrentWeather(lat, lon)
            _currentWeather.value = weatherRes

            val forecastRes = repository.getForecast(lat, lon)
            _forecast.value = forecastRes

            _isRefreshing.value = false
        }
    }

    fun loadWeatherForCity(cityName: String) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _isRefreshing.value = true
            _activeLocation.value = ActiveLocation(name = cityName, isGpsBased = false)

            val weatherRes = repository.getCurrentWeatherByCity(cityName)
            _currentWeather.value = weatherRes

            if (weatherRes is Resource.Success) {
                val data = weatherRes.data
                _activeLocation.value = ActiveLocation(data.cityName, data.latitude, data.longitude, false)
                val forecastRes = repository.getForecast(data.latitude, data.longitude)
                _forecast.value = forecastRes
            } else {
                _forecast.value = Resource.Error((weatherRes as? Resource.Error)?.message ?: "Failed to load forecast")
            }

            _isRefreshing.value = false
        }
    }

    fun refreshWeather() {
        val loc = _activeLocation.value
        if (loc.isGpsBased && locationManager.hasLocationPermission()) {
            fetchLocationAndLoadWeather()
        } else if (loc.latitude != null && loc.longitude != null) {
            loadWeatherForCoordinates(loc.latitude, loc.longitude, loc.name, loc.isGpsBased)
        } else if (!loc.name.isNullOrBlank()) {
            loadWeatherForCity(loc.name)
        } else {
            fetchLocationAndLoadWeather()
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            val weatherRes = _currentWeather.value
            if (weatherRes is Resource.Success) {
                val data = weatherRes.data
                val isFav = isCurrentCityFavorite.value
                if (isFav) {
                    repository.removeFavoriteCity(data.latitude, data.longitude)
                } else {
                    repository.addFavoriteCity(
                        name = data.cityName,
                        state = null,
                        country = data.country,
                        lat = data.latitude,
                        lon = data.longitude
                    )
                }
            }
        }
    }

    fun dismissLocationMessage() {
        _locationMessage.value = null
    }
}
