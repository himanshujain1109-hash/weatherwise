package com.weatherwise.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager as AndroidLocationManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

sealed class LocationResult {
    data class Success(val location: Location) : LocationResult()
    data object PermissionDenied : LocationResult()
    data object GpsDisabled : LocationResult()
    data class Error(val message: String) : LocationResult()
}

class AppLocationManager(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    fun hasLocationPermission(): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineGranted || coarseGranted
    }

    fun isGpsEnabled(): Boolean {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? AndroidLocationManager
            ?: return false
        return lm.isProviderEnabled(AndroidLocationManager.GPS_PROVIDER) ||
                lm.isProviderEnabled(AndroidLocationManager.NETWORK_PROVIDER)
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): LocationResult {
        if (!hasLocationPermission()) {
            return LocationResult.PermissionDenied
        }

        if (!isGpsEnabled()) {
            return LocationResult.GpsDisabled
        }

        return suspendCancellableCoroutine { continuation ->
            val cts = CancellationTokenSource()

            continuation.invokeOnCancellation {
                cts.cancel()
            }

            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                cts.token
            ).addOnSuccessListener { location ->
                if (location != null) {
                    continuation.resume(LocationResult.Success(location))
                } else {
                    // Try last known location as fallback
                    fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc ->
                        if (lastLoc != null) {
                            continuation.resume(LocationResult.Success(lastLoc))
                        } else {
                            continuation.resume(LocationResult.Error("Unable to acquire location fix. Please ensure location services are enabled."))
                        }
                    }.addOnFailureListener { e ->
                        continuation.resume(LocationResult.Error(e.message ?: "Failed to get last known location."))
                    }
                }
            }.addOnFailureListener { e ->
                continuation.resume(LocationResult.Error(e.message ?: "Failed to acquire location."))
            }
        }
    }
}
