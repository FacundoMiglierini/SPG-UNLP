package com.example.spgunlp.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.spgunlp.model.Perfil

@Dao
interface PerfilDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addPerfil(perfil: Perfil)

    @Query("SELECT * FROM perfil_table WHERE email = :email")
    fun getPerfilByEmail(email: String): LiveData<Perfil?>
}