package pt.iscode.gestorcandidaturas

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import pt.iscode.gestorcandidaturas.daos.ApplicationDAO
import pt.iscode.gestorcandidaturas.daos.CompanyDAO
import pt.iscode.gestorcandidaturas.daos.StatusDAO
import pt.iscode.gestorcandidaturas.entities.Application
import pt.iscode.gestorcandidaturas.entities.Company
import pt.iscode.gestorcandidaturas.entities.Status
import java.util.concurrent.Executors

@TypeConverters(Converters::class)
@Database(entities = [Company::class, Status::class, Application::class], version = 1)
abstract class AppDatabase: RoomDatabase() {
    abstract fun companyDao(): CompanyDAO
    abstract fun statusDao(): StatusDAO
    abstract fun applicationDao(): ApplicationDAO

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "job_tracker_database"
                )
                    .addCallback(prepopulateCallback)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private val prepopulateCallback = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)

                Executors.newSingleThreadExecutor().execute {
                    INSTANCE?.statusDao()?.apply {
                        insertAll(
                            listOf(
                                Status(name = "Applied"),
                                Status(name = "Refused"),
                                Status(name = "Interview"),
                                Status(name = "Waiting")
                            )
                        )
                    }
                }
            }
        }
    }
}