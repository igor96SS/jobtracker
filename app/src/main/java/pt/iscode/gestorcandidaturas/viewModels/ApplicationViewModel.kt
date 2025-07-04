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
    private val applicationRepository: ApplicationRepository,
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

    private val _updateApplicationStatus = MutableLiveData<Boolean>()
    val updateApplicationStatus: LiveData<Boolean> = _updateApplicationStatus


    //check if company exists
    suspend fun companyExists(name: String): Boolean {
        return companyRepository.companyExists(name)
    }


    //region load data
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
                applicationRepository.getApplicationById(applicationId)
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
                statusId = application.statusID,
                status = statusName,
                notes = application.notes ?: "",
                applicationDate = application.dateApplied.toString(),
                applicationURL = application.applicationURL,
                applicationLocation = application.location
            )
            _applicationDetails.postValue(applicationValues)
        }
    }

    fun loadAllData() {
        viewModelScope.launch {
            val companies = withContext(Dispatchers.IO) { companyRepository.getAllCompanies() }
            val statuses = withContext(Dispatchers.IO) { statusRepository.getAllStatuses() }
            val apps = withContext(Dispatchers.IO) { applicationRepository.getAllApplications() }

            val valuesList = apps.map { app ->
                val companyName = companies.find { it.id == app.companyID }?.name ?: "Unknown Company"
                val statusId = statuses.find { it.id == app.statusID }?.id ?:0
                val statusName = statuses.find { it.id == app.statusID }?.name ?: "Unknown Status"
                ApplicationsValues(
                    applicationId = app.id,
                    companyName = companyName,
                    jobTitle = app.name,
                    statusId = statusId,
                    status = statusName,
                    notes = app.notes ?: "",
                    applicationDate = app.dateApplied.toString(),
                    applicationURL = app.applicationURL,
                    applicationLocation = app.location
                )
            }

            _applications.postValue(valuesList)
            _companies.postValue(companies)
            _statuses.postValue(statuses)

        }
    }

    //endregion

    //region add data
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
            applicationRepository.insertApplication(application)
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
            loadCompanies()
        }
    }

    //endregion

    //region delete data
    fun deleteApplication(applicationID: Int){
        viewModelScope.launch {
            val application = withContext(Dispatchers.IO){
                applicationRepository.getApplicationById(applicationID)
            }

            applicationRepository.deleteApplication(application)
        }
    }

    //endregion

    fun updateApplication(id:Int, companyId: Int, jobTitle: String, location: String, dateApplied: String, applicationUrl: String, statusId: Int, notes: String){
        viewModelScope.launch {
            try {
                val userFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                val parsedDate = LocalDate.parse(dateApplied, userFormatter)

                val application = Application(
                    id = id,
                    name = jobTitle,
                    location = location,
                    dateApplied = parsedDate,
                    applicationURL = applicationUrl,
                    statusID = statusId,
                    companyID = companyId,
                    notes = notes
                )
                applicationRepository.updateApplication(application)
                _updateApplicationStatus.postValue(true)
            } catch (e: Exception) {
                _updateApplicationStatus.postValue(false)
            }
        }
    }
}

