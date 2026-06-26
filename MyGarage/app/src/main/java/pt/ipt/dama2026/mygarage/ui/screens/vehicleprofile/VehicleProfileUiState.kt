package pt.ipt.dama2026.mygarage.ui.screens.vehicleprofile

data class VehicleProfileUiState(
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
    val serviceHistory: List<ServiceHistoryItem>,
    val formErrors: Map<String, Int> = emptyMap()
)