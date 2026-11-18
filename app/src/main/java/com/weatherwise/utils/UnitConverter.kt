package com.weatherwise.utils

import java.util.Locale
import kotlin.math.roundToInt

enum class TemperatureUnit(val symbol: String, val displayName: String) {
    CELSIUS("°C", "Celsius (°C)"),
    FAHRENHEIT("°F", "Fahrenheit (°F)")
}

enum class WindSpeedUnit(val symbol: String, val displayName: String) {
    KM_H("km/h", "Kilometers per hour (km/h)"),
    M_S("m/s", "Meters per second (m/s)"),
    MPH("mph", "Miles per hour (mph)")
}

object UnitConverter {

    /**
     * Converts Celsius temperature to the requested unit.
     */
    fun formatTemperature(tempCelsius: Double, unit: TemperatureUnit): String {
        val converted = when (unit) {
            TemperatureUnit.CELSIUS -> tempCelsius
            TemperatureUnit.FAHRENHEIT -> (tempCelsius * 9.0 / 5.0) + 32.0
        }
        return "${converted.roundToInt()}${unit.symbol}"
    }

    fun getTemperatureValue(tempCelsius: Double, unit: TemperatureUnit): Double {
        return when (unit) {
            TemperatureUnit.CELSIUS -> tempCelsius
            TemperatureUnit.FAHRENHEIT -> (tempCelsius * 9.0 / 5.0) + 32.0
        }
    }

    /**
     * Converts wind speed from m/s (OpenWeather default in metric) to the target unit.
     */
    fun formatWindSpeed(speedMeterPerSec: Double, unit: WindSpeedUnit): String {
        val converted = when (unit) {
            WindSpeedUnit.M_S -> speedMeterPerSec
            WindSpeedUnit.KM_H -> speedMeterPerSec * 3.6
            WindSpeedUnit.MPH -> speedMeterPerSec * 2.23694
        }
        return String.format(Locale.getDefault(), "%.1f %s", converted, unit.symbol)
    }

    /**
     * Converts meteorological degrees (0° - 360°) to cardinal compass direction.
     */
    fun degreesToCardinalDirection(degrees: Int): String {
        val directions = arrayOf(
            "N", "NNE", "NE", "ENE",
            "E", "ESE", "SE", "SSE",
            "S", "SSW", "SW", "WSW",
            "W", "WNW", "NW", "NNW"
        )
        val index = ((degrees / 22.5) + 0.5).toInt() % 16
        return directions[index]
    }

    /**
     * Formats visibility in meters to kilometers.
     */
    fun formatVisibility(visibilityMeters: Int): String {
        val km = visibilityMeters / 1000.0
        return String.format(Locale.getDefault(), "%.1f km", km)
    }

    /**
     * Formats atmospheric pressure in hPa.
     */
    fun formatPressure(hPa: Int): String {
        return "$hPa hPa"
    }
}
