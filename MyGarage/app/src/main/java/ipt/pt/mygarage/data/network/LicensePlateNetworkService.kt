package ipt.pt.mygarage.data.network

import android.util.Log
import com.google.gson.Gson
import ipt.pt.mygarage.data.mapper.toDomainVehicleData
import ipt.pt.mygarage.data.model.LicensePlateApiResponse
import ipt.pt.mygarage.domain.licenseplates.ErrorType
import ipt.pt.mygarage.domain.licenseplates.LicensePlateApiResult
import ipt.pt.mygarage.domain.licenseplates.LicensePlateApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class LicensePlateNetworkService(
    private val username: String
) : LicensePlateApiService {

    private companion object {
        private const val TAG = "LicensePlateNetworkService"
        private const val SOAP_NAMESPACE = "http://regcheck.org.uk"
        private const val SOAP_METHOD_NAME = "CheckPortugal"
        private const val SOAP_ACTION = "http://regcheck.org.uk/CheckPortugal"
        private const val SOAP_URL = "http://www.regcheck.org.uk/api/reg.asmx"

        // Accept plates with letters and numbers (at least 2-3 of each, total 5-6 chars)
        private val VALID_PLATE_PATTERN = Regex("^[A-Z0-9]{5,7}$")
    }

    override suspend fun lookupVehicle(plate: String): LicensePlateApiResult {
        return withContext(Dispatchers.IO) {
            try {
                if (!validatePlateFormat(plate)) {
                    return@withContext LicensePlateApiResult.Error(
                        "Invalid license plate format",
                        ErrorType.INVALID_PLATE
                    )
                }

                val normalizedPlate = normalizePlate(plate)
                val response = callSoapService(normalizedPlate)

                when {
                    response != null && response.abiCode != null -> {
                        val vehicleData = response.toDomainVehicleData(normalizedPlate)
                        LicensePlateApiResult.Success(vehicleData)
                    }
                    else -> {
                        LicensePlateApiResult.Error(
                            "Vehicle not found in registry",
                            ErrorType.NOT_FOUND
                        )
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "Network error during plate lookup", e)
                LicensePlateApiResult.Error(
                    "Network error. Please check your connection.",
                    ErrorType.NETWORK_ERROR
                )
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error during plate lookup", e)
                LicensePlateApiResult.Error(
                    "Unknown error: ${e.message}",
                    ErrorType.UNKNOWN
                )
            }
        }
    }

    override suspend fun validatePlateFormat(plate: String): Boolean {
        return withContext(Dispatchers.Default) {
            val normalizedPlate = plate.replace("-", "").replace(".", "").replace("•", "").uppercase()
            // Just check that it has letters and numbers and reasonable length
            normalizedPlate.length >= 5 && normalizedPlate.length <= 7 &&
            normalizedPlate.matches(VALID_PLATE_PATTERN)
        }
    }

    private fun normalizePlate(plate: String): String {
        // Remove ALL non-alphanumeric characters and spaces, keep only letters and numbers
        return plate.filter { it.isLetterOrDigit() }.uppercase()
    }

    private fun callSoapService(registrationNumber: String): LicensePlateApiResponse? {
        return try {
            Log.d(TAG, "Looking up plate: $registrationNumber")
            val soapRequest = buildSoapRequest(registrationNumber)
            Log.d(TAG, "SOAP Request:\n$soapRequest")

            val url = URL(SOAP_URL)
            val connection = url.openConnection() as HttpURLConnection

            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "text/xml; charset=utf-8")
            connection.setRequestProperty("SOAPAction", SOAP_ACTION)
            connection.doOutput = true
            connection.connectTimeout = 15000
            connection.readTimeout = 15000

            connection.outputStream.use { os ->
                os.write(soapRequest.toByteArray(Charsets.UTF_8))
                os.flush()
            }

            val responseCode = connection.responseCode
            Log.d(TAG, "Response code: $responseCode")

            val responseStream = if (responseCode == HttpURLConnection.HTTP_OK) {
                connection.inputStream
            } else {
                connection.errorStream
            }

            val responseBody = responseStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } ?: ""
            Log.d(TAG, "Response body:\n$responseBody")

            // Check for SOAP Fault
            if (responseBody.contains("<soap:Fault>") || responseBody.contains("faultstring")) {
                val faultMessage = extractFaultMessage(responseBody)
                Log.e(TAG, "SOAP Fault: $faultMessage")
                return null
            }

            if (responseCode == HttpURLConnection.HTTP_OK) {
                val result = parseSoapResponse(responseBody)
                Log.d(TAG, "Parsed result: $result")
                result
            } else {
                Log.e(TAG, "SOAP request failed with code: $responseCode")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "SOAP service call failed", e)
            e.printStackTrace()
            null
        }
    }

    private fun buildSoapRequest(registrationNumber: String): String {
        Log.d(TAG, "Building SOAP with username: '$username' (length: ${username.length})")
        return """<?xml version="1.0" encoding="utf-8"?>
<soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Body>
    <CheckPortugal xmlns="$SOAP_NAMESPACE">
      <RegistrationNumber>$registrationNumber</RegistrationNumber>
      <username>$username</username>
    </CheckPortugal>
  </soap:Body>
</soap:Envelope>"""
    }

    private fun extractFaultMessage(xmlResponse: String): String {
        return try {
            val startIdx = xmlResponse.indexOf("<faultstring>") + "<faultstring>".length
            val endIdx = xmlResponse.indexOf("</faultstring>", startIdx)
            if (startIdx > 0 && endIdx > startIdx) {
                xmlResponse.substring(startIdx, endIdx).trim()
                    .replace("&gt;", ">")
                    .replace("&lt;", "<")
                    .replace("&amp;", "&")
            } else {
                "Unknown SOAP fault"
            }
        } catch (e: Exception) {
            "Unknown SOAP fault"
        }
    }

    private fun parseSoapResponse(xmlResponse: String): LicensePlateApiResponse? {
        return try {
            val gson = Gson()

            // JSON is in <vehicleJson> tags within the SOAP response
            val startIdx = xmlResponse.indexOf("<vehicleJson>") + "<vehicleJson>".length
            val endIdx = xmlResponse.indexOf("</vehicleJson>")

            if (startIdx > 12 && endIdx > startIdx) {
                val jsonString = xmlResponse.substring(startIdx, endIdx).trim()
                Log.d(TAG, "Extracted JSON: $jsonString")
                val result = gson.fromJson(jsonString, LicensePlateApiResponse::class.java)
                Log.d(TAG, "Parsed successfully: ${result.abiCode}")
                result
            } else {
                Log.e(TAG, "Could not find <vehicleJson> in SOAP response")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse SOAP response", e)
            e.printStackTrace()
            null
        }
    }
}
