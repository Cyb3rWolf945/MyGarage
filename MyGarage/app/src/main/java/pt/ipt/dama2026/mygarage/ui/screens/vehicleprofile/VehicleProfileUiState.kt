package pt.ipt.dama2026.mygarage.ui.screens.vehicleprofile

/** Estado do ecrã de perfil do veículo com todos os campos e histórico de serviços. */
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
    val locationAddress: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val serviceHistory: List<ServiceHistoryItem>,
    val formErrors: Map<String, Int> = emptyMap()
)