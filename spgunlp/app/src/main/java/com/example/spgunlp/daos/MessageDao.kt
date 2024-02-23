package com.example.spgunlp.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.spgunlp.model.AppMessage

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addMessage(message: AppMessage)

    @Query("SELECT * FROM messages_table WHERE idVisit = :idVisit AND idPrinciple = :idPrinciple ORDER BY id ASC")
    fun getMessagesByVisitPrinciple(idVisit: Long, idPrinciple: Long): LiveData<List<AppMessage>>
}