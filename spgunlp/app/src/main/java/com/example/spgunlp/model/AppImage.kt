package com.example.spgunlp.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName="images_table")
@Parcelize
data class AppImage(
    @PrimaryKey
    val id: Int?,
    val contenido: ByteArray?,
    val tipo: String?,
    var visitId: Int?
): Parcelable {
    constructor(contenido: ByteArray, tipo: String) : this(null, contenido, tipo, null)
}
