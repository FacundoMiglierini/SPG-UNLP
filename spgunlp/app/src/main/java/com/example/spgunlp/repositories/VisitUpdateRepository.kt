package com.example.spgunlp.repositories

import com.example.spgunlp.daos.VisitUpdateDao
import com.example.spgunlp.model.VisitUpdate

class VisitUpdateRepository(private val visitUpdateDao: VisitUpdateDao) {

    suspend fun addVisit(visit: VisitUpdate){
        visitUpdateDao.insertVisit(visit)
        visitUpdateDao.updateVisit(visit)
    }

    fun getVisitsByEmail(email: String): List<VisitUpdate> {
        return visitUpdateDao.getVisitsByEmail(email)
    }
}