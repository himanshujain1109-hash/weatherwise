package com.weatherwise.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.weatherwise.BuildConfig
import com.weatherwise.data.preferences.ThemeMode
import com.weatherwise.utils.TemperatureUnit
import com.weatherwise.utils.WindSpeedUnit
import com.weatherwise.viewmodel.SettingsViewModel
import com.weatherwise.viewmodel.WeatherViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    weatherViewModel: WeatherViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val prefs by settingsViewModel.preferences.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showApiKeyDialog by remember { mutableStateOf(false) }
    var apiKeyInput by remember { mutableStateOf("") }

    var showTempUnitDialog by remember { mutableStateOf(false) }
    var showWindUnitDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showRefreshDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Unit Settings Group
            item {
                SettingsSectionTitle("Units & Display")
            }

            item {
                SettingsCard {
                    // Temperature Unit
                    SettingsRow(
                        icon = Icons.Default.Thermostat,
                        title = "Temperature Unit",
                        subtitle = prefs?.temperatureUnit?.displayName ?: "Celsius (°C)",
                        onClick = { showTempUnitDialog = true }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Wind Speed Unit
                    SettingsRow(
                        icon = Icons.Default.Speed,
                        title = "Wind Speed Unit",
                        subtitle = prefs?.windSpeedUnit?.displayName ?: "Kilometers per hour (km/h)",
                        onClick = { showWindUnitDialog = true }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Theme
                    SettingsRow(
                        icon = Icons.Default.Brightness4,
                        title = "Theme",
                        subtitle = prefs?.themeMode?.displayName ?: "System Default",
                        onClick = { showThemeDialog = true }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Animations Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Animation,
                                contentDescription = "Weather Animations",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Weather Animations",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Dynamic rain, snow, clouds and sun effects",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = prefs?.animationsEnabled ?: true,
                            onCheckedChange = { settingsViewModel.setAnimationsEnabled(it) }
                        )
                    }
                }
            }

            // Location & Refresh Group
            item {
                SettingsSectionTitle("Location & Refresh")
            }

            item {
                SettingsCard {
                    // Location Weather Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Location",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Location-Based Weather",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Automatically detect weather for your current position",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = prefs?.locationWeatherEnabled ?: true,
                            onCheckedChange = { settingsViewModel.setLocationWeatherEnabled(it) }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Auto Refresh Interval
                    SettingsRow(
                        icon = Icons.Default.Refresh,
                        title = "Auto-Refresh Frequency",
                        subtitle = when (prefs?.autoRefreshMinutes) {
                            0 -> "Manual Only"
                            15 -> "Every 15 minutes"
                            30 -> "Every 30 minutes"
                            60 -> "Every 1 hour"
                            180 -> "Every 3 hours"
                            else -> "Every 1 hour"
                        },
                        onClick = { showRefreshDialog = true }
                    )
                }
            }

            // API Configuration
            item {
                SettingsSectionTitle("API Configuration")
            }

            item {
                SettingsCard {
                    val hasKey = (prefs?.customApiKey?.isNotBlank() == true) || BuildConfig.OPENWEATHER_API_KEY.isNotBlank()
                    SettingsRow(
                        icon = Icons.Default.Key,
                        title = "Weather API Provider",
                        subtitle = if (hasKey) "OpenWeatherMap API (Custom Key Active)" else "Open-Meteo API Active (No Key Required). Tap to configure OpenWeather key.",
                        onClick = {
                            apiKeyInput = prefs?.customApiKey ?: ""
                            showApiKeyDialog = true
                        }
                    )
                }
            }

            // Data & Cache Management
            item {
                SettingsSectionTitle("Data & Cache")
            }

            item {
                SettingsCard {
                    SettingsRow(
                        icon = Icons.Default.DeleteSweep,
                        title = "Clear Search History",
                        subtitle = "Remove all previously searched cities",
                        onClick = {
                            settingsViewModel.clearSearchHistory()
                            scope.launch {
                                snackbarHostState.showSnackbar("Search history cleared")
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SettingsRow(
                        icon = Icons.Default.DeleteSweep,
                        title = "Clear Cached Weather",
                        subtitle = "Remove offline weather and forecast database records",
                        onClick = {
                            settingsViewModel.clearCachedData()
                            scope.launch {
                                snackbarHostState.showSnackbar("Weather cache cleared")
                            }
                        }
                    )
                }
            }

            // About & Attribution
            item {
                SettingsSectionTitle("About WeatherWise")
            }

            item {
                SettingsCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "About",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "WeatherWise v1.0.0",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Production Native Android Weather Application\nBuilt with Kotlin, Jetpack Compose, MVVM & Material 3\nWeather Data powered by Open-Meteo & OpenWeatherMap APIs",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // API Key Dialog
    if (showApiKeyDialog) {
        AlertDialog(
            onDismissRequest = { showApiKeyDialog = false },
            title = { Text("OpenWeatherMap API Key") },
            text = {
                Column {
                    Text(
                        text = "Enter your OpenWeatherMap API key (free from openweathermap.org/api):",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = { apiKeyInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Paste API key here") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        settingsViewModel.setCustomApiKey(apiKeyInput.trim())
                        showApiKeyDialog = false
                        weatherViewModel.refreshWeather()
                        scope.launch {
                            snackbarHostState.showSnackbar("API key saved. Refreshing weather data...")
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showApiKeyDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Temperature Unit Dialog
    if (showTempUnitDialog) {
        AlertDialog(
            onDismissRequest = { showTempUnitDialog = false },
            title = { Text("Select Temperature Unit") },
            text = {
                Column {
                    TemperatureUnit.entries.forEach { unit ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    settingsViewModel.setTemperatureUnit(unit)
                                    showTempUnitDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = prefs?.temperatureUnit == unit,
                                onClick = {
                                    settingsViewModel.setTemperatureUnit(unit)
                                    showTempUnitDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = unit.displayName)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTempUnitDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Wind Speed Unit Dialog
    if (showWindUnitDialog) {
        AlertDialog(
            onDismissRequest = { showWindUnitDialog = false },
            title = { Text("Select Wind Speed Unit") },
            text = {
                Column {
                    WindSpeedUnit.entries.forEach { unit ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    settingsViewModel.setWindSpeedUnit(unit)
                                    showWindUnitDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = prefs?.windSpeedUnit == unit,
                                onClick = {
                                    settingsViewModel.setWindSpeedUnit(unit)
                                    showWindUnitDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = unit.displayName)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showWindUnitDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Theme Mode Dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Select Theme") },
            text = {
                Column {
                    ThemeMode.entries.forEach { mode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    settingsViewModel.setThemeMode(mode)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = prefs?.themeMode == mode,
                                onClick = {
                                    settingsViewModel.setThemeMode(mode)
                                    showThemeDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = mode.displayName)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Refresh Frequency Dialog
    if (showRefreshDialog) {
        val intervals = listOf(
            0 to "Manual Only",
            15 to "Every 15 minutes",
            30 to "Every 30 minutes",
            60 to "Every 1 hour",
            180 to "Every 3 hours"
        )
        AlertDialog(
            onDismissRequest = { showRefreshDialog = false },
            title = { Text("Auto-Refresh Frequency") },
            text = {
                Column {
                    intervals.forEach { (mins, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    settingsViewModel.setAutoRefreshMinutes(mins)
                                    showRefreshDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = prefs?.autoRefreshMinutes == mins,
                                onClick = {
                                    settingsViewModel.setAutoRefreshMinutes(mins)
                                    showRefreshDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = label)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showRefreshDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
    )
}

@Composable
fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            content()
        }
    }
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
