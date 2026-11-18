package com.weatherwise.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.weatherwise.domain.model.WeatherConditionType
import com.weatherwise.ui.theme.WeatherRainBlue
import com.weatherwise.ui.theme.WeatherSnowWhite
import com.weatherwise.ui.theme.WeatherStormPurple
import com.weatherwise.ui.theme.WeatherSunGold

@Composable
fun WeatherConditionIcon(
    conditionType: WeatherConditionType,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    tint: Color? = null
) {
    val (icon, defaultTint) = when (conditionType) {
        WeatherConditionType.CLEAR_DAY -> Icons.Default.WbSunny to WeatherSunGold
        WeatherConditionType.CLEAR_NIGHT -> Icons.Default.NightsStay to Color(0xFFE2E8F0)
        WeatherConditionType.CLOUDY_DAY -> Icons.Default.CloudQueue to Color(0xFF90A4AE)
        WeatherConditionType.CLOUDY_NIGHT -> Icons.Default.Cloud to Color(0xFF78909C)
        WeatherConditionType.RAINY -> Icons.Default.WaterDrop to WeatherRainBlue
        WeatherConditionType.DRIZZLE -> Icons.Default.Grain to Color(0xFF81D4FA)
        WeatherConditionType.SNOWY -> Icons.Default.AcUnit to Color(0xFFB2EBF2)
        WeatherConditionType.THUNDERSTORM -> Icons.Default.Thunderstorm to WeatherStormPurple
        WeatherConditionType.ATMOSPHERE -> Icons.Default.Air to Color(0xFFB0BEC5)
    }

    Icon(
        imageVector = icon,
        contentDescription = conditionType.name,
        tint = tint ?: defaultTint,
        modifier = modifier.size(size)
    )
}
