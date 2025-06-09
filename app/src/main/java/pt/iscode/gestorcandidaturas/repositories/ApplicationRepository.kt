package pt.iscode.gestorcandidaturas.repositories

import pt.iscode.gestorcandidaturas.daos.ApplicationDAO
import pt.iscode.gestorcandidaturas.entities.Application


class ApplicationRepository(private val applicationDao: ApplicationDAO) {

    suspend fun getAllApplications(): List<Application> {
        return applicationDao.getAll()
    }

    suspend fun getApplicationById(id: Int): Application {
        return applicationDao.getApplicationByID(id)
    }

    suspend fun insertApplication(application: Application) {
        applicationDao.insert(application)
    }

    suspend fun updateApplication(application: Application) {
        applicationDao.update(application)
    }

    suspend fun deleteApplication(application: Application) {
        applicationDao.delete(application)
    }
}
