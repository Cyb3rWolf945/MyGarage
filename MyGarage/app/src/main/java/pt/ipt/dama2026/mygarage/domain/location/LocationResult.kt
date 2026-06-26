package pt.ipt.dama2026.mygarage.domain.location

/**
 * Represents the result of a location-fetch operation.
 */
sealed class LocationResult {
    data class Success(val lat: Double, val lng: Double) : LocationResult()
    data class Error(val message: String) : LocationResult()
}
