package com.weatherwise.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.weatherwise.data.database.FavoriteCityEntity
import com.weatherwise.ui.components.WeatherConditionIcon
import com.weatherwise.utils.TemperatureUnit
import com.weatherwise.utils.UnitConverter
import com.weatherwise.viewmodel.FavoritesViewModel
import com.weatherwise.viewmodel.WeatherViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    favoritesViewModel: FavoritesViewModel,
    weatherViewModel: WeatherViewModel,
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val favoriteCities by favoritesViewModel.favoriteCities.collectAsState()
    val weatherMap by favoritesViewModel.cityWeatherMap.collectAsState()
    val userPrefs by weatherViewModel.userPreferences.collectAsState()
    val tempUnit = userPrefs?.temperatureUnit ?: TemperatureUnit.CELSIUS

    // Fetch previews for favorites
    LaunchedEffect(favoriteCities) {
        favoriteCities.forEach { city ->
            favoritesViewModel.loadWeatherForFavorite(city)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Saved Cities",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        if (favoriteCities.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "No Favorites",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No saved cities yet",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap the heart icon on any city's weather card on the Home screen to save it here for fast access.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(favoriteCities) { city ->
                    val previewWeather = weatherMap[city.id]

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                weatherViewModel.loadWeatherForCoordinates(
                                    lat = city.latitude,
                                    lon = city.longitude,
                                    cityName = city.name,
                                    isGps = false
                                )
                                onNavigateToHome()
                            },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // City Info & Default Chip
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = city.name,
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (city.isDefault) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        AssistChip(
                                            onClick = { },
                                            label = { Text("Default") },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.Home,
                                                    contentDescription = "Default City",
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = AssistChipDefaults.assistChipColors(
                                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                                labelColor = MaterialTheme.colorScheme.primary
                                            )
                                        )
                                    }
                                }

                                Text(
                                    text = city.country,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                if (previewWeather != null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = previewWeather.conditionTitle,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            // Temperature preview & Condition Icon
                            if (previewWeather != null) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    WeatherConditionIcon(
                                        conditionType = previewWeather.conditionType,
                                        size = 32.dp
                                    )
                                    Text(
                                        text = UnitConverter.formatTemperature(
                                            previewWeather.temperatureCelsius,
                                            tempUnit
                                        ),
                                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            // Actions: Set as default & Delete
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { favoritesViewModel.setDefault(city) }
                                ) {
                                    Icon(
                                        imageVector = if (city.isDefault) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                        contentDescription = "Set as default",
                                        tint = if (city.isDefault) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                IconButton(
                                    onClick = { favoritesViewModel.removeFavorite(city) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Remove from favorites",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
