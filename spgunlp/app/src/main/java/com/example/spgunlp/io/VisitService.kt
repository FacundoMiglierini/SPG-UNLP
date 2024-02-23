package com.example.spgunlp.io

import com.example.spgunlp.BuildConfig
import com.example.spgunlp.io.response.VisitByIdResponse
import com.example.spgunlp.model.AppVisit
import com.example.spgunlp.model.AppVisitParameters
import com.example.spgunlp.model.AppVisitUpdate
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.Response

interface VisitService {

    @Headers("Accept: */*")
    @GET("visitas/")
    suspend fun getVisits(@Header("Authorization") token: String): Response<List<AppVisit>>

    @Headers("Accept: */*")
    @GET("principios/")
    suspend fun getPrinciples(@Header("Authorization") token: String): Response<List<AppVisitParameters.Principle>>

    @PUT("visitas/{id}")
    suspend fun updateVisitById(@Header("Authorization") token: String, @Path("id") visitId: Int, @Body visit: AppVisitUpdate): Response<AppVisit>

    @GET("home/")
    suspend fun getHome(@Header("Authorization") token: String): Response<List<String>>

    companion object Factory {
        fun create(): VisitService {
            val baseUrl= BuildConfig.BASE_URL
            val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("$baseUrl/tesina/")
                .build()

            return retrofit.create(VisitService::class.java)
        }
    }
}