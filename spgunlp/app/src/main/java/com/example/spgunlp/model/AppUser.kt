package com.example.spgunlp.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "users_table")
@Parcelize
data class AppUser(
    @PrimaryKey
    val id: Int?,
    val email: String?,
    @Ignore
    val password: String?,
    val celular: String?,
    val nombre: String?,
    val organizacion: String?,
    @Ignore
    val posicion: Int?,
    val posicionResponse: Posicion?,
    val roles: List<Rol>?,
    val estado: Boolean?
): Parcelable {
    constructor(email: String, password: String) : this(null,
        email,
        password,
        null,
        null,
        null,
        null,
        null,
        null,
        null
    )

    constructor(email: String, password: String, celular: String, nombre: String, organizacion: String, posicion: Int) : this(null,
        email,
        password,
        celular,
        nombre,
        organizacion,
        posicion,
        null,
        null,
        null)

    constructor(id: Int, email: String, celular: String?, nombre: String?, organizacion: String?, posicionResponse: Posicion?, roles: List<Rol>?, estado: Boolean?) : this(
        id,
        email,
        null,
        celular,
        nombre,
        organizacion,
        null,
        posicionResponse,
        roles,
        estado
    )
}
