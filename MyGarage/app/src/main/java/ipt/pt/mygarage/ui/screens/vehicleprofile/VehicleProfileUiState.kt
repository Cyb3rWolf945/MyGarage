package ipt.pt.mygarage.ui.screens.vehicleprofile

data class VehicleProfileUiState(
    val modelName: String,
    val year: String,
    val mileage: String,
    val inspectionDate: String,
    val oilType: String,
    val owner: String,
    val seatCount: String,
    val doorCount: String,
    val fuelType: String,
    val engineCapacity: String,
    val iucValue: String,
    val mileageToNextService: String,
    val locationAddress: String,
    val serviceHistory: List<ServiceHistoryItem>
)