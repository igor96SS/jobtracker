package pt.iscode.gestorcandidaturas.daos

import androidx.room.Dao
import androidx.room.Query
import pt.iscode.gestorcandidaturas.entities.Status

@Dao
interface SatusDAO {
    @Query("SELECT * FROM status")
    fun getALL(): List<Status>

    @Query("SELECT name FROM Status where id = :id")
    fun getStatusByID(id: Int): String
}