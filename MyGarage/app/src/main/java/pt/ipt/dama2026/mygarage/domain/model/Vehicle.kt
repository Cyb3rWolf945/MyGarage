package pt.ipt.dama2026.mygarage.domain.model

/**
 * Domain-level representation of a vehicle in the garage.
 * Decoupled from any persistence or UI frameworks.
 *
 * Image fields support an offline-first strategy:
 *   - [localImageFileNames] for locally stored images (multiple support).
 *   - [remoteImageUrl] reserved for future cloud synchronization.
 */
data class Vehicle(
    val id: String,
    val plate: String,
    val name: String,
    val year: String,
    val mileage: String,
    val inspectionDate: String? = null,
    val oilType: String? = null,
    val owner: String,
    val seatCount: String? = null,
    val doorCount: String? = null,
    val fuelType: String,
    val engineCapacity: String,
    val iucValue: String? = null,
    val mileageToNextService: String? = null,
    val locationAddress: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val localImageFileNames: List<String> = emptyList(),
    val remoteImageUrl: String? = null
)
