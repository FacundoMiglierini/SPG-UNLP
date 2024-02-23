package com.example.spgunlp.model

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation
import kotlinx.parcelize.Parcelize


val VISIT_ITEM="VISIT_ITEM"
val IS_ACTIVE="IS_ACTIVE"

@Entity(tableName = "visits_table")
@Parcelize
data class AppVisit(
    @PrimaryKey
    val id: Int?,
    val comentarioImagenes: String?,
    val estadoVisita: String?,
    val fechaActualizacion: String?,
    val fechaCreacion: String?,
    val fechaVisita: String?,
    @Ignore
    val imagenes: List<AppImage>?,
    @Ignore
    val integrantes: List<AppUser>?,
    @Embedded(prefix = "quinta_")
    val quintaResponse: AppQuinta?,
    val usuarioOperacion: String?,
    @Ignore
    val visitaParametrosResponse: List<AppVisitParameters>?,
): Parcelable {
    constructor(id:Int,comentarioImagenes: String?,estadoVisita: String?,fechaActualizacion: String?,fechaCreacion: String?,fechaVisita: String?,quintaResponse: AppQuinta?,usuarioOperacion: String?):
            this(
                id,
                comentarioImagenes,
                estadoVisita,
                fechaActualizacion,
                fechaCreacion,
                fechaVisita,
                null,
                null,
                quintaResponse,
                usuarioOperacion,
                null
            )
}
@Parcelize
data class AppQuinta (
    val id: Int?,
    val comentarios: String?,
    val direccion: String?,
    val fechaCreacion: String?,
    val fechaUltimaVisita: String?,
    val imagenes: List<AppImage>?,
    val nombreProductor: String?,
    val organizacion: String?,
    val selloGarantia: String?,
    val superficieAgroecologiaCampo: Int?,
    val superficieAgroecologiaInvernaculo: Int?,
    val superficieTotalCampo: Int?,
    val superficieTotalInvernaculo : Int?,
    val usuarioOperacion: String?,
): Parcelable

@Entity(tableName = "visit_user_join", primaryKeys = ["visitId", "userId"],)
data class VisitUserJoin(
    val visitId: Int,
    val userId: Int
)


/*
data class VisitWithMembers(
    @Embedded
    val visit: AppVisit,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(VisitUserJoin::class, parentColumn = "visitId", entityColumn = "userId")
    )
    val integrantes: List<AppUser>,
)

data class VisitWithMembersAndImages(
   @Embedded
   val visit: VisitWithMembers,
   @Relation(
       parentColumn = "id",
       entityColumn = "visitId"
   )
   val imagenes: List<AppImage>,
)

data class VisitWithImagesMembersAndParameters(
    @Embedded
    val visit: VisitWithMembersAndImages,
    @Relation(
        parentColumn = "id",
        entityColumn = "visitId"
    )
    val parameters: List<AppVisitParameters>
)
*/

data class VisitWithImagesMembersAndParameters(
    @Embedded
    val visit: AppVisit,
    @Relation(
        parentColumn = "id",
        entityColumn = "visitId"
    )
    val imagenes: List<AppImage>,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(VisitUserJoin::class, parentColumn = "visitId", entityColumn = "userId")
    )
    val integrantes: List<AppUser>,
    @Relation(
        parentColumn = "id",
        entityColumn = "visitId"
    )
    val parameters: List<AppVisitParameters>
)