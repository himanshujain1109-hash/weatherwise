package com.weatherwise.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {

    // Current weather cache
    @Query("SELECT * FROM cached_weather WHERE id = 1 LIMIT 1")
    suspend fun getCachedWeather(): CachedWeatherEntity?

    @Query("SELECT * FROM cached_weather WHERE id = 1 LIMIT 1")
    fun getCachedWeatherFlow(): Flow<CachedWeatherEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCachedWeather(weather: CachedWeatherEntity)

    @Query("DELETE FROM cached_weather")
    suspend fun clearCachedWeather()

    // Forecast cache
    @Query("SELECT * FROM cached_forecast WHERE id = 1 LIMIT 1")
    suspend fun getCachedForecast(): CachedForecastEntity?

    @Query("SELECT * FROM cached_forecast WHERE id = 1 LIMIT 1")
    fun getCachedForecastFlow(): Flow<CachedForecastEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCachedForecast(forecast: CachedForecastEntity)

    @Query("DELETE FROM cached_forecast")
    suspend fun clearCachedForecast()

    // Favorites
    @Query("SELECT * FROM favorite_cities ORDER BY addedAtMillis DESC")
    fun getAllFavoriteCities(): Flow<List<FavoriteCityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavoriteCity(city: FavoriteCityEntity): Long

    @Query("DELETE FROM favorite_cities WHERE id = :id")
    suspend fun deleteFavoriteCityById(id: Long)

    @Query("DELETE FROM favorite_cities WHERE latitude = :lat AND longitude = :lon")
    suspend fun deleteFavoriteCityByCoords(lat: Double, lon: Double)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_cities WHERE latitude = :lat AND longitude = :lon LIMIT 1)")
    fun isCityFavorite(lat: Double, lon: Double): Flow<Boolean>

    @Query("UPDATE favorite_cities SET isDefault = 0")
    suspend fun clearAllDefaultCities()

    @Query("UPDATE favorite_cities SET isDefault = 1 WHERE id = :id")
    suspend fun setDefaultCity(id: Long)

    @Query("SELECT * FROM favorite_cities WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefaultCity(): FavoriteCityEntity?

    // Search History
    @Query("SELECT * FROM search_history ORDER BY searchedAtMillis DESC LIMIT 20")
    fun getRecentSearchHistory(): Flow<List<SearchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearchHistory(item: SearchHistoryEntity): Long

    @Query("DELETE FROM search_history WHERE id = :id")
    suspend fun deleteSearchHistoryById(id: Long)

    @Query("DELETE FROM search_history")
    suspend fun clearSearchHistory()
}
