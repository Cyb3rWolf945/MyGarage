package ipt.pt.mygarage.domain.location

/**
 * Domain contract for location services.
 * Implementations handle the platform-specific FusedLocationProvider or similar.
 */
interface LocationManager {

    /** Synchronously check whether the app currently holds location permission. */
    fun hasLocationPermission(): Boolean

    /** Obtain the device's last known location. Suspending to allow for async providers. */
    suspend fun getCurrentLocation(): LocationResult
}
