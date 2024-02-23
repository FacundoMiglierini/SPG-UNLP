package com.example.spgunlp.ui.maps

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.spgunlp.model.Poligono
import com.example.spgunlp.repositories.PoligonoRepository
import com.example.spgunlp.util.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PoligonoViewModel(application: Application):AndroidViewModel(application) {
    private val getPoligonos: LiveData<List<Poligono>>
    private val repository: PoligonoRepository

    init {
        val poligonoDao = AppDatabase.getDatabase(application).poligonoDao()
        repository = PoligonoRepository(poligonoDao)
        getPoligonos = repository.getPoligonos
    }

    fun addPoligono(poligono: Poligono): LiveData<Long>{
        val id = MutableLiveData<Long>()
        viewModelScope.launch(Dispatchers.IO){
            id.postValue(repository.addPoligono(poligono))
        }
        return id

    }

    fun getPoliByIdVisit(id: Long): LiveData<List<Poligono>>{
        return repository.getPoliByIdVisit(id)
    }

    fun deletePoliById(id: Long){
        viewModelScope.launch(Dispatchers.IO){
            repository.deletePoliById(id)
        }
    }
}