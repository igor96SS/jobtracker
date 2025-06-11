package pt.iscode.gestorcandidaturas.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import pt.iscode.gestorcandidaturas.repositories.ApplicationRepository
import pt.iscode.gestorcandidaturas.repositories.CompanyRepository
import pt.iscode.gestorcandidaturas.repositories.StatusRepository

class ApplicationViewModelFactory(
    private val repository: ApplicationRepository,
    private val companyRepository: CompanyRepository,
    private val statusRepository: StatusRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ApplicationViewModel::class.java)) {
            return ApplicationViewModel(repository, companyRepository, statusRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
