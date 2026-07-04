package pt.ipt.dama2026.mygarage.domain.licenseplates

/**
 * Resultado da consulta de uma matrícula à API externa.
 *
 * É um sealed class: ou deu certo (Success com os dados do veículo)
 * No caso da matricula devolver os dados todos do veículo, o LicensePlateVehicleData é preenchido com os campos disponíveis.
 * No caso da matrícula não existir, ou não ter dados, ou a API estar em baixo, ou não haver internet, ou outro erro qualquer, devolve-se um Error com a mensagem e o tipo de erro.
 *
 * Usado pelo LicensePlateNetworkService → LicensePlateMapper → UI.
 */


 /**
  * Devolve o resultado da consulta de uma matrícula à API externa.
  */
sealed class LicensePlateApiResult {
    /** Matrícula encontrada. Contém os dados do veículo para preencher o formulário. */
    data class Success(val vehicleData: LicensePlateVehicleData) : LicensePlateApiResult()
    /** Algo correu mal. O errorType indica à UI que mensagem mostrar. */
    data class Error(val message: String, val errorType: ErrorType) : LicensePlateApiResult()
}

/** Tipos de erro possíveis ao consultar uma matrícula. */
enum class ErrorType {
    NETWORK_ERROR,      // sem internet
    INVALID_PLATE,      // formato inválido (ex.: "123")
    NOT_FOUND,          // matrícula não existe na base de dados
    API_UNAVAILABLE,    // serviço externo em baixo
    UNKNOWN             // erro inesperado
}

/** Dados do veículo devolvidos pela API de matrículas. */
data class LicensePlateVehicleData(
    val plate: String,
    val vehicleModel: String? = null,    // marca + modelo
    val year: String? = null,            // ano de registo
    val color: String? = null,           // cor
    val fuelType: String? = null,        // gasoline, diesel, electric
    val owner: String? = null,           // sempre null (API externa não tem)
    val engineCapacity: String? = null,  // cilindrada
    val raiseStatus: String? = null,     // ABICode (roubo, penhora, etc.)
    val additionalInfo: Map<String, String> = emptyMap()  // extensível
)
