package com.example.spgunlp.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.spgunlp.model.AppVisitParameters

@Dao
interface PrinciplesDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPrinciples(principles: List<AppVisitParameters.Principle>)

    @Query("DELETE FROM principles_table")
    suspend fun clearPrinciples()

    @Transaction
    @Query("SELECT * FROM principles_table")
    fun getPrinciples(): List<AppVisitParameters.Principle>

    @Transaction
    @Query("SELECT * FROM principles_table")
    fun getPrinciplesAsync(): LiveData<List<AppVisitParameters.Principle>>
}