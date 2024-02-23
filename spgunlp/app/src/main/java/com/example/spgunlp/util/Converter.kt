package com.example.spgunlp.util

import androidx.room.TypeConverter
import com.example.spgunlp.model.AppImage
import com.example.spgunlp.model.AppVisitParameters
import com.example.spgunlp.model.AppVisitUpdate
import com.example.spgunlp.model.Posicion
import com.example.spgunlp.model.Rol
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converter {
    @TypeConverter
    fun fromImagesList(images: List<AppImage>): String {
        //TODO store images outside the DB
        return Gson().toJson(images)
    }

    @TypeConverter
    fun toImagesList(images: String): List<AppImage>{
        val listType = object : TypeToken<List<AppImage>>() {}.type
        return Gson().fromJson(images, listType)
    }

    @TypeConverter
    fun fromPosicion(posicion: Posicion?): String {
        return Gson().toJson(posicion)
    }

    @TypeConverter
    fun toPosicion(posicion: String): Posicion?{
        return run {
            val type = object : TypeToken<Posicion>() {}.type
            Gson().fromJson(posicion, type)
        }
    }

    @TypeConverter
    fun fromRolesList(roles: List<Rol>?): String {
        return Gson().toJson(roles)
    }

    @TypeConverter
    fun toRolesList(roles: String): List<Rol>?{
        val listType = object : TypeToken<List<Rol>>() {}.type
        return Gson().fromJson(roles, listType)
    }

    @TypeConverter
    fun fromPrinciple(principle: AppVisitParameters.Principle): String {
        return Gson().toJson(principle)
    }

    @TypeConverter
    fun toPrinciple(principle: String): AppVisitParameters.Principle{
        val type = object : TypeToken<AppVisitParameters.Principle>() {}.type
        return Gson().fromJson(principle, type)
    }

    @TypeConverter
    fun fromAppVisitUpdate(update: AppVisitUpdate): String {
        return Gson().toJson(update)
    }

    @TypeConverter
    fun toAppVisitUpdate(update: String): AppVisitUpdate{
        val type = object : TypeToken<AppVisitUpdate>() {}.type
        return Gson().fromJson(update, type)
    }
}
