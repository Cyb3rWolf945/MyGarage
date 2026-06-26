package pt.ipt.dama2026.mygarage.data.mapper

import pt.ipt.dama2026.mygarage.data.model.LicensePlateApiResponse
import pt.ipt.dama2026.mygarage.domain.licenseplates.LicensePlateVehicleData

fun LicensePlateApiResponse.toDomainVehicleData(plate: String): LicensePlateVehicleData = LicensePlateVehicleData(
    plate = plate,
    vehicleModel = modelDescription?.currentTextValue ?: carModel?.currentTextValue,
    year = registrationYear,
    color = colour,
    fuelType = fuelType?.currentTextValue,
    owner = null,
    engineCapacity = engineSize?.currentTextValue,
    raiseStatus = abiCode,
    additionalInfo = emptyMap()
)
