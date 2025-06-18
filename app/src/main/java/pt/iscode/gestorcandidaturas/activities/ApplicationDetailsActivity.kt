package pt.iscode.gestorcandidaturas.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import pt.iscode.gestorcandidaturas.AppDatabase
import pt.iscode.gestorcandidaturas.R
import pt.iscode.gestorcandidaturas.StatusTranslator
import pt.iscode.gestorcandidaturas.ToolbarManager
import pt.iscode.gestorcandidaturas.databinding.ActivityApplicationDetailsBinding
import pt.iscode.gestorcandidaturas.repositories.ApplicationRepository
import pt.iscode.gestorcandidaturas.repositories.CompanyRepository
import pt.iscode.gestorcandidaturas.repositories.StatusRepository
import pt.iscode.gestorcandidaturas.viewModels.ApplicationViewModel
import pt.iscode.gestorcandidaturas.viewModels.ApplicationViewModelFactory

class ApplicationDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityApplicationDetailsBinding

    private lateinit var applicationRepository: ApplicationRepository
    private lateinit var companyRepository: CompanyRepository
    private lateinit var statusRepository: StatusRepository

    // ViewModel initialization
    private val viewModel: ApplicationViewModel by viewModels {
        ApplicationViewModelFactory(applicationRepository, companyRepository, statusRepository)
    }

    private var applicationID: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityApplicationDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        //Initializing Repositories
        //needs to be called in first
        reposInitialization()

        // get application id and send it to the function to populate UI
        applicationID = intent.getIntExtra("applicationID", -1)
        populateData(applicationID)



        //Initializing toolbar
        ToolbarManager(this).setup(
            title = resources.getString(R.string.toolbar_title),
            showEdit = true,
            showDelete = true,
            onEditClick = { updateApplication(applicationID) },
            onDeleteClick = { deleteApplication()}
        )

    }

    // Edit button action
    private fun updateApplication(applicationId: Int){
        val intent = Intent(this, AddApplicationActivity::class.java)
        intent.putExtra("editApplicationID",applicationId)
        startActivity(intent)
        finish()
    }

    // Delete button action
    private fun deleteApplication() {
        AlertDialog.Builder(this)
            .setTitle(resources.getString(R.string.dialog_delete_title))
            .setMessage(resources.getString(R.string.dialog_delete_message))
            .setPositiveButton(resources.getString(R.string.dialog_confirm_button)) { _, _ ->
                viewModel.deleteApplication(applicationID)
                Toast.makeText(this, resources.getString(R.string.dialog_delete_toast_confirmation), Toast.LENGTH_SHORT).show()
                finish()
            }
            .setNegativeButton(resources.getString(R.string.dialog_cancel_button), null)
            .show()
    }

    private fun populateData(applicationID: Int) {

        viewModel.loadApplicationById(applicationID)

        viewModel.applicationDetail.observe(this) { applicationValues ->
            binding.jobTitleTextView.text = applicationValues.jobTitle
            binding.companyNameTextView.text = applicationValues.companyName

            binding.urlHeader.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_url_link, 0, 0, 0)
            binding.jobUrlTextView.text = applicationValues.applicationURL

            binding.appliedAtTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_calendar, 0, 0, 0)
            binding.appliedAtTextView.text = applicationValues.applicationDate

            binding.jobStatusTextView.text = StatusTranslator.translate(this, applicationValues.status)

            binding.notesHeader.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_note, 0, 0, 0)
            binding.notesTextView.text = applicationValues.notes

            binding.jobLocationTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_location_pin, 0, 0, 0)
            binding.jobLocationTextView.text = applicationValues.applicationLocation
        }
    }

    private fun reposInitialization(){
        val db = AppDatabase.getDatabase(this)

        applicationRepository = ApplicationRepository(db.applicationDao())
        companyRepository = CompanyRepository(db.companyDao())
        statusRepository = StatusRepository(db.statusDao())
    }
}