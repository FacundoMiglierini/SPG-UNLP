package com.example.spgunlp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "poligonos_table")
data class Poligono(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val idVisit: Long,
    val nombre: String?,
    val coordenadas: String
)
