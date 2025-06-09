package pt.iscode.gestorcandidaturas.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import pt.iscode.gestorcandidaturas.entities.Status

@Dao
interface StatusDAO {
    @Query("SELECT * FROM status")
    fun getALL(): List<Status>

    @Query("SELECT name FROM Status where id = :id")
    fun getStatusByID(id: Int): String

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(statuses: List<Status>)
}