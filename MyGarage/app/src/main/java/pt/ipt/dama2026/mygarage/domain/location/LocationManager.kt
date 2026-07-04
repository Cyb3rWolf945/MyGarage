package pt.ipt.dama2026.mygarage.domain.location

/**
 * Interface de domínio para obter a localização GPS do dispositivo.
 *
 * Define o contrato: quem quiser implementar localização tem de saber
 * verificar permissões e devolver coordenadas.
 *
 * Implementação concreta é feita no AndroidLocationManager (FusedLocationProviderClient).
 * Injetada via Hilt no AppModule.
 *
 */
interface LocationManager {

    /** É sempre true na implementação porque a verificação real é feita na UI antes de chamar getCurrentLocation. */
    fun hasLocationPermission(): Boolean
    /** Pede uma localização única. Suspende até receber ou falhar. */
    suspend fun getCurrentLocation(): LocationResult
}
