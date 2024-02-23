package com.example.spgunlp.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Posicion(
    val id: Int?,
    val nombre: String?,
    val habilitado: Boolean?
): Parcelable
