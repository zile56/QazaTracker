package com.zilehasnain.qazatracker.data.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import com.zilehasnain.qazatracker.domain.model.LocationData
import com.zilehasnain.qazatracker.domain.repository.LocationProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.Executor
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Reads the platform LocationManager directly, with approximate (coarse) permission only.
 * No Google Play Services and no geocoding: the position is used for the maths and forgotten.
 */
class AndroidLocationProvider @Inject constructor(
    @ApplicationContext private val context: Context
) : LocationProvider {

    override fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    @Suppress("MissingPermission") // Guarded by hasPermission().
    override suspend fun currentLocation(): LocationData? {
        if (!hasPermission()) return null
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
        val providers = listOf(LocationManager.NETWORK_PROVIDER, LocationManager.PASSIVE_PROVIDER)
            .filter { runCatching { manager.isProviderEnabled(it) }.getOrDefault(false) }

        // A recent fix is free and instant, so try that first.
        val lastKnown = providers.mapNotNull { runCatching { manager.getLastKnownLocation(it) }.getOrNull() }
            .maxByOrNull { it.time }
        if (lastKnown != null) return lastKnown.toLocationData()

        val provider = providers.firstOrNull { it == LocationManager.NETWORK_PROVIDER } ?: return null
        val fresh = withTimeoutOrNull(FRESH_FIX_TIMEOUT_MILLIS) {
            suspendCancellableCoroutine<Location?> { continuation ->
                val signal = androidx.core.os.CancellationSignal()
                continuation.invokeOnCancellation { signal.cancel() }
                val executor = Executor { it.run() }
                LocationManagerCompat.getCurrentLocation(manager, provider, signal, executor) { location ->
                    if (continuation.isActive) continuation.resume(location)
                }
            }
        }
        return fresh?.toLocationData()
    }

    private fun Location.toLocationData() = LocationData(latitude, longitude)

    private companion object {
        const val FRESH_FIX_TIMEOUT_MILLIS = 10_000L
    }
}
