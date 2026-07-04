package pt.ipt.dama2026.mygarage.data.mapper

import pt.ipt.dama2026.mygarage.data.model.LicensePlateApiResponse
import pt.ipt.dama2026.mygarage.domain.licenseplates.LicensePlateVehicleData

/**
 * Converte a resposta do serviço SOAP de matrículas
 * para o modelo de domínio LicensePlateVehicleData.
 *
 * O serviço de rede LicensePlateNetworkService extrai o JSON dentro de <vehicleJson> da resposta SOAP
 * e desserializa para LicensePlateApiResponse. Este mapper converte esse resultado
 * intermédio no modelo final usado pela app.
 *
 */
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
