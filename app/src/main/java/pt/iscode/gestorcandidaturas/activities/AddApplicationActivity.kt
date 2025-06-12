package pt.iscode.gestorcandidaturas.activities

import android.app.DatePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import pt.iscode.gestorcandidaturas.AppDatabase
import pt.iscode.gestorcandidaturas.R
import pt.iscode.gestorcandidaturas.databinding.ActivityAddApplicationBinding
import pt.iscode.gestorcandidaturas.repositories.ApplicationRepository
import pt.iscode.gestorcandidaturas.repositories.CompanyRepository
import pt.iscode.gestorcandidaturas.repositories.StatusRepository
import pt.iscode.gestorcandidaturas.viewModels.ApplicationViewModel
import pt.iscode.gestorcandidaturas.viewModels.ApplicationViewModelFactory
import java.util.Locale

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

        //Open Add Company Dialog
        openCompanyDialog()

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
                    val dateString = String.format(Locale.FRANCE,"%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear)

                    binding.datePickerInput.setText(dateString)
            }, year, month, day
            )
            datePickerDialog.show()
        }
    }

    private fun populateSpinners() {
        viewModel.companiesLiveData.observe(this) { companyList ->
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

        viewModel.statusesLiveData.observe(this) { statusList ->
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

    private fun openCompanyDialog(){
        binding.companyBTN.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.layout_company_dialog, null)
            val alertDialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .setNegativeButton("Cancel",null)
                .setPositiveButton("Save",null)
                .create()

            alertDialog.setOnShowListener {
                val companyName = dialogView.findViewById<TextInputEditText>(R.id.companyNameText)
                val companyWebsite = dialogView.findViewById<TextInputEditText>(R.id.companyWebsiteText)
                val companyLinkedin = dialogView.findViewById<TextInputEditText>(R.id.companyLinkedinText)

                val saveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                saveButton.setOnClickListener {
                    val name = companyName.text?.toString()?.trim()
                    val website = companyWebsite.text?.toString()?.trim() ?: ""
                    val linkedin = companyLinkedin.text?.toString()?.trim()?: ""

                    if (name.isNullOrEmpty()){
                        companyName.error = "Company Name is required"
                        return@setOnClickListener
                    }

                    viewModel.addCompany(name,website, linkedin)
                    viewModel.loadCompanies()
                    alertDialog.dismiss()
                }
            }
            alertDialog.show()
        }
    }

    private fun saveApplication() {
        binding.saveApplications.setOnClickListener {
            val selectedCompanyPosition = binding.companySpinner.selectedItemPosition
            val companyList = viewModel.companiesLiveData.value
            val companyId = companyList?.getOrNull(selectedCompanyPosition)?.id
                ?: run {
                    Toast.makeText(this, "Selecione uma empresa válida", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

            val statusName = binding.statusDropdown.text.toString()
            val statusList = viewModel.statusesLiveData.value
            val statusId = statusList?.find { it.name == statusName }?.id
                ?: run {
                    Toast.makeText(this, "Selecione um status válido", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

            val jobTitle = binding.jobTitleText.text.toString().trim()
            val jobUrl = binding.jobUrlText.text.toString().trim()
            val jobLocation = binding.jobLocationText.text.toString().trim()
            val applicationDate = binding.datePickerInput.text.toString().trim()
            val notes = binding.notesInput.text.toString().trim()

            viewModel.addApplications(
                companyId,
                jobTitle,
                jobLocation,
                applicationDate,
                jobUrl,
                statusId,
                notes
            )
        }
    }


    private fun reposInitialization(){
        val db = AppDatabase.getDatabase(this)

        applicationRepository = ApplicationRepository(db.applicationDao())
        companyRepository = CompanyRepository(db.companyDao())
        statusRepository = StatusRepository(db.statusDao())
    }

}