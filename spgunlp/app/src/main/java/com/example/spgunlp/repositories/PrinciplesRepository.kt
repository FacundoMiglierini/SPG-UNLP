package com.example.spgunlp.repositories

import androidx.lifecycle.LiveData
import com.example.spgunlp.daos.PrinciplesDao
import com.example.spgunlp.model.AppVisitParameters

class PrinciplesRepository(private val principlesDao: PrinciplesDao) {
    suspend fun insertPrinciples(principles: List<AppVisitParameters.Principle>){
        return principlesDao.insertPrinciples(principles)
    }

    fun getPrinciples(): LiveData<List<AppVisitParameters.Principle>>{
        return principlesDao.getPrinciplesAsync()
    }

    suspend fun clearPrinciples(){
        principlesDao.clearPrinciples()
    }
}