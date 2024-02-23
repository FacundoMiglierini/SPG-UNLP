package com.example.spgunlp.model

import androidx.room.Entity

data class AppVisitUpdate(
    val fechaVisita: String?,
    val integrantes: List<Int>?,
    val parametros: List<ParametersUpdate>?,
    val quintaId: Int?
){
    data class ParametersUpdate (
        val aspiracionesFamiliares: String?,
        val comentarios: String?,
        val cumple: Boolean?,
        val parametroId: Int?,
        val sugerencias: String?
    )
}

@Entity(tableName = "changes_table", primaryKeys = ["email", "visitId"])
data class VisitUpdate(
    val email: String,
    val visit: AppVisitUpdate,
    val visitId: Int
)