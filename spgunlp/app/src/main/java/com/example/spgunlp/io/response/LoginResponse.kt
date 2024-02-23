package com.example.spgunlp.io.response

data class LoginResponse(
    val usuario: String,
    val token: String,
)

data class AuthErrorResponse(
    val codigoError: Int,
    val detalleError: String,
    val descripcion: String,
)
