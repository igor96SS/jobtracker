package pt.iscode.gestorcandidaturas.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import pt.iscode.gestorcandidaturas.entities.Company

@Dao
interface CompanyDAO {
    @Query("SELECT * FROM company")
    fun getAll(): List<Company>

    @Query("SELECT * FROM company WHERE id= :id")
    fun getCompanyByID(id: Int): Company

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(company: Company)

    @Delete
    fun delete(company: Company)

    @Update
    fun update(company: Company)
}