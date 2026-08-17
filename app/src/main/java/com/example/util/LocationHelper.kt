package com.example.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.example.model.CityLocation
import com.example.model.TurkishCities
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.math.*

object LocationHelper {

    fun hasLocationPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): Location? {
        if (!hasLocationPermission(context)) return null

        return suspendCancellableCoroutine { continuation ->
            try {
                val fusedClient = LocationServices.getFusedLocationProviderClient(context)
                fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener { location: Location? ->
                        if (location != null) {
                            continuation.resume(location)
                        } else {
                            // Fallback to last known location or LocationManager
                            fusedClient.lastLocation.addOnSuccessListener { lastLoc ->
                                if (lastLoc != null) {
                                    continuation.resume(lastLoc)
                                } else {
                                    continuation.resume(getLocationFromManager(context))
                                }
                            }.addOnFailureListener {
                                continuation.resume(getLocationFromManager(context))
                            }
                        }
                    }
                    .addOnFailureListener {
                        continuation.resume(getLocationFromManager(context))
                    }
            } catch (e: Exception) {
                continuation.resume(getLocationFromManager(context))
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLocationFromManager(context: Context): Location? {
        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
            val gpsLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val networkLoc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            val passiveLoc = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)

            return listOfNotNull(gpsLoc, networkLoc, passiveLoc)
                .maxByOrNull { it.time }
        } catch (e: Exception) {
            return null
        }
    }

    /**
     * Finds the nearest city from TurkishCities database, or creates a precise GPS CityLocation
     * with exact coordinates.
     */
    fun resolveCityFromLocation(location: Location): CityLocation {
        val lat = location.latitude
        val lon = location.longitude

        var closestCity: CityLocation = TurkishCities.defaultCity
        var minDistance = Double.MAX_VALUE

        for (city in TurkishCities.list) {
            val dist = calculateDistanceKm(lat, lon, city.latitude, city.longitude)
            if (dist < minDistance) {
                minDistance = dist
                closestCity = city
            }
        }

        return if (minDistance <= 45.0) {
            // Within 45km of known city center
            closestCity.copy(latitude = lat, longitude = lon)
        } else {
            CityLocation(
                name = "📍 GPS Konumu (${String.format("%.2f", lat)}, ${String.format("%.2f", lon)})",
                country = "Otomatik Konum",
                latitude = lat,
                longitude = lon,
                timeZoneOffsetHours = 3.0
            )
        }
    }

    private fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}
