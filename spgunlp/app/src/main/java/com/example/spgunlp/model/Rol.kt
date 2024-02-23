package com.example.spgunlp.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Rol (
    val id: Int?,
    val nombre: String?
): Parcelable