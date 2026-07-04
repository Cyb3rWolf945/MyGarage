package pt.ipt.dama2026.mygarage.domain.model

/**
 * Veículo.
 *
 * Modelo de domínio
 * A conversão de/para VehicleEntity é feita pelos mappers em data/mapper/.
 */
data class Vehicle(
    val id: String,
    val plate: String,
    val name: String,
    val year: String,
    val mileage: String,
    val mileageKm: Double = 0.0,
    val inspectionDate: String? = null,
    val oilType: String? = null,
    val owner: String,
    val seatCount: String? = null,
    val doorCount: String? = null,
    val fuelType: String,
    val engineCapacity: String,
    val iucValue: String? = null,
    val locationAddress: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val localImageFileNames: List<String> = emptyList(),
    val remoteImageUrl: String? = null
)
