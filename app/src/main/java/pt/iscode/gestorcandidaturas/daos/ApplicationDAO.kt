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
    fun getAll(): List<Application>

    @Query("SELECT * FROM Application WHERE id = :id")
    fun getApplicationByID(id: Int): Application

    @Insert
    fun insert(application: Application)

    @Delete
    fun delete(application: Application)

    @Update
    fun update(application: Application)
}