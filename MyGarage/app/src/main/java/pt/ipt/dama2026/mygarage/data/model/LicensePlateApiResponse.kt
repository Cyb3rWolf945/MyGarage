package pt.ipt.dama2026.mygarage.data.model

import com.google.gson.annotations.SerializedName

/**
 * Modelos para desserializar a resposta do serviço SOAP de matrículas (regcheck.org.uk).
 *
 * Fluxo:
 * 1. LicensePlateNetworkService extrai o JSON dentro de <vehicleJson> da resposat SOAP.
 * 2. O Gson desserializa para LicensePlateApiResponse.
 * 3. LicensePlateMapper converte para LicensePlateVehicleData (modelo de domínio).
 *
 * Campos TextValueField: a API encapsula certos valores (marca, modelo, combustível)
 * dentro de um objeto com a chave "CurrentTextValue", exigindo esta estrutura intermédia.
 */

/** Resposta JSON extraída do envelope SOAP do serviço de matrículas (regcheck.org.uk). */
data class LicensePlateApiResponse(
    @SerializedName("ABICode")
    val abiCode: String? = null,
    @SerializedName("Description")
    val description: String? = null,
    @SerializedName("RegistrationYear")
    val registrationYear: String? = null,
    @SerializedName("CarMake")
    val carMake: TextValueField? = null,
    @SerializedName("CarModel")
    val carModel: TextValueField? = null,
    @SerializedName("EngineSize")
    val engineSize: TextValueField? = null,
    @SerializedName("FuelType")
    val fuelType: TextValueField? = null,
    @SerializedName("MakeDescription")
    val makeDescription: TextValueField? = null,
    @SerializedName("ModelDescription")
    val modelDescription: TextValueField? = null,
    @SerializedName("NumberOfSeats")
    val numberOfSeats: TextValueField? = null,
    @SerializedName("Version")
    val version: String? = null,
    @SerializedName("Colour")
    val colour: String? = null,
    @SerializedName("VechileIdentificationNumber")
    val vehicleIdentificationNumber: String? = null,
    @SerializedName("RegistrationDate")
    val registrationDate: String? = null,
    @SerializedName("ImageUrl")
    val imageUrl: String? = null,
    @SerializedName("GrossWeight")
    val grossWeight: String? = null,
    @SerializedName("NetWeight")
    val netWeight: String? = null,
    @SerializedName("Imported")
    val imported: Int? = null,
    val error: String? = null
)

/**
 * A API não devolve "FuelType": "Gasolina", mas sim:
 * "FuelType": { "CurrentTextValue": "Gasolina" }
 * então temos de utilizar esta estrutura intermédia para campos que a API devolve como objeto em vez de string.
 *
 * Esta classe, vai permitir que a biblioteca Gson consiga fazer o parse da resposta.
 */
data class TextValueField(
    @SerializedName("CurrentTextValue")
    val currentTextValue: String? = null
)
