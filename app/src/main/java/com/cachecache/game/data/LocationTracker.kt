package com.cachecache.game.data

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/** Position GPS haute précision du téléphone, mise à jour demandée toutes les ~1 seconde. */
class LocationTracker(context: Context) {

    private val client = LocationServices.getFusedLocationProviderClient(context.applicationContext)

    /** À n'appeler qu'une fois la permission de localisation accordée. */
    @SuppressLint("MissingPermission")
    fun updates(intervalMs: Long = 1_000L): Flow<Location> = callbackFlow {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMs)
            .setMinUpdateIntervalMillis(500L)
            .build()
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { trySend(it) }
            }
        }
        client.lastLocation.addOnSuccessListener { last -> last?.let { trySend(it) } }
        client.requestLocationUpdates(request, callback, Looper.getMainLooper())
            .addOnFailureListener { close(it) }
        awaitClose { client.removeLocationUpdates(callback) }
    }
}
