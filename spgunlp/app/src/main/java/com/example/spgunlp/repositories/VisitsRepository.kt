package com.example.spgunlp.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import com.example.spgunlp.daos.VisitsDao
import com.example.spgunlp.model.AppVisit
import com.example.spgunlp.model.VisitUserJoin
import com.example.spgunlp.model.VisitWithImagesMembersAndParameters

class VisitsRepository(private val visitsDao: VisitsDao) {

    suspend fun insertVisit(visit: AppVisit){
        visitsDao.insertVisit(visit)
        visit.imagenes?.let { images ->
            images.forEach {
               it.visitId = visit.id
            }
            visitsDao.insertImagesList(images)
        }
        visit.integrantes?.let { members ->
            visitsDao.insertUsersList(members)
            members.forEach {
                visitsDao.insertVisitUserJoin(VisitUserJoin(visit.id!!, it.id!!))
            }
        }
        visit.visitaParametrosResponse?.let { parameters ->
            parameters.forEach {
                it.visitId = visit.id
            }
            visitsDao.insertParametersList(parameters)
        }
    }

    suspend fun insertVisits(visits: List<AppVisit>){
        visitsDao.insertVisits(visits)
        visits.forEach {visit ->
            visit.imagenes?.let { images ->
                images.forEach {
                    it.visitId = visit.id
                }
                visitsDao.insertImagesList(images)
            }
            visit.integrantes?.let { members ->
                visitsDao.insertUsersList(members)
                members.forEach {
                    visitsDao.insertVisitUserJoin(VisitUserJoin(visit.id!!, it.id!!))
                }
            }
            visit.visitaParametrosResponse?.let { parameters ->
                parameters.forEach {
                    it.visitId = visit.id
                }
                visitsDao.insertParametersList(parameters)
            }
        }
    }

    suspend fun updateVisit(visit: AppVisit){
        try {
            visitsDao.updateVisit(visit)
            visit.imagenes?.let { images ->
                images.forEach {
                    it.visitId = visit.id
                }
                visitsDao.updateImagesList(images)
            }
            visit.integrantes?.let { members ->
                visitsDao.updateUsersList(members)
                members.forEach {
                    visitsDao.updateVisitUserJoin(VisitUserJoin(visit.id!!, it.id!!))
                }
            }
            visit.visitaParametrosResponse?.let { parameters ->
                parameters.forEach {
                    it.visitId = visit.id
                }
                visitsDao.updateParametersList(parameters)
            }
        } catch (e: Exception) {
            Log.e("SPGUNLP_DB", e.message.toString())
        }
    }

    suspend fun updateVisits(visits: List<AppVisit>){
        visitsDao.updateVisits(visits)
    }

    suspend fun clearVisits(){
        visitsDao.clearVisits()
        visitsDao.clearImagesList()
        visitsDao.clearUsersList()
        visitsDao.clearVisitUserJoin()
        visitsDao.clearParametersList()
    }

    fun getAllVisits(): List<AppVisit>{
        val visits = visitsDao.getAllFullVisits()
        val appVisitList = mutableListOf<AppVisit>()
        visits.forEach { visit ->
            appVisitList.add(
                AppVisit(
                    visit.visit.id,
                    visit.visit.comentarioImagenes,
                    visit.visit.estadoVisita,
                    visit.visit.fechaActualizacion,
                    visit.visit.fechaCreacion,
                    visit.visit.fechaVisita,
                    visit.imagenes,
                    visit.integrantes,
                    visit.visit.quintaResponse,
                    visit.visit.usuarioOperacion,
                    visit.parameters
                )
            )
        }
        return appVisitList
    }
    fun getVisitById(visitId: Int): LiveData<VisitWithImagesMembersAndParameters> {
        return visitsDao.getFullVisitById(visitId)
    }

}