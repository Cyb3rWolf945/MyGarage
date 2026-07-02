package pt.ipt.dama2026.mygarage.domain.location

/**
 * Domain contract for location services.
 */
interface LocationManager {

    fun hasLocationPermission(): Boolean
    suspend fun getCurrentLocation(): LocationResult
}
