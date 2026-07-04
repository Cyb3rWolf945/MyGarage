package pt.ipt.dama2026.mygarage.domain.licenseplates

/**
 * Interface de domínio para consulta de matrículas.
 *
 * Define o contrato: quem quiser implementar um serviço de matrículas
 * tem de saber procurar um veículo e validar o formato da matrícula.
 *
 * Implementação concreta: LicensePlateNetworkService (usa Retrofit para chamar a API externa).
 * Injetada via Hilt no AppModule.
 */
interface LicensePlateApiService {
    /** Procura a matrícula na base de dados externa. Devolve Success ou Error. */
    suspend fun lookupVehicle(plate: String): LicensePlateApiResult
    /** Verifica se a matrícula tem formato português válido (ex.: AA-00-00). */
    suspend fun validatePlateFormat(plate: String): Boolean
}
