package com.weatherwise

import com.weatherwise.domain.model.WeatherConditionType
import com.weatherwise.utils.TemperatureUnit
import com.weatherwise.utils.UnitConverter
import com.weatherwise.utils.WindSpeedUnit
import org.junit.Assert.assertEquals
import org.junit.Test

class UnitConverterTest {

    @Test
    fun testCelsiusToFahrenheit() {
        assertEquals("32°F", UnitConverter.formatTemperature(0.0, TemperatureUnit.FAHRENHEIT))
        assertEquals("212°F", UnitConverter.formatTemperature(100.0, TemperatureUnit.FAHRENHEIT))
        assertEquals("-40°F", UnitConverter.formatTemperature(-40.0, TemperatureUnit.FAHRENHEIT))
        assertEquals("25°C", UnitConverter.formatTemperature(25.0, TemperatureUnit.CELSIUS))
    }

    @Test
    fun testWindSpeedConversion() {
        val speedMps = 10.0 // 10 m/s = 36 km/h
        assertEquals("36.0 km/h", UnitConverter.formatWindSpeed(speedMps, WindSpeedUnit.KM_H))
        assertEquals("10.0 m/s", UnitConverter.formatWindSpeed(speedMps, WindSpeedUnit.M_S))
    }

    @Test
    fun testCardinalDirectionConversion() {
        assertEquals("N", UnitConverter.degreesToCardinalDirection(0))
        assertEquals("E", UnitConverter.degreesToCardinalDirection(90))
        assertEquals("S", UnitConverter.degreesToCardinalDirection(180))
        assertEquals("W", UnitConverter.degreesToCardinalDirection(270))
        assertEquals("NE", UnitConverter.degreesToCardinalDirection(45))
        assertEquals("SW", UnitConverter.degreesToCardinalDirection(225))
    }

    @Test
    fun testVisibilityAndPressureFormatting() {
        assertEquals("10.0 km", UnitConverter.formatVisibility(10000))
        assertEquals("2.5 km", UnitConverter.formatVisibility(2500))
        assertEquals("1013 hPa", UnitConverter.formatPressure(1013))
    }

    @Test
    fun testWeatherConditionClassification() {
        // 200 is thunderstorm
        assertEquals(
            WeatherConditionType.THUNDERSTORM,
            WeatherConditionType.fromOpenWeather(200, "11d")
        )
        // 500 is rain
        assertEquals(
            WeatherConditionType.RAINY,
            WeatherConditionType.fromOpenWeather(500, "10d")
        )
        // 600 is snow
        assertEquals(
            WeatherConditionType.SNOWY,
            WeatherConditionType.fromOpenWeather(600, "13d")
        )
        // 800 is clear day or night depending on icon
        assertEquals(
            WeatherConditionType.CLEAR_DAY,
            WeatherConditionType.fromOpenWeather(800, "01d")
        )
        assertEquals(
            WeatherConditionType.CLEAR_NIGHT,
            WeatherConditionType.fromOpenWeather(800, "01n")
        )
    }
}
