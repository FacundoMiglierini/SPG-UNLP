package com.example.spgunlp.repositories

import androidx.lifecycle.LiveData
import com.example.spgunlp.daos.PoligonoDao
import com.example.spgunlp.model.Poligono

class PoligonoRepository(private val poligonoDao: PoligonoDao) {
    val getPoligonos: LiveData<List<Poligono>> = poligonoDao.getPoligonos()

    suspend fun addPoligono(poligono: Poligono): Long{
        return poligonoDao.addPoligono(poligono)
    }

    fun getPoliByIdVisit(id: Long): LiveData<List<Poligono>>{
        return poligonoDao.getPoliByIdVisit(id)
    }

    suspend fun deletePoliById(id: Long){
        poligonoDao.deletePoliById(id)
    }
}