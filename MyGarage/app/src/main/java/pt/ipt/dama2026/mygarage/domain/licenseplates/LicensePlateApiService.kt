package pt.ipt.dama2026.mygarage.domain.licenseplates

interface LicensePlateApiService {
    suspend fun lookupVehicle(plate: String): LicensePlateApiResult
    suspend fun validatePlateFormat(plate: String): Boolean
}
