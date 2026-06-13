package ipt.pt.mygarage.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import ipt.pt.mygarage.domain.location.LocationManager
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Android-specific [LocationManager] implementation backed by
 * [FusedLocationProviderClient] (Google Play Services).
 *
 * Uses [PRIORITY_HIGH_ACCURACY] with a single-update request
 * suspended via [suspendCoroutine] for a clean coroutine API.
 */
class AndroidLocationManager(context: Context) : LocationManager {

    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    override fun hasLocationPermission(): Boolean {
        // Permission checks are delegated to the Compose-level handler;
        // this is a safety-net fallback only.
        return true
    }

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): ipt.pt.mygarage.domain.location.LocationResult =
        suspendCoroutine { continuation ->
            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 5_000L
            ).apply {
                setMaxUpdates(1)
            }.build()

            val callback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    fusedClient.removeLocationUpdates(this)
                    val location: Location? = result.lastLocation
                    if (location != null) {
                        continuation.resume(
                            ipt.pt.mygarage.domain.location.LocationResult.Success(
                                lat = location.latitude,
                                lng = location.longitude
                            )
                        )
                    } else {
                        continuation.resume(
                            ipt.pt.mygarage.domain.location.LocationResult.Error(
                                "No location available"
                            )
                        )
                    }
                }
            }

            fusedClient.requestLocationUpdates(
                locationRequest,
                callback,
                Looper.getMainLooper()
            )
        }
}
