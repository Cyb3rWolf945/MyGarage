package pt.ipt.dama2026.mygarage.domain.location

/**
 * Result of a location fetch: Success(lat, lng) or Error(message).
 */
sealed class LocationResult {
    data class Success(val lat: Double, val lng: Double) : LocationResult()
    data class Error(val message: String) : LocationResult()
}
