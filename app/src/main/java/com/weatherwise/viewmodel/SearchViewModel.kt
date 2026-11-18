package com.weatherwise.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weatherwise.data.database.SearchHistoryEntity
import com.weatherwise.data.repository.WeatherRepository
import com.weatherwise.domain.model.CitySearchResult
import com.weatherwise.utils.Resource
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class SearchViewModel(
    private val repository: WeatherRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _searchResults = MutableStateFlow<Resource<List<CitySearchResult>>>(Resource.Success(emptyList()))
    val searchResults: StateFlow<Resource<List<CitySearchResult>>> = _searchResults.asStateFlow()

    val searchHistory: StateFlow<List<SearchHistoryEntity>> = repository.getSearchHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            _query
                .debounce(350)
                .distinctUntilChanged()
                .collect { q ->
                    if (q.trim().length >= 2) {
                        performSearch(q.trim())
                    } else {
                        _searchResults.value = Resource.Success(emptyList())
                    }
                }
        }
    }

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
    }

    private fun performSearch(queryString: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _searchResults.value = Resource.Loading
            val result = repository.searchCities(queryString)
            _searchResults.value = result
        }
    }

    fun onCitySelected(city: CitySearchResult) {
        viewModelScope.launch {
            repository.addSearchHistory(
                query = _query.value,
                name = city.name,
                state = city.state,
                country = city.country,
                lat = city.latitude,
                lon = city.longitude
            )
        }
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch {
            repository.deleteSearchHistoryItem(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearSearchHistory()
        }
    }
}
