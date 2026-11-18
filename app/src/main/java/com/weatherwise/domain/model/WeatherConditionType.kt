package com.weatherwise.domain.model

enum class WeatherConditionType {
    CLEAR_DAY,
    CLEAR_NIGHT,
    CLOUDY_DAY,
    CLOUDY_NIGHT,
    RAINY,
    DRIZZLE,
    SNOWY,
    THUNDERSTORM,
    ATMOSPHERE; // Mist, Fog, Haze, Dust

    companion object {
        fun fromOpenWeather(conditionId: Int, iconCode: String): WeatherConditionType {
            val isNight = iconCode.endsWith("n")
            return when (conditionId) {
                in 200..232 -> THUNDERSTORM
                in 300..321 -> DRIZZLE
                in 500..531 -> RAINY
                in 600..622 -> SNOWY
                in 701..781 -> ATMOSPHERE
                800 -> if (isNight) CLEAR_NIGHT else CLEAR_DAY
                in 801..804 -> if (isNight) CLOUDY_NIGHT else CLOUDY_DAY
                else -> if (isNight) CLEAR_NIGHT else CLEAR_DAY
            }
        }

        fun fromWmoCode(wmoCode: Int, isNight: Boolean): WeatherConditionType {
            return when (wmoCode) {
                0, 1 -> if (isNight) CLEAR_NIGHT else CLEAR_DAY
                2, 3 -> if (isNight) CLOUDY_NIGHT else CLOUDY_DAY
                45, 48 -> ATMOSPHERE
                51, 53, 55, 56, 57 -> DRIZZLE
                61, 63, 65, 66, 67, 80, 81, 82 -> RAINY
                71, 73, 75, 77, 85, 86 -> SNOWY
                95, 96, 99 -> THUNDERSTORM
                else -> if (isNight) CLEAR_NIGHT else CLEAR_DAY
            }
        }

        fun getWmoTitleAndDescription(wmoCode: Int): Pair<String, String> {
            return when (wmoCode) {
                0 -> "Clear" to "Clear sky"
                1 -> "Mainly Clear" to "Mainly clear sky"
                2 -> "Partly Cloudy" to "Partly cloudy"
                3 -> "Overcast" to "Overcast clouds"
                45, 48 -> "Fog" to "Fog and depositing rime fog"
                51, 53, 55 -> "Drizzle" to "Drizzle precipitation"
                56, 57 -> "Freezing Drizzle" to "Freezing drizzle"
                61, 63, 65 -> "Rain" to "Rain showers"
                66, 67 -> "Freezing Rain" to "Freezing rain"
                71, 73, 75, 77 -> "Snow" to "Snowfall"
                80, 81, 82 -> "Rain Showers" to "Heavy rain showers"
                85, 86 -> "Snow Showers" to "Snow showers"
                95, 96, 99 -> "Thunderstorm" to "Thunderstorm"
                else -> "Clear" to "Clear sky"
            }
        }
    }
}
