package pt.iscode.gestorcandidaturas.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import pt.iscode.gestorcandidaturas.entities.Application

@Dao
interface ApplicationDAO {
    @Query("SELECT * FROM Application")
    suspend fun getAll(): List<Application>

    @Query("SELECT * FROM Application WHERE id = :id")
    suspend fun getApplicationByID(id: Int): Application

    @Insert
    suspend fun insert(application: Application)

    @Delete
    suspend fun delete(application: Application)

    @Update
    suspend fun update(application: Application)
}