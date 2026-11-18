# WeatherWise – Real-Time Weather Forecast Android Application

**WeatherWise** is a production-quality, native Android weather application built from the ground up using **Kotlin**, **Jetpack Compose**, **Material 3**, and **MVVM Clean Architecture**.

It fetches real-time meteorological conditions and 5-day forecasts from the **OpenWeatherMap API**, supports global city search with live debounced geocoding, detects current device location using **FusedLocationProviderClient**, provides offline caching via **Room Database**, customizes user preferences using **Jetpack DataStore**, and delivers smooth, lightweight weather-based Canvas animations.

---

## 📱 Features

1. **Live Current Weather Conditions**:
   - Location header with City and Country.
   - Observation date, time, and timezone offset handling.
   - Large temperature display in **°C** or **°F**.
   - Feels-like temperature, Daily High and Daily Low.
   - Meteorological condition title and description with dynamic vector icons.
   - Favorite city quick-toggle (heart icon).

2. **Additional Meteorological Metric Cards**:
   - **Humidity**: Percentage (%) and comfort rating.
   - **Wind**: Speed in **km/h**, **m/s**, or **mph**, cardinal compass direction (e.g. NNE, SSW), and rotating compass pointer.
   - **Atmospheric Pressure**: Measured in **hPa** with high/low pressure indicator.
   - **Visibility**: Distance in kilometers (**km**).
   - **Cloudiness**: Cloud coverage percentage (%).
   - **Sunrise & Sunset**: Localized sunrise and sunset times.

3. **Hourly Weather Forecast (24 Hours)**:
   - Horizontally scrollable timeline of upcoming intervals.
   - Timestamp, condition icon, temperature, rain precipitation probability (PoP %), and wind speed.

4. **5-Day Weather Forecast & Interactive Charts**:
   - Day of the week, formatted date, representative daytime condition, icon, min/max temperatures, wind speed, and humidity.
   - **Dual Visualization Modes**:
     - **List View**: Structured cards for each day.
     - **Temperature Trend Chart**: Custom interactive Jetpack Compose Canvas line chart with cubic Bezier curve smoothing, max/min curves, gradient fills, and data points.

5. **Worldwide City Search & Geocoding**:
   - Debounced search queries (350ms) using OpenWeather Geocoding API.
   - Suggestion dropdown displaying matching city, state/region, and country.
   - Automatic home screen update upon selection.
   - Persistent search history with individual delete and "Clear All" options.

6. **Location Detection**:
   - Single-shot, battery-safe location acquisition via **FusedLocationProviderClient**.
   - Handles `ACCESS_COARSE_LOCATION` and `ACCESS_FINE_LOCATION`.
   - Graceful fallback for GPS disabled or permission denial states.

7. **Dynamic Weather Backgrounds & Canvas Animations**:
   - **Sunny**: Warm radiating gradient with rotating sunbeams and core glow.
   - **Night**: Twinkling starfield with crescent moon glow.
   - **Rainy**: Angled falling raindrops.
   - **Snowy**: Gently drifting snowflakes with sinusoidal horizontal sway.
   - **Thunderstorm**: Dark storm atmosphere with periodic lightning flash animations.
   - **Cloudy**: Soft floating cloud shapes drifting across the canvas.
   - Fully toggleable in Settings to preserve battery or respect reduced motion preferences.

8. **Offline Room Database Cache**:
   - Automatically caches the latest successful weather and 5-day forecast payloads.
   - Displays cached data when offline with an "Offline Mode • [Relative Time]" banner and manual retry button.
   - Never replaces valid cached data with failed network responses.

9. **Saved Cities & Favorites**:
   - Dedicated Favorites screen displaying saved cities with real-time temperature and condition previews.
   - Set any city as your startup "Default" city.
   - One-tap navigation to view weather for any saved city.

10. **Customizable Settings (DataStore)**:
    - Temperature Unit: Celsius (°C) / Fahrenheit (°F).
    - Wind Speed Unit: km/h, m/s, mph.
    - Theme Mode: Light, Dark, System Default.
    - Location Weather: Toggle automatic location fetching.
    - Auto-Refresh Interval: Manual, 15m, 30m, 1h, 3h.
    - Weather Animations: ON / OFF toggle.
    - In-App API Key Configuration: Enter or update your OpenWeatherMap API key directly within the app UI!
    - Cache & History Management: Clear search history and cached database records.

---

## 🛠️ Technology Stack

| Layer / Concern | Technology |
|---|---|
| **Language** | Kotlin 2.0.21 |
| **UI Framework** | Jetpack Compose (BOM 2024.12.01) |
| **Design System** | Material 3 (1.3.1) |
| **Architecture** | MVVM + Clean Architecture |
| **Networking** | Retrofit 2.11.0 + OkHttp 4.12.0 |
| **JSON Serialization** | Gson 2.11.0 |
| **Database** | Room 2.6.1 (with KSP) |
| **Preferences** | Jetpack DataStore Preferences 1.1.1 |
| **Location** | Google Play Services Location (FusedLocationProviderClient 21.3.0) |
| **Navigation** | Navigation Compose 2.8.5 |
| **Asynchronous & Concurrency** | Kotlin Coroutines & StateFlow / SharedFlow |
| **Build System** | Gradle 8.13 + Android Gradle Plugin 8.7.3 + Java 21 |
| **Minimum SDK** | Android 8.0 (API 26) |
| **Target SDK** | Android 15 (API 35) |

---

## 📁 Project Architecture

```
WeatherWise/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/weatherwise/
│   │   │   ├── WeatherWiseApp.kt                     # Application class & DI container
│   │   │   ├── MainActivity.kt                       # Single-activity container & edge-to-edge
│   │   │   ├── navigation/
│   │   │   │   ├── Screen.kt                         # Navigation routes & bottom bar items
│   │   │   │   └── AppNavigation.kt                  # NavHost & animated transitions
│   │   │   ├── data/
│   │   │   │   ├── api/
│   │   │   │   │   ├── ApiModels.kt                  # OpenWeatherMap DTOs
│   │   │   │   │   ├── WeatherApiService.kt          # Retrofit interface
│   │   │   │   │   └── RetrofitClient.kt             # OkHttp & Retrofit instance
│   │   │   │   ├── database/
│   │   │   │   │   ├── WeatherEntities.kt            # Room entities (Cache, Favorites, History)
│   │   │   │   │   ├── WeatherDao.kt                 # Room DAO with coroutine & Flow queries
│   │   │   │   │   └── WeatherDatabase.kt            # Room database builder
│   │   │   │   ├── preferences/
│   │   │   │   │   └── UserPreferencesRepository.kt  # DataStore preferences repository
│   │   │   │   └── repository/
│   │   │   │       └── WeatherRepository.kt          # Offline-first repository orchestrator
│   │   │   ├── domain/model/
│   │   │   │   ├── WeatherConditionType.kt           # Weather classification & mapping
│   │   │   │   ├── WeatherData.kt                    # Domain current weather model
│   │   │   │   ├── ForecastData.kt                   # Domain hourly & daily forecast models
│   │   │   │   └── CitySearchResult.kt               # Geocoding search model
│   │   │   ├── location/
│   │   │   │   └── LocationManager.kt                # Battery-conscious location provider
│   │   │   ├── ui/
│   │   │   │   ├── theme/
│   │   │   │   │   ├── Color.kt                      # Light/Dark Material 3 palettes & gradients
│   │   │   │   │   ├── Type.kt                       # Material 3 typography definitions
│   │   │   │   │   └── Theme.kt                      # Dynamic theme provider
│   │   │   │   ├── components/
│   │   │   │   │   ├── WeatherBackground.kt          # Canvas weather animations
│   │   │   │   │   ├── WeatherCard.kt                # Main temperature & condition card
│   │   │   │   │   ├── WeatherDetailCard.kt          # 8 metric detail cards
│   │   │   │   │   ├── HourlyForecastItem.kt         # Horizontally scrollable hourly row
│   │   │   │   │   ├── DailyForecastItem.kt          # 5-day daily forecast rows
│   │   │   │   │   ├── TemperatureTrendChart.kt      # Bezier Canvas temperature trend chart
│   │   │   │   │   ├── WeatherConditionIcon.kt       # Vector weather condition icons
│   │   │   │   │   └── OfflineBanner.kt              # Offline mode indicator banner
│   │   │   │   └── screens/
│   │   │   │       ├── HomeScreen.kt                 # Current weather, hourly & metrics
│   │   │   │       ├── ForecastScreen.kt             # 5-day breakdown & interactive chart
│   │   │   │       ├── SearchScreen.kt               # City search & recent history
│   │   │   │       ├── FavoritesScreen.kt            # Saved cities with live preview
│   │   │   │       └── SettingsScreen.kt             # App preferences & API key manager
│   │   │   ├── viewmodel/
│   │   │   │   ├── WeatherViewModel.kt
│   │   │   │   ├── SearchViewModel.kt
│   │   │   │   ├── FavoritesViewModel.kt
│   │   │   │   ├── SettingsViewModel.kt
│   │   │   │   └── ViewModelFactory.kt
│   │   │   └── utils/
│   │   │       ├── DateFormatter.kt                  # Localized time, date & relative time
│   │   │       ├── UnitConverter.kt                  # °C/°F, km/h, m/s, mph, compass bearing
│   │   │       ├── NetworkUtils.kt                   # Connectivity checks
│   │   │       └── Resource.kt                       # Sealed class for UI state management
│   │   └── res/                                      # Vector drawables, mipmaps, strings, colors
│   └── src/test/java/com/weatherwise/
│       └── UnitConverterTest.kt                      # Unit tests for conversions & conditions
├── gradle/
│   └── wrapper/                                      # Gradle 8.13 wrapper
├── local.properties.example                          # Template for SDK and API key configuration
├── build.gradle.kts                                  # Root build script
├── settings.gradle.kts                               # Project settings
└── gradle.properties                                 # Build flags
```

---

## 🔑 Weather API Setup (No API Key Required!)

**WeatherWise works 100% out-of-the-box without an API key!** Powered by the open-access **Open-Meteo API**, you can search cities, view real-time weather, and check 5-day forecasts immediately after installing.

### Optional: Custom OpenWeatherMap API Key
If you prefer to use OpenWeatherMap:
1. Tap **Settings** in the app.
2. Under **Weather API Provider**, select **OpenWeatherMap API Key**.
3. Paste your free API key from [openweathermap.org/api](https://openweathermap.org/api) and tap **Save**.
4. Alternatively, add `OPENWEATHER_API_KEY=your_key` to `local.properties`.

---

## 🚀 Building & Running

### Prerequisites
- **JDK**: Java 21 (bundled with recent Android Studio versions or OpenJDK 21)
- **Android SDK**: API 35 with Build Tools 35.0.0
- **Android Studio**: Ladybug (2024.2+) or newer

### Building via Command Line (Gradle)
```powershell
# Run unit tests
.\gradlew.bat testDebugUnitTest

# Build debug APK
.\gradlew.bat assembleDebug
```

The compiled APK will be located at:
```
app/build/outputs/apk/debug/app-debug.apk
```

### Installing on Device / Emulator
Ensure USB debugging is enabled on your Android device:
```powershell
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## 🧪 Unit Testing

Unit tests cover:
- Temperature conversions (Celsius ↔ Fahrenheit) with edge cases (0°C, -40°C, 100°C).
- Wind speed conversions (m/s ↔ km/h, m/s ↔ mph).
- Meteorological degree to 16-point cardinal compass conversion (e.g. 0° = N, 90° = E, 225° = SW).
- Visibility formatting (meters to km) and pressure formatting.
- OpenWeather condition code mapping to domain condition types.

Run tests:
```powershell
.\gradlew.bat testDebugUnitTest
```
Result: **100% Passed**.

---

## 📄 License
WeatherWise is open-source under the Apache 2.0 License.
Weather data provided by [OpenWeatherMap](https://openweathermap.org/).
