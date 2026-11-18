package com.weatherwise

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.weatherwise.data.preferences.ThemeMode
import com.weatherwise.navigation.AppNavigation
import com.weatherwise.ui.theme.WeatherWiseTheme
import com.weatherwise.viewmodel.FavoritesViewModel
import com.weatherwise.viewmodel.SearchViewModel
import com.weatherwise.viewmodel.SettingsViewModel
import com.weatherwise.viewmodel.ViewModelFactory
import com.weatherwise.viewmodel.WeatherViewModel

class MainActivity : ComponentActivity() {

    private val app by lazy { application as WeatherWiseApp }

    private val viewModelFactory by lazy {
        ViewModelFactory(
            weatherRepository = app.weatherRepository,
            preferencesRepository = app.preferencesRepository,
            locationManager = app.locationManager
        )
    }

    private val weatherViewModel: WeatherViewModel by viewModels { viewModelFactory }
    private val searchViewModel: SearchViewModel by viewModels { viewModelFactory }
    private val favoritesViewModel: FavoritesViewModel by viewModels { viewModelFactory }
    private val settingsViewModel: SettingsViewModel by viewModels { viewModelFactory }

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (fineGranted || coarseGranted) {
            weatherViewModel.requestLocationWeather()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Request location permission if not already granted
        if (!app.locationManager.hasLocationPermission()) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }

        setContent {
            val userPrefs by weatherViewModel.userPreferences.collectAsState()
            val themeMode = userPrefs?.themeMode ?: ThemeMode.SYSTEM

            WeatherWiseTheme(themeMode = themeMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(
                        weatherViewModel = weatherViewModel,
                        searchViewModel = searchViewModel,
                        favoritesViewModel = favoritesViewModel,
                        settingsViewModel = settingsViewModel
                    )
                }
            }
        }
    }
}
