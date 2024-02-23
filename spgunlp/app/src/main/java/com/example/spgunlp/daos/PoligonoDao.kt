package com.example.spgunlp.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.spgunlp.model.Poligono

@Dao
interface PoligonoDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addPoligono(poligono: Poligono) : Long

    @Query("SELECT * FROM poligonos_table ORDER BY id ASC")
    fun getPoligonos(): LiveData<List<Poligono>>

    @Query("SELECT * FROM poligonos_table WHERE idVisit = :id")
    fun getPoliByIdVisit(id: Long): LiveData<List<Poligono>>

    @Query("DELETE FROM poligonos_table WHERE id = :id")
    suspend fun deletePoliById(id: Long)
}