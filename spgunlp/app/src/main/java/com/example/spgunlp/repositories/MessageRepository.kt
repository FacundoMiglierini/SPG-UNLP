package com.example.spgunlp.repositories

import androidx.lifecycle.LiveData
import com.example.spgunlp.daos.MessageDao
import com.example.spgunlp.model.AppMessage

class MessageRepository(private val messageDao: MessageDao) {
    suspend fun addMessage(message: AppMessage) {
        messageDao.addMessage(message)
    }

    fun getMessagesByVisitPrinciple(idVisit: Long, idPrinciple: Long): LiveData<List<AppMessage>> {
        return messageDao.getMessagesByVisitPrinciple(idVisit, idPrinciple)
    }
}
