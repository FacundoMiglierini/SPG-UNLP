package com.example.spgunlp.io.response
import com.example.spgunlp.model.AppImage
import com.example.spgunlp.model.AppUser
import com.example.spgunlp.model.AppQuinta
import com.example.spgunlp.model.AppVisitParameters

data class VisitByIdResponse(
    val comentarioImagenes: String?,
    val estadoVisita: String?,
    val fechaActualizacion: String?,
    val fechaCreacion: String?,
    val fechaVisita: String?,
    val imagenes: List<AppImage>?,
    val integrantes: List<AppUser>?,
    val quintaResponse: AppQuinta?,
    val usuarioOperacion: String?,
    val visitaParametrosResponse: List<AppVisitParameters>?,
    val id: Int?
)
