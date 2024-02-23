package com.example.spgunlp.io

import com.example.spgunlp.BuildConfig
import com.example.spgunlp.io.response.LoginResponse
import com.example.spgunlp.model.AppUser
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("auth/login")
    suspend fun login(@Body user:AppUser): Response<LoginResponse>

    @POST("auth/registro")
    suspend fun registro(@Body user:AppUser): Response<Void>

    companion object Factory {
        fun create(): AuthService {
            val baseUrl= BuildConfig.BASE_URL
            val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("$baseUrl/tesina/")
                .build()

            return retrofit.create(AuthService::class.java)
        }
    }
}