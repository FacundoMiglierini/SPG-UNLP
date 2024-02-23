package com.example.spgunlp.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.spgunlp.model.Perfil
import com.example.spgunlp.repositories.PerfilRepository
import com.example.spgunlp.util.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application): AndroidViewModel(application)  {

    private val repository: PerfilRepository
    init {
        val perfilDao = AppDatabase.getDatabase(application).perfilDao()
        repository = PerfilRepository(perfilDao)
    }

    fun addPerfil(perfil: Perfil){
        viewModelScope.launch(Dispatchers.IO){
            repository.addPerfil(perfil)
        }
    }

    fun getPerfilByEmail(email: String): LiveData<Perfil?> {
        return repository.getPerfilByEmail(email)
    }

}