package ipt.pt.mygarage.data.mapper

import ipt.pt.mygarage.data.model.LicensePlateApiResponse
import ipt.pt.mygarage.domain.licenseplates.LicensePlateVehicleData

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
