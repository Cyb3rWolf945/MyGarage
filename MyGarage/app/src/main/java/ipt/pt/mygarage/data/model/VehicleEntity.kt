package ipt.pt.mygarage.data.model

/**
 * Represents a vehicle in the garage. Currently backed by hardcoded data;
 * will be migrated to Room (@Entity) in a future iteration.
 */
data class VehicleEntity(
    val id: String,
    val modelName: String,
    val plate: String,
    val mileage: String,
    val year: String,
    val status: String
)
