package ipt.pt.mygarage.ui.screens.vehicleprofile

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
    val mileageToNextService: String,
    val locationAddress: String? = null,
    val serviceHistory: List<ServiceHistoryItem>
)