package pt.ipt.dama2026.mygarage.domain.location

/**
 * Resultado de pedir a localização ao GPS.
 *
 * Sealed class: ou correu bem (Success com latitude/longitude)
 * ou houve erro (Error com a mensagem).
 *
 * Devolvido por LocationManager.getCurrentLocation().
 */
sealed class LocationResult {
    /** Localização obtida com sucesso. */
    data class Success(val lat: Double, val lng: Double) : LocationResult()
    /** Algo falhou (GPS desligado, sem permissão, timeout). */
    data class Error(val message: String) : LocationResult()
}
