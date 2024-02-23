package com.example.spgunlp.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.spgunlp.model.AppImage
import com.example.spgunlp.model.AppUser
import com.example.spgunlp.model.AppVisit
import com.example.spgunlp.model.AppVisitParameters
import com.example.spgunlp.model.VisitUserJoin
import com.example.spgunlp.model.VisitWithImagesMembersAndParameters
import androidx.room.Update as Update

@Dao
interface VisitsDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertVisit(visit: AppVisit)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertVisits(visits: List<AppVisit>)

    @Update
    suspend fun updateVisits(visits: List<AppVisit>)

    @Update
    suspend fun updateVisit(visit: AppVisit)

    @Query("DELETE FROM visits_table") suspend fun clearVisits()

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertImagesList(images: List<AppImage>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUsersList(users: List<AppUser>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertVisitUserJoin(join: VisitUserJoin)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertParametersList(parameters: List<AppVisitParameters>)

    @Update
    suspend fun updateImagesList(images: List<AppImage>)

    @Update
    suspend fun updateUsersList(users: List<AppUser>)

    @Update
    suspend fun updateVisitUserJoin(join: VisitUserJoin)

    @Update
    suspend fun updateParametersList(parameters: List<AppVisitParameters>)

    @Query("DELETE FROM images_table") suspend fun clearImagesList()

    @Query("DELETE FROM users_table") suspend fun clearUsersList()

    @Query("DELETE FROM visit_user_join") suspend fun clearVisitUserJoin()

    @Query("DELETE FROM parameters_table") suspend fun clearParametersList()

    @Transaction
    @Query("SELECT * FROM visits_table")
    fun getAllFullVisits(): List<VisitWithImagesMembersAndParameters>

    @Transaction
    @Query("SELECT * FROM visits_table WHERE id = :id")
    fun getFullVisitById(id: Int): LiveData<VisitWithImagesMembersAndParameters>
}