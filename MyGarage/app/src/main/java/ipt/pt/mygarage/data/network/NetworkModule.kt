package ipt.pt.mygarage.data.network

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {
    private const val MATRICULA_API_BASE_URL = "https://www.matricula.co.pt"

    private fun createGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .create()
    }

    private fun createRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(MATRICULA_API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(createGson()))
            .build()
    }

    fun createMatriculaApiService(): MatriculaApiService {
        return createRetrofit().create(MatriculaApiService::class.java)
    }
}
