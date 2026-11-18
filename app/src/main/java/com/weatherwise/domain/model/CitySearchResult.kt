package com.weatherwise.domain.model

data class CitySearchResult(
    val name: String,
    val state: String?,
    val country: String,
    val latitude: Double,
    val longitude: Double
) {
    val displayLocation: String
        get() = if (!state.isNullOrBlank()) {
            "$name, $state, $country"
        } else {
            "$name, $country"
        }
}
