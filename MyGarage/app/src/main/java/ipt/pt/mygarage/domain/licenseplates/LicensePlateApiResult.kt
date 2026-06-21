package ipt.pt.mygarage.domain.licenseplates

sealed class LicensePlateApiResult {
    data class Success(val vehicleData: LicensePlateVehicleData) : LicensePlateApiResult()
    data class Error(val message: String, val errorType: ErrorType) : LicensePlateApiResult()
    object Loading : LicensePlateApiResult()
    object Idle : LicensePlateApiResult()
}

enum class ErrorType {
    NETWORK_ERROR,
    INVALID_PLATE,
    NOT_FOUND,
    API_UNAVAILABLE,
    UNKNOWN
}

data class LicensePlateVehicleData(
    val plate: String,
    val vehicleModel: String? = null,
    val year: String? = null,
    val color: String? = null,
    val fuelType: String? = null,
    val owner: String? = null,
    val engineCapacity: String? = null,
    val raiseStatus: String? = null,
    val additionalInfo: Map<String, String> = emptyMap()
)
