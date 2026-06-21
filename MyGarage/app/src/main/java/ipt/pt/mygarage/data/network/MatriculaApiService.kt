package ipt.pt.mygarage.data.network

import ipt.pt.mygarage.data.model.LicensePlateApiResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface MatriculaApiService {
    @GET("/data/consulta/DataConsultaMatricula")
    suspend fun lookupPlate(
        @Query("matricula") plate: String,
        @Query("username") username: String
    ): Response<LicensePlateApiResponse>
}
