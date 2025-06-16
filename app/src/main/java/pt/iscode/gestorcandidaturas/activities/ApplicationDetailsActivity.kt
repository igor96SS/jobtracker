package pt.iscode.gestorcandidaturas.activities

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import pt.iscode.gestorcandidaturas.AppDatabase
import pt.iscode.gestorcandidaturas.R
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
        applicationID = intent.getIntExtra("applicationID", 0)
        populateData(applicationID)



        //Initializing toolbar
        ToolbarManager(this).setup(
            title = "Your Applications",
            showEdit = true,
            showDelete = true,
            onEditClick = {  },
            onDeleteClick = { deleteApplication()}
        )

    }

    private fun deleteApplication() {
        AlertDialog.Builder(this)
            .setTitle("Delete Confirmation")
            .setMessage("Are you sure you want to delete?")
            .setPositiveButton("Yes") { _, _ ->
                viewModel.deleteApplication(applicationID)
                Toast.makeText(this, "Application deleted", Toast.LENGTH_SHORT).show()
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun populateData(applicationID: Int) {

        viewModel.loadApplicationById(applicationID)

        viewModel.applicationDetail.observe(this) { applicationValues ->
            binding.jobTitleTextView.text = applicationValues.jobTitle
            binding.companyNameTextView.text = applicationValues.companyName
            binding.jobUrlTextView.text = applicationValues.applicationURL
            binding.appliedAtTextView.text = applicationValues.applicationDate
            binding.jobStatusTextView.text = applicationValues.status
            binding.notesTextView.text = applicationValues.notes
        }
    }

    private fun reposInitialization(){
        val db = AppDatabase.getDatabase(this)

        applicationRepository = ApplicationRepository(db.applicationDao())
        companyRepository = CompanyRepository(db.companyDao())
        statusRepository = StatusRepository(db.statusDao())
    }
}