package ipt.pt.mygarage.domain.licenseplates

interface LicensePlateApiService {
    suspend fun lookupVehicle(plate: String): LicensePlateApiResult
    suspend fun validatePlateFormat(plate: String): Boolean
}
