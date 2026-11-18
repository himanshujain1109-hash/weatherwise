package com.weatherwise.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.weatherwise.domain.model.WeatherData
import com.weatherwise.utils.DateFormatter
import com.weatherwise.utils.UnitConverter
import com.weatherwise.utils.WindSpeedUnit

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdditionalWeatherDetailsSection(
    weather: WeatherData,
    windSpeedUnit: WindSpeedUnit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Weather Details",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            maxItemsInEachRow = 2,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Humidity
            DetailCard(
                icon = Icons.Default.WaterDrop,
                iconTint = Color(0xFF29B6F6),
                title = "Humidity",
                value = "${weather.humidityPercent}%",
                subtitle = if (weather.humidityPercent > 65) "High humidity" else "Comfortable",
                modifier = Modifier.weight(1f)
            )

            // Wind
            val windDirection = UnitConverter.degreesToCardinalDirection(weather.windDirectionDegrees)
            DetailCard(
                icon = Icons.Default.Air,
                iconTint = Color(0xFF26A69A),
                title = "Wind",
                value = UnitConverter.formatWindSpeed(weather.windSpeedMeterSec, windSpeedUnit),
                subtitle = "$windDirection (${weather.windDirectionDegrees}°)",
                extra = {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = "Wind Direction",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(16.dp)
                            .rotate(weather.windDirectionDegrees.toFloat())
                    )
                },
                modifier = Modifier.weight(1f)
            )

            // Pressure
            DetailCard(
                icon = Icons.Default.Compress,
                iconTint = Color(0xFFAB47BC),
                title = "Pressure",
                value = UnitConverter.formatPressure(weather.pressureHpa),
                subtitle = if (weather.pressureHpa >= 1013) "High pressure" else "Low pressure",
                modifier = Modifier.weight(1f)
            )

            // Visibility
            DetailCard(
                icon = Icons.Default.Visibility,
                iconTint = Color(0xFFFFA726),
                title = "Visibility",
                value = UnitConverter.formatVisibility(weather.visibilityMeters),
                subtitle = if (weather.visibilityMeters >= 10000) "Clear visibility" else "Reduced visibility",
                modifier = Modifier.weight(1f)
            )

            // Cloudiness
            DetailCard(
                icon = Icons.Default.Cloud,
                iconTint = Color(0xFF78909C),
                title = "Cloudiness",
                value = "${weather.cloudinessPercent}%",
                subtitle = when {
                    weather.cloudinessPercent < 20 -> "Clear sky"
                    weather.cloudinessPercent < 60 -> "Partly cloudy"
                    else -> "Overcast"
                },
                modifier = Modifier.weight(1f)
            )

            // Sunrise & Sunset
            DetailCard(
                icon = Icons.Default.WbSunny,
                iconTint = Color(0xFFFFB300),
                title = "Sunrise & Sunset",
                value = DateFormatter.formatTimeOnly(weather.sunriseTimestamp, weather.timezoneOffsetSeconds),
                subtitle = "Sunset: ${DateFormatter.formatTimeOnly(weather.sunsetTimestamp, weather.timezoneOffsetSeconds)}",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun DetailCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    extra: @Composable (() -> Unit)? = null
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                extra?.invoke()
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
