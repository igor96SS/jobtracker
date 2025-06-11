package pt.iscode.gestorcandidaturas.activities

import android.app.DatePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import pt.iscode.gestorcandidaturas.AppDatabase
import pt.iscode.gestorcandidaturas.R
import pt.iscode.gestorcandidaturas.databinding.ActivityAddApplicationBinding
import pt.iscode.gestorcandidaturas.repositories.ApplicationRepository
import pt.iscode.gestorcandidaturas.repositories.CompanyRepository
import pt.iscode.gestorcandidaturas.repositories.StatusRepository
import pt.iscode.gestorcandidaturas.viewModels.ApplicationViewModel
import pt.iscode.gestorcandidaturas.viewModels.ApplicationViewModelFactory

class AddApplicationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddApplicationBinding

    private lateinit var applicationRepository: ApplicationRepository
    private lateinit var companyRepository: CompanyRepository
    private lateinit var statusRepository: StatusRepository

    // ViewModel initialization
    private val viewModel: ApplicationViewModel by viewModels {
        ApplicationViewModelFactory(applicationRepository, companyRepository, statusRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddApplicationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //Initializing Repositories
        reposInitialization()

        //Select Application Date
        selectDate()

        //Populating Spinners/DropDowns
        populateSpinners()

        //Save Application
        saveApplication()

    }

    private fun selectDate(){
        binding.datePickerInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this,
                { _,selectedYear, selectedMonth, selectedDay ->
                    val dateString = "${selectedDay}/${selectedMonth+1}/${selectedYear}"
                    binding.datePickerInput.setText(dateString)
            }, year, month, day
            )
            datePickerDialog.show()
        }
    }

    private fun populateSpinners() {
        viewModel.companies.observe(this) { companyList ->
            val names = if (companyList.isNotEmpty()) {
                companyList.map { it.name }
            } else {
                listOf("Add a Company") // ToDo change to a string.xml
            }
            val companyAdapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                names
            )
            companyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.companySpinner.adapter = companyAdapter
        }

        viewModel.statuses.observe(this) { statusList ->
            val statusAdapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                statusList.map { it.name }
            )
            binding.statusDropdown.setAdapter(statusAdapter)
        }

        // Calling data functions
        viewModel.loadCompanies()
        viewModel.loadStatus()
    }

    private fun saveApplication(){
        binding.saveApplications.setOnClickListener {
            val companyId = binding.companySpinner.selectedItemId.toString().toInt()
            val jobTitle = binding.jobTitleText.text.toString()
            val jobUrl = binding.jobUrlText.text.toString()
            val jobLocation = binding.jobLocationText.toString()
            val applicationDate = binding.datePickerInput.text.toString()
            val notes = binding.notesInput.text.toString()
            val status = binding.statusDropdown.id

            viewModel.addApplications(companyId,jobTitle, jobLocation, applicationDate, jobUrl, status, notes)
        }
    }

    private fun reposInitialization(){
        val db = AppDatabase.getDatabase(this)

        applicationRepository = ApplicationRepository(db.applicationDao())
        companyRepository = CompanyRepository(db.companyDao())
        statusRepository = StatusRepository(db.statusDao())
    }

}