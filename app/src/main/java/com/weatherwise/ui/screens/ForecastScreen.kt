package com.weatherwise.ui.screens

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.weatherwise.ui.components.DailyForecastCard
import com.weatherwise.ui.components.OfflineBanner
import com.weatherwise.ui.components.TemperatureTrendChart
import com.weatherwise.ui.components.WeatherBackground
import com.weatherwise.utils.Resource
import com.weatherwise.utils.TemperatureUnit
import com.weatherwise.utils.WindSpeedUnit
import com.weatherwise.viewmodel.WeatherViewModel

enum class ForecastViewType {
    LIST,
    CHART
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForecastScreen(
    weatherViewModel: WeatherViewModel,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val forecastState by weatherViewModel.forecast.collectAsState()
    val currentWeatherState by weatherViewModel.currentWeather.collectAsState()
    val userPrefs by weatherViewModel.userPreferences.collectAsState()

    val tempUnit = userPrefs?.temperatureUnit ?: TemperatureUnit.CELSIUS
    val windUnit = userPrefs?.windSpeedUnit ?: WindSpeedUnit.KM_H
    val animationsEnabled = userPrefs?.animationsEnabled ?: true

    var selectedView by remember { mutableStateOf(ForecastViewType.LIST) }

    val activeWeather = (currentWeatherState as? Resource.Success)?.data

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "5-Day Forecast",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        activeWeather?.let {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "City",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "${it.cityName}, ${it.country}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
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
                when (val state = forecastState) {
                    is Resource.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
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
                        val bundle = state.data

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Offline banner
                            if (bundle.isOffline) {
                                item {
                                    OfflineBanner(
                                        lastUpdatedMillis = bundle.lastUpdatedMillis,
                                        onRefresh = { weatherViewModel.refreshWeather() }
                                    )
                                }
                            }

                            // View Switcher (List vs Chart)
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FilterChip(
                                        selected = selectedView == ForecastViewType.LIST,
                                        onClick = { selectedView = ForecastViewType.LIST },
                                        label = { Text("Daily List View") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.List,
                                                contentDescription = "List View",
                                                modifier = Modifier.size(18.dp)
                                            )
                                        },
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    FilterChip(
                                        selected = selectedView == ForecastViewType.CHART,
                                        onClick = { selectedView = ForecastViewType.CHART },
                                        label = { Text("Temperature Trend Chart") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.BarChart,
                                                contentDescription = "Chart View",
                                                modifier = Modifier.size(18.dp)
                                            )
                                        },
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }
                            }

                            if (selectedView == ForecastViewType.CHART) {
                                item {
                                    TemperatureTrendChart(
                                        dailyList = bundle.dailyList,
                                        temperatureUnit = tempUnit
                                    )
                                }
                            }

                            // Always show daily forecast cards (or below chart for comprehensive detail)
                            item {
                                Text(
                                    text = if (selectedView == ForecastViewType.CHART) "Detailed Daily Breakdowns" else "Upcoming 5 Days",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp, start = 4.dp)
                                )
                            }

                            items(bundle.dailyList) { daily ->
                                DailyForecastCard(
                                    forecast = daily,
                                    temperatureUnit = tempUnit,
                                    windSpeedUnit = windUnit
                                )
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
