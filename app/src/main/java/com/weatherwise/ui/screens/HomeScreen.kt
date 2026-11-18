package com.weatherwise.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.weatherwise.domain.model.WeatherData
import com.weatherwise.ui.components.AdditionalWeatherDetailsSection
import com.weatherwise.ui.components.DailyForecastCard
import com.weatherwise.ui.components.HourlyForecastSection
import com.weatherwise.ui.components.MainWeatherCard
import com.weatherwise.ui.components.OfflineBanner
import com.weatherwise.ui.components.WeatherBackground
import com.weatherwise.utils.Resource
import com.weatherwise.utils.TemperatureUnit
import com.weatherwise.utils.WindSpeedUnit
import com.weatherwise.viewmodel.WeatherViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    weatherViewModel: WeatherViewModel,
    onNavigateToSearch: () -> Unit,
    onNavigateToForecast: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentWeatherState by weatherViewModel.currentWeather.collectAsState()
    val forecastState by weatherViewModel.forecast.collectAsState()
    val isRefreshing by weatherViewModel.isRefreshing.collectAsState()
    val isFavorite by weatherViewModel.isCurrentCityFavorite.collectAsState()
    val locationMessage by weatherViewModel.locationMessage.collectAsState()
    val userPrefs by weatherViewModel.userPreferences.collectAsState()

    val tempUnit = userPrefs?.temperatureUnit ?: TemperatureUnit.CELSIUS
    val windUnit = userPrefs?.windSpeedUnit ?: WindSpeedUnit.KM_H
    val animationsEnabled = userPrefs?.animationsEnabled ?: true

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(locationMessage) {
        locationMessage?.let {
            snackbarHostState.showSnackbar(it)
            weatherViewModel.dismissLocationMessage()
        }
    }

    val activeWeather = (currentWeatherState as? Resource.Success)?.data

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "W",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "WeatherWise",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    // Location Detection Button
                    IconButton(onClick = { weatherViewModel.requestLocationWeather() }) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "Current Location",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    // Search Button
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search City",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    // Manual Refresh Button
                    IconButton(onClick = { weatherViewModel.refreshWeather() }) {
                        if (isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh Weather",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    // Settings Button
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { innerPadding ->
        WeatherBackground(
            conditionType = activeWeather?.conditionType,
            animationsEnabled = animationsEnabled,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (val state = currentWeatherState) {
                    is Resource.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Fetching real-time weather...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    is Resource.Error -> {
                        ErrorCard(
                            message = state.message,
                            onRetry = { weatherViewModel.refreshWeather() },
                            onOpenSettings = onNavigateToSettings,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                                .align(Alignment.Center)
                        )
                    }
                    is Resource.Success -> {
                        val weather = state.data
                        val forecastBundle = (forecastState as? Resource.Success)?.data

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Offline banner if cached
                            if (state.isOffline || weather.isOffline) {
                                item {
                                    OfflineBanner(
                                        lastUpdatedMillis = weather.lastUpdatedMillis,
                                        onRefresh = { weatherViewModel.refreshWeather() }
                                    )
                                }
                            }

                            // Main Weather Card
                            item {
                                MainWeatherCard(
                                    weather = weather,
                                    temperatureUnit = tempUnit,
                                    isFavorite = isFavorite,
                                    onToggleFavorite = { weatherViewModel.toggleFavorite() }
                                )
                            }

                            // Hourly Forecast Section (24 Hours)
                            if (forecastBundle != null && forecastBundle.hourlyList.isNotEmpty()) {
                                item {
                                    HourlyForecastSection(
                                        hourlyList = forecastBundle.hourlyList,
                                        temperatureUnit = tempUnit,
                                        windSpeedUnit = windUnit
                                    )
                                }
                            }

                            // Additional Weather Metrics
                            item {
                                AdditionalWeatherDetailsSection(
                                    weather = weather,
                                    windSpeedUnit = windUnit
                                )
                            }

                            // 5-Day Forecast Preview
                            if (forecastBundle != null && forecastBundle.dailyList.isNotEmpty()) {
                                item {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "5-Day Forecast",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        FilledTonalButton(
                                            onClick = onNavigateToForecast,
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text("View Full & Chart")
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                contentDescription = "Forecast details",
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }

                                items(forecastBundle.dailyList.take(3).size) { index ->
                                    val daily = forecastBundle.dailyList[index]
                                    DailyForecastCard(
                                        forecast = daily,
                                        temperatureUnit = tempUnit,
                                        windSpeedUnit = windUnit
                                    )
                                }
                            }

                            item {
                                Spacer(modifier = Modifier.height(40.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ErrorCard(
    message: String,
    onRetry: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(54.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Weather Information Unavailable",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onRetry,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Retry")
                }

                if (message.contains("API key", ignoreCase = true)) {
                    FilledTonalButton(
                        onClick = onOpenSettings,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Configure API Key")
                    }
                }
            }
        }
    }
}
