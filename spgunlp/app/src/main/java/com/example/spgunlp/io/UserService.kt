package com.example.spgunlp.io

import com.example.spgunlp.BuildConfig
import com.example.spgunlp.model.AppUser
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Path

interface UserService {
    @Headers("Accept: */*")
    @GET("usuarios/activos/")
    suspend fun getUsers(@Header("Authorization") token: String): Response<List<AppUser>>

    companion object Factory {
        fun create(): UserService {
            val baseUrl= BuildConfig.BASE_URL
            val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("$baseUrl/tesina/")
                .build()

            return retrofit.create(UserService::class.java)
        }
    }
}