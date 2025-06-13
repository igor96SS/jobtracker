package pt.iscode.gestorcandidaturas.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pt.iscode.gestorcandidaturas.entities.Application
import pt.iscode.gestorcandidaturas.entities.Company
import pt.iscode.gestorcandidaturas.entities.Status
import pt.iscode.gestorcandidaturas.models.ApplicationsValues
import pt.iscode.gestorcandidaturas.repositories.ApplicationRepository
import pt.iscode.gestorcandidaturas.repositories.CompanyRepository
import pt.iscode.gestorcandidaturas.repositories.StatusRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ApplicationViewModel(
    private val repository: ApplicationRepository,
    private val companyRepository: CompanyRepository,
    private val statusRepository: StatusRepository
) : ViewModel() {

    // Backing properties for exposing immutable LiveData to the UI.
    // ViewModel updates the data internally,
    // while only exposing read-only access (LiveData) to Activities or Fragments.
    private val _applications = MutableLiveData<List<ApplicationsValues>>()
    val applications: LiveData<List<ApplicationsValues>> get() = _applications

    private val _companies = MutableLiveData<List<Company>>()
    val companiesLiveData: LiveData<List<Company>> get() = _companies

    private val _statuses = MutableLiveData<List<Status>>()
    val statusesLiveData: LiveData<List<Status>> get() = _statuses

    private val _applicationDetails = MutableLiveData<ApplicationsValues>()
    val applicationDetail: LiveData<ApplicationsValues> get() = _applicationDetails

    fun loadStatus(){
        viewModelScope.launch {
            val list = withContext(Dispatchers.IO){
                statusRepository.getAllStatuses()
            }
            _statuses.value = list
        }
    }

    fun loadCompanies(){
        viewModelScope.launch {
            val list = withContext(Dispatchers.IO){
                companyRepository.getAllCompanies()
            }
            _companies.value = list
        }
    }

    fun loadApplicationById(applicationId: Int){
        viewModelScope.launch {
            val application = withContext(Dispatchers.IO) {
                repository.getApplicationById(applicationId)
            }

            val company = withContext(Dispatchers.IO) {
                companyRepository.getCompanyById(application.companyID)
            }
            val statusName = withContext(Dispatchers.IO) {
                statusRepository.getStatusById(application.statusID)
            }

            val applicationValues = ApplicationsValues(
                applicationId = application.id,
                companyName = company.name,
                jobTitle = application.name,
                status = statusName,
                notes = application.notes ?: "",
                applicationDate = application.dateApplied.toString(),
                applicationURL = application.applicationURL
            )
            _applicationDetails.postValue(applicationValues)
        }
    }

    fun loadAllData() {
        viewModelScope.launch {
            val companies = withContext(Dispatchers.IO) { companyRepository.getAllCompanies() }
            val statuses = withContext(Dispatchers.IO) { statusRepository.getAllStatuses() }
            val apps = withContext(Dispatchers.IO) { repository.getAllApplications() }

            val valuesList = apps.map { app ->
                val companyName = companies.find { it.id == app.companyID }?.name ?: "Unknown Company"
                val statusName = statuses.find { it.id == app.statusID }?.name ?: "Unknown Status"
                ApplicationsValues(
                    applicationId = app.id,
                    companyName = companyName,
                    jobTitle = app.name,
                    status = statusName,
                    notes = app.notes ?: "",
                    applicationDate = app.dateApplied.toString(),
                    applicationURL = app.applicationURL
                )
            }

            _applications.postValue(valuesList)
            _companies.postValue(companies)
            _statuses.postValue(statuses)

        }
    }


    fun addApplications(companyId: Int, jobTitle: String, location: String, dateApplied: String, applicationUrl: String, statusId: Int, notes: String){
        viewModelScope.launch {
            val userFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            val parsedDate = LocalDate.parse(dateApplied, userFormatter)

            val application = Application(
                name = jobTitle,
                location = location,
                dateApplied = parsedDate,
                applicationURL = applicationUrl,
                statusID = statusId,
                companyID = companyId,
                notes = notes
            )
            repository.insertApplication(application)
        }
    }

    fun addCompany(companyName: String, companyWebsite: String, companyLinkedin: String){
        viewModelScope.launch {
            val company = Company(
                name = companyName,
                website = companyWebsite,
                linkedinUrl = companyLinkedin
            )

            companyRepository.insertCompany(company)
        }
    }
}

