package com.example.spgunlp.ui.visit

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.spgunlp.model.AppMessage
import com.example.spgunlp.repositories.MessageRepository
import com.example.spgunlp.util.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MessagesViewModel(application: Application): AndroidViewModel(application) {
    private val repository: MessageRepository
    init {
        val messageDao = AppDatabase.getDatabase(application).messageDao()
        repository = MessageRepository(messageDao)
    }

    fun addMessage(message: AppMessage){
        viewModelScope.launch(Dispatchers.IO){
            repository.addMessage(message)
        }
    }

    fun getMessagesByVisitPrinciple(idVisit: Long, idPrinciple: Long): LiveData<List<AppMessage>> {
        return repository.getMessagesByVisitPrinciple(idVisit, idPrinciple)
    }
}