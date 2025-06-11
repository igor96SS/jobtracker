package pt.iscode.gestorcandidaturas.repositories

import pt.iscode.gestorcandidaturas.daos.CompanyDAO
import pt.iscode.gestorcandidaturas.entities.Company

class CompanyRepository(private val companyDao: CompanyDAO) {

    suspend fun getAllCompanies(): List<Company>{
        return companyDao.getAll()
    }
}