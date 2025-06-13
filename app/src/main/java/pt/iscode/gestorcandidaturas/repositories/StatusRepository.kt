package pt.iscode.gestorcandidaturas.repositories

import pt.iscode.gestorcandidaturas.daos.StatusDAO
import pt.iscode.gestorcandidaturas.entities.Status

class StatusRepository(private val statusDao: StatusDAO) {

    suspend fun getAllStatuses(): List<Status>{
        return statusDao.getALL()
    }

    suspend fun getStatusById(id: Int): String{
        return statusDao.getStatusByID(id)
    }
}