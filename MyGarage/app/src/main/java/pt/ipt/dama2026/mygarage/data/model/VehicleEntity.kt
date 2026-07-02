package pt.ipt.dama2026.mygarage.data.model

/**
 * Room entity representing a vehicle in the garage.
 */
data class VehicleEntity(
    val id: String,
    val modelName: String,
    val plate: String,
    val mileage: String,
    val year: String,
    val status: String
)
