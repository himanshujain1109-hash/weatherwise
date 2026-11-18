package com.weatherwise.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Light Mode Colors
val LightPrimary = Color(0xFF1976D2)
val LightOnPrimary = Color(0xFFFFFFFF)
val LightPrimaryContainer = Color(0xFFD1E4FF)
val LightOnPrimaryContainer = Color(0xFF001D36)

val LightSecondary = Color(0xFF26A69A)
val LightOnSecondary = Color(0xFFFFFFFF)
val LightSecondaryContainer = Color(0xFFB2DFDB)
val LightOnSecondaryContainer = Color(0xFF00201B)

val LightBackground = Color(0xFFF4F8FC)
val LightOnBackground = Color(0xFF17212B)
val LightSurface = Color(0xFFFFFFFF)
val LightOnSurface = Color(0xFF17212B)
val LightSurfaceVariant = Color(0xFFE5EDF5)
val LightOnSurfaceVariant = Color(0xFF424E5B)
val LightOutline = Color(0xFFC4D1DF)

// Dark Mode Colors
val DarkPrimary = Color(0xFF4FC3F7)
val DarkOnPrimary = Color(0xFF003548)
val DarkPrimaryContainer = Color(0xFF004D68)
val DarkOnPrimaryContainer = Color(0xFFBCE9FF)

val DarkSecondary = Color(0xFF80CBC4)
val DarkOnSecondary = Color(0xFF003732)
val DarkSecondaryContainer = Color(0xFF005049)
val DarkOnSecondaryContainer = Color(0xFFA1F2E8)

val DarkBackground = Color(0xFF101820)
val DarkOnBackground = Color(0xFFFFFFFF)
val DarkSurface = Color(0xFF1C2733)
val DarkOnSurface = Color(0xFFFFFFFF)
val DarkSurfaceVariant = Color(0xFF253342)
val DarkOnSurfaceVariant = Color(0xFFB9C8D6)
val DarkOutline = Color(0xFF3B4C5D)

// Weather Accent Colors
val WeatherSunGold = Color(0xFFFFB300)
val WeatherSunYellow = Color(0xFFFFD54F)
val WeatherRainBlue = Color(0xFF42A5F5)
val WeatherStormPurple = Color(0xFF7E57C2)
val WeatherSnowWhite = Color(0xFFE0F7FA)

// Weather Background Gradients
val SunnyGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF2196F3), Color(0xFFFFB74D))
)
val NightGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF0D1B2A), Color(0xFF1B263B), Color(0xFF415A77))
)
val CloudyGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF455A64), Color(0xFF78909C), Color(0xFF90A4AE))
)
val RainyGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF263238), Color(0xFF37474F), Color(0xFF455A64))
)
val SnowyGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF607D8B), Color(0xFF90A4AE), Color(0xFFCFD8DC))
)
val StormGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF1A1A24), Color(0xFF2C243B), Color(0xFF311B92))
)
