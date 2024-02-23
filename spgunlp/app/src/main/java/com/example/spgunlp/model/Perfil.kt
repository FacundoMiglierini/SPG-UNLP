package com.example.spgunlp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "perfil_table")
data class Perfil(
    @PrimaryKey(autoGenerate = false)
    val email: String,
    val nombre: String,
    val posicion: String,
    val celular: String,
    val organizacion: String,
    val rol: String
)
