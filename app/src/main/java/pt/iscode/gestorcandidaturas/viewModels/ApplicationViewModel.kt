package pt.iscode.gestorcandidaturas.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pt.iscode.gestorcandidaturas.Converters
import pt.iscode.gestorcandidaturas.entities.Application
import pt.iscode.gestorcandidaturas.entities.Company
import pt.iscode.gestorcandidaturas.entities.Status
import pt.iscode.gestorcandidaturas.models.ApplicationsValues
import pt.iscode.gestorcandidaturas.repositories.ApplicationRepository
import pt.iscode.gestorcandidaturas.repositories.CompanyRepository
import pt.iscode.gestorcandidaturas.repositories.StatusRepository

class ApplicationViewModel(
    private val repository: ApplicationRepository,
    private val companyRepository: CompanyRepository,
    private val statusRepository: StatusRepository
) : ViewModel() {

    private val _applications = MutableLiveData<List<ApplicationsValues>>()
    val applications: LiveData<List<ApplicationsValues>> get() = _applications

    val companies = MutableLiveData<List<Company>>()
    val statuses = MutableLiveData<List<Status>>()

    fun loadStatus(){
        viewModelScope.launch {
            val list = withContext(Dispatchers.IO){
                statusRepository.getAllStatuses()
            }
            statuses.value = list
        }
    }

    fun loadCompanies(){
        viewModelScope.launch {
            val list = withContext(Dispatchers.IO){
                companyRepository.getAllCompanies()
            }
            companies.value = list
        }
    }

    fun loadApplications() {
        viewModelScope.launch {
            val apps = withContext(Dispatchers.IO) { repository.getAllApplications() }
            val companies = withContext(Dispatchers.IO) { companyRepository.getAllCompanies() }
            val statuses = withContext(Dispatchers.IO) { statusRepository.getAllStatuses() }

            val valuesList = apps.map { app ->
                val companyName = companies.find { it.id == app.companyID }!!.name
                val statusName = statuses.find { it.id == app.statusID }!!.name

                ApplicationsValues(
                    companyName = companyName,
                    jobTitle = app.name,
                    status = statusName,
                    notes = app.notes ?: "",
                    applicationDate = app.dateApplied.toString()
                )
            }

            _applications.value = valuesList
        }
    }

    fun addApplications(companyId: Int, jobTitle: String, location: String, dateApplied: String, applicationUrl: String, statusId: Int, notes: String){
        viewModelScope.launch {
            val dateFormatted = Converters.toLocalDate(dateApplied)

            val application = Application(
                name = jobTitle,
                location = location,
                dateApplied = dateFormatted,
                applicationURL = applicationUrl,
                statusID = statusId,
                companyID = companyId,
                notes = notes
            )
            repository.insertApplication(application)
        }

    }
}

