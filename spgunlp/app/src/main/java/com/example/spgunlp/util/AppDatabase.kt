package com.example.spgunlp.util

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.spgunlp.daos.MessageDao
import com.example.spgunlp.daos.PerfilDao
import com.example.spgunlp.daos.PoligonoDao
import com.example.spgunlp.daos.PrinciplesDao
import com.example.spgunlp.daos.VisitUpdateDao
import com.example.spgunlp.daos.VisitsDao
import com.example.spgunlp.model.AppImage
import com.example.spgunlp.model.AppUser
import com.example.spgunlp.model.AppVisit
import com.example.spgunlp.model.AppVisitParameters
import com.example.spgunlp.model.AppMessage
import com.example.spgunlp.model.Perfil
import com.example.spgunlp.model.Poligono
import com.example.spgunlp.model.VisitUpdate
import com.example.spgunlp.model.VisitUserJoin

@Database(
    entities = [Poligono::class, Perfil::class, AppVisit::class, VisitUpdate::class, AppImage::class, AppUser::class, AppVisitParameters::class, VisitUserJoin::class, AppVisitParameters.Principle::class, AppMessage::class],
    version = 9,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4),
        AutoMigration(from = 4, to = 5),
        AutoMigration(from = 5, to = 6),
        AutoMigration(from = 6, to = 7),
        AutoMigration(from = 7, to = 8),
        AutoMigration(from = 8, to = 9),
    ]
)
@TypeConverters(
    Converter::class,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun poligonoDao(): PoligonoDao
    abstract fun perfilDao(): PerfilDao
    abstract fun visitsDao(): VisitsDao
    abstract fun visitUpdatesDao(): VisitUpdateDao
    abstract fun principlesDao(): PrinciplesDao
    abstract fun  messageDao(): MessageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                return instance
            }
        }
    }
}