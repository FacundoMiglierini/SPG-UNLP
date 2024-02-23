package com.example.spgunlp.repositories

import androidx.lifecycle.LiveData
import com.example.spgunlp.daos.PerfilDao
import com.example.spgunlp.model.Perfil

class PerfilRepository(private val perfilDao: PerfilDao) {

    suspend fun addPerfil(perfil: Perfil){
        perfilDao.addPerfil(perfil)
    }

    fun getPerfilByEmail(email: String): LiveData<Perfil?>{
        return perfilDao.getPerfilByEmail(email)
    }
}