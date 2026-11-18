package com.weatherwise.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weatherwise.data.database.FavoriteCityEntity
import com.weatherwise.data.repository.WeatherRepository
import com.weatherwise.domain.model.WeatherData
import com.weatherwise.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val repository: WeatherRepository
) : ViewModel() {

    val favoriteCities: StateFlow<List<FavoriteCityEntity>> = repository.getFavoriteCities()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _cityWeatherMap = MutableStateFlow<Map<Long, WeatherData>>(emptyMap())
    val cityWeatherMap: StateFlow<Map<Long, WeatherData>> = _cityWeatherMap.asStateFlow()

    fun removeFavorite(city: FavoriteCityEntity) {
        viewModelScope.launch {
            repository.removeFavoriteCityById(city.id)
        }
    }

    fun setDefault(city: FavoriteCityEntity) {
        viewModelScope.launch {
            repository.setDefaultCity(
                id = city.id,
                name = city.name,
                lat = city.latitude,
                lon = city.longitude
            )
        }
    }

    fun loadWeatherForFavorite(city: FavoriteCityEntity) {
        viewModelScope.launch {
            val res = repository.getCurrentWeather(city.latitude, city.longitude)
            if (res is Resource.Success) {
                _cityWeatherMap.value = _cityWeatherMap.value + (city.id to res.data)
            }
        }
    }
}
