package com.example.spgunlp.model

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "parameters_table")
@Parcelize
data class AppVisitParameters (
    val aspiracionesFamiliares: String?,
    val comentarios: String?,
    val cumple: Boolean?,
    @PrimaryKey
    val id: Int?,
    val nombre: String?,
    @Embedded(prefix = "parameter_")
    val parametro: Parameter?,
    val sugerencias: String?,
    var visitId: Int?,
): Parcelable {
    @Parcelize
    data class Parameter(
        val habilitado: Boolean?,
        val id: Int?,
        val nombre: String?,
        val principioAgroecologico: Principle?,
        val situacionEsperable: String?
    ): Parcelable

    @Entity(tableName = "principles_table")
    @Parcelize
    data class Principle(
        val habilitado: Boolean?,
        @PrimaryKey
        val id: Int?,
        val nombre: String?
    ): Parcelable{
        constructor(
            habilitado: Boolean,
            id: Int,
            nombre: String
        ):this(
            habilitado,
            null,
            nombre
        )
    }

}
