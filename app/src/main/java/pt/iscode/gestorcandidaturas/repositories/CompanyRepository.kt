package pt.iscode.gestorcandidaturas.repositories

import pt.iscode.gestorcandidaturas.daos.CompanyDAO
import pt.iscode.gestorcandidaturas.entities.Company

class CompanyRepository(private val companyDao: CompanyDAO) {

    suspend fun getAllCompanies(): List<Company>{
        return companyDao.getAll()
    }

    suspend fun insertCompany(company: Company){
        companyDao.insert(company)
    }

    suspend fun getCompanyById(id: Int): Company{
        return companyDao.getCompanyByID(id)
    }

    suspend fun companyExists(name: String): Boolean{
        return companyDao.companyExists(name)!= null
    }
}