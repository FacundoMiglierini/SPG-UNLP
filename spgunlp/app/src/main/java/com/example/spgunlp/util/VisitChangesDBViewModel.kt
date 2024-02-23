package com.example.spgunlp.util

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.spgunlp.model.AppVisitUpdate
import com.example.spgunlp.model.VisitUpdate
import com.example.spgunlp.repositories.VisitUpdateRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VisitChangesDBViewModel(application: Application):AndroidViewModel(application) {
    private val repository: VisitUpdateRepository

    init {
        val visitUpdateDao = AppDatabase.getDatabase(application).visitUpdatesDao()
        repository = VisitUpdateRepository(visitUpdateDao)
    }

    fun addVisit(visit: AppVisitUpdate, email: String, id: Int){
        viewModelScope.launch(Dispatchers.IO){
            val visitUpdate = VisitUpdate(email, visit, id)
            repository.addVisit(visitUpdate)
        }
    }

    fun getVisitsByEmail(email: String): List<VisitUpdate>{
        return repository.getVisitsByEmail(email)
    }

}