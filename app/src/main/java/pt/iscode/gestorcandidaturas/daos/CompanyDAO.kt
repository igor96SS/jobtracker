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
    suspend fun getAll(): List<Company>

    @Query("SELECT * FROM company WHERE id= :id")
    suspend fun getCompanyByID(id: Int): Company

    @Query("SELECT name FROM company WHERE LOWER(name) = LOWER(:name) LIMIT 1")
    suspend fun companyExists(name: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(company: Company)

    @Delete
    suspend fun delete(company: Company)

    @Update
    suspend fun update(company: Company)
}