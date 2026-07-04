package pt.ipt.dama2026.mygarage.data.location

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
import pt.ipt.dama2026.mygarage.domain.location.LocationManager
import pt.ipt.dama2026.mygarage.domain.location.LocationResult as DomainLocationResult
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Obtém a localização GPS do dispositivo (uma única vez).
 *
 * Fluxo das permissões e obtenção da localização:
 * 1. A UI verifica se a permissão de localização foi concedida.
 *    Se não -> mostra diálogo a pedir permissão.
 *    Se sim -> chama getCurrentLocation().
 * 2. getCurrentLocation() suspende a coroutine e pede 1 update ao GPS.
 * 3. Ao receber o resultado:
 *    Se localização disponível -> devolve Success(lat, lng).
 *    Se não -> devolve Error.
 *
 * Nota: hasLocationPermission devolve sempre true. A razão é que a interface
 * LocationManager exige este método, mas a verificação real da permissão
 * (ACCESS_FINE_LOCATION) já foi feita antes na UI (AndroidX Activity Result API).
 * Quando o código chega a esta classe, a permissão já está garantida.
 */
class AndroidLocationManager(context: Context) : LocationManager {

    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    override fun hasLocationPermission(): Boolean {
        return true
    }

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): DomainLocationResult =
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
                            DomainLocationResult.Success(
                                lat = location.latitude,
                                lng = location.longitude
                            )
                        )
                    } else {
                        continuation.resume(
                            DomainLocationResult.Error(
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
