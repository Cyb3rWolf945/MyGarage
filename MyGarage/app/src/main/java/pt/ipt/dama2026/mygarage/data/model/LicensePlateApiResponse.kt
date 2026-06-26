package pt.ipt.dama2026.mygarage.data.model

import com.google.gson.annotations.SerializedName

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

data class TextValueField(
    @SerializedName("CurrentTextValue")
    val currentTextValue: String? = null
)
