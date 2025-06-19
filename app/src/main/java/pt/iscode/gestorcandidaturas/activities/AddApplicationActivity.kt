package pt.iscode.gestorcandidaturas.activities

import android.app.DatePickerDialog
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.util.TypedValue
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import pt.iscode.gestorcandidaturas.AppDatabase
import pt.iscode.gestorcandidaturas.R
import pt.iscode.gestorcandidaturas.StatusManager
import pt.iscode.gestorcandidaturas.ToolbarManager
import pt.iscode.gestorcandidaturas.databinding.ActivityAddApplicationBinding
import pt.iscode.gestorcandidaturas.entities.Status
import pt.iscode.gestorcandidaturas.repositories.ApplicationRepository
import pt.iscode.gestorcandidaturas.repositories.CompanyRepository
import pt.iscode.gestorcandidaturas.repositories.StatusRepository
import pt.iscode.gestorcandidaturas.viewModels.ApplicationViewModel
import pt.iscode.gestorcandidaturas.viewModels.ApplicationViewModelFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class AddApplicationActivity : AppCompatActivity(){
    private lateinit var binding: ActivityAddApplicationBinding

    private lateinit var applicationRepository: ApplicationRepository
    private lateinit var companyRepository: CompanyRepository
    private lateinit var statusRepository: StatusRepository

    // ViewModel initialization
    private val viewModel: ApplicationViewModel by viewModels {
        ApplicationViewModelFactory(applicationRepository, companyRepository, statusRepository)
    }

    private var applicationID: Int = -1

    private lateinit var companyAdapter: ArrayAdapter<String>
    private lateinit var statusAdapter: ArrayAdapter<String>

    private lateinit var translatedStatusList: List<String>
    private lateinit var originalStatusList: List<Status>
    private var statusToSelectFromDb: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddApplicationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Add this because Scroll in android 15+
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right,
                systemBars.bottom + imeInsets.bottom)
            insets
        }

        //Initializing toolbar
        ToolbarManager(this).setup(
            title = resources.getString(R.string.toolbar_title),
        )

        //Initializing Repositories
        reposInitialization()

        //Open Add Company Dialog
        openCompanyDialog()

        //Select Application Date
        selectDate()

        //Populating Spinners/DropDowns
        populateCompanyDropDown()
        populateStatusDropDown()

        // Verify edit mode
        applicationID = intent.getIntExtra("editApplicationID", -1)
        if (applicationID > -1) {
            binding.saveApplications.text = resources.getString(R.string.update_application_button)
            saveApplication(true)
            populateDataEdit(applicationID)
        } else {
            saveApplication(false)
        }

        //Error listeners
        setupErrorListeners()

    }

    private fun populateDataEdit(applicationId: Int) {
        viewModel.loadApplicationById(applicationId)

        viewModel.applicationDetail.observe(this) { applicationValues ->
            binding.jobTitleText.setText(applicationValues.jobTitle)
            binding.jobUrlText.setText(applicationValues.applicationURL)
            val parsedDate = LocalDate.parse(applicationValues.applicationDate)
            val userFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            binding.datePickerInput.setText(parsedDate.format(userFormatter))
            binding.notesInput.setText(applicationValues.notes)
            binding.jobLocationText.setText(applicationValues.applicationLocation)

            //Check if adapter is initialized
            if (::companyAdapter.isInitialized) {
                val index = companyAdapter.getPosition(applicationValues.companyName)
                binding.companyDropdown.setText(companyAdapter.getItem(index), false)
            }

            if (::statusAdapter.isInitialized) {
                val translatedStatus = StatusManager.translate(this, applicationValues.status)
                val index = statusAdapter.getPosition(translatedStatus)
                if (index >= 0) {
                    binding.statusDropdown.setText(statusAdapter.getItem(index), false)
                }
            } else {
                // Save status to when adapter is initialized
                statusToSelectFromDb = StatusManager.translate(this, applicationValues.status)
            }
        }
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
            }, year, month, day)

            datePickerDialog.setOnShowListener {
                val typedValue = TypedValue()
                theme.resolveAttribute(com.google.android.material.R.attr.colorSecondary, typedValue, true)
                val color = typedValue.data

                datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE)?.setTextColor(color)
                datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE)?.setTextColor(color)
            }


            datePickerDialog.show()
        }
    }

    private fun populateCompanyDropDown(){
        viewModel.companiesLiveData.observe(this) { companyList ->
            val names = if (companyList.isNotEmpty()) {
                companyList.map { it.name }
            } else {
                listOf(resources.getString(R.string.empty_company_dropdown))
            }
            companyAdapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                names
            )
            binding.companyDropdown.setAdapter(companyAdapter)
            companyAdapter.notifyDataSetChanged()
        }

        viewModel.loadCompanies()
    }

    private fun populateStatusDropDown() {
        viewModel.statusesLiveData.observe(this) { statusList ->
            originalStatusList = statusList
            translatedStatusList = statusList.map { StatusManager.translate(this, it.name) }

            statusAdapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                translatedStatusList
            )
            binding.statusDropdown.setAdapter(statusAdapter)

            // Apply BD status before adapter ready
            statusToSelectFromDb?.let {
                val index = statusAdapter.getPosition(it)
                if (index >= 0) {
                    binding.statusDropdown.setText(statusAdapter.getItem(index), false)
                }
                statusToSelectFromDb = null
            }
        }

        // Calling data functions
        viewModel.loadStatus()
    }

    private fun openCompanyDialog(){
        binding.companyBTN.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.layout_company_dialog, null)
            val alertDialog = AlertDialog.Builder(this, R.style.MyAlertDialogTheme)
                .setView(dialogView)
                .setNegativeButton(resources.getString(R.string.dialog_cancel_button),null)
                .setPositiveButton(resources.getString(R.string.dialog_save_button),null)
                .create()

            alertDialog.setOnShowListener {
                val companyName = dialogView.findViewById<TextInputEditText>(R.id.companyNameText)
                val companyWebsite = dialogView.findViewById<TextInputEditText>(R.id.companyWebsiteText)
                val companyLinkedin = dialogView.findViewById<TextInputEditText>(R.id.companyLinkedinText)
                val companyNameLayout = dialogView.findViewById<TextInputLayout>(R.id.companyNameLayout)

                // Form listener
                companyName.doOnTextChanged { text, _, _, _ ->
                    if (text.isNullOrBlank()) {
                        companyNameLayout.error = getString(R.string.empty_text)
                    } else {
                        companyNameLayout.error = null
                    }
                }

                val saveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                saveButton.setOnClickListener {
                    val name = companyName.text?.toString()?.trim()
                    val website = companyWebsite.text?.toString()?.trim() ?: ""
                    val linkedin = companyLinkedin.text?.toString()?.trim()?: ""

                    // Form Validate
                    var isValid = true
                    if (companyName.text.isNullOrBlank()){
                        companyNameLayout.error = resources.getString(R.string.empty_text)
                        isValid = false
                    }

                    if (!isValid) {
                        return@setOnClickListener
                    }

                    if (name!= null){
                        // Check if company exists
                        lifecycleScope.launch {
                            val exists = viewModel.companyExists(name)

                            if (exists) {
                                companyNameLayout.error = getString(R.string.existing_company)
                                return@launch
                            }

                            viewModel.addCompany(name, website, linkedin)
                            alertDialog.dismiss()
                        }
                    }

                }
            }
            alertDialog.show()
        }
    }

    //Save and Edit Application
    private fun saveApplication(isEdit: Boolean) {
        binding.saveApplications.setOnClickListener {
            val companyName = binding.companyDropdown.text.toString()
            val companyList = viewModel.companiesLiveData.value
            val companyId = companyList?.find {it.name == companyName}?.id
                ?: run {
                    Toast.makeText(this, resources.getString(R.string.company_error_select), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

            val selectedStatusText = binding.statusDropdown.text.toString()
            val selectedIndex = translatedStatusList.indexOf(selectedStatusText)
            if (selectedIndex == -1) {
                Toast.makeText(this, resources.getString(R.string.status_error_select), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val statusId = originalStatusList[selectedIndex].id

            var isValid = true
            binding.jobTitleInput.error = null
            binding.jobLocationInput.error = null
            binding.dateLayout.error = null

            if (binding.jobTitleText.text.isNullOrBlank()) {
                binding.jobTitleInput.error = getString(R.string.empty_text)
                isValid = false
            } else {
                binding.jobTitleInput.error = null
            }

            if (binding.jobLocationText.text.isNullOrBlank()) {
                binding.jobLocationInput.error = getString(R.string.empty_text)
                isValid = false
            } else {
                binding.jobLocationInput.error = null
            }

            if (binding.datePickerInput.text.isNullOrBlank()) {
                binding.dateLayout.error = getString(R.string.empty_text)
                isValid = false
            } else {
                binding.dateLayout.error = null
            }

            if (!isValid) {
                return@setOnClickListener
            }


            val jobTitle = binding.jobTitleText.text.toString().trim()
            val jobUrl = binding.jobUrlText.text.toString().trim()
            val jobLocation = binding.jobLocationText.text.toString().trim()
            val applicationDate = binding.datePickerInput.text.toString().trim()
            val notes = binding.notesInput.text.toString().trim()


            // Edit mode
            if (isEdit){
                viewModel.updateApplication(
                    applicationID,
                    companyId,
                    jobTitle,
                    jobLocation,
                    applicationDate,
                    jobUrl,
                    statusId,
                    notes
                )
                val intent = Intent(this, ApplicationDetailsActivity::class.java)
                intent.putExtra("applicationID", applicationID)
                Toast.makeText(this, resources.getString(R.string.toast_application_updated), Toast.LENGTH_SHORT).show()
                startActivity(intent)
                finish()
            } else{
                viewModel.addApplications(
                    companyId,
                    jobTitle,
                    jobLocation,
                    applicationDate,
                    jobUrl,
                    statusId,
                    notes
                )
                Toast.makeText(this, resources.getString(R.string.toast_application_saved), Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }
    private fun setupErrorListeners() {
        binding.jobTitleText.doOnTextChanged { text, _, _, _ ->
            if (text.isNullOrBlank()) {
                binding.jobTitleInput.error = getString(R.string.empty_text)
            } else {
                binding.jobTitleInput.error = null
            }
        }

        binding.jobLocationText.doOnTextChanged { text, _, _, _ ->
            if (text.isNullOrBlank()) {
                binding.jobLocationInput.error = getString(R.string.empty_text)
            } else {
                binding.jobLocationInput.error = null
            }
        }

        binding.datePickerInput.doOnTextChanged { text, _, _, _ ->
            if (text.isNullOrBlank()) {
                binding.dateLayout.error = getString(R.string.empty_text)
            } else {
                binding.dateLayout.error = null
            }
        }

    }

    private fun reposInitialization(){
        val db = AppDatabase.getDatabase(this)

        applicationRepository = ApplicationRepository(db.applicationDao())
        companyRepository = CompanyRepository(db.companyDao())
        statusRepository = StatusRepository(db.statusDao())
    }

}