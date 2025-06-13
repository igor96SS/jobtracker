package pt.iscode.gestorcandidaturas.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import pt.iscode.gestorcandidaturas.AppDatabase
import pt.iscode.gestorcandidaturas.R
import pt.iscode.gestorcandidaturas.adapters.ApplicationsAdapter
import pt.iscode.gestorcandidaturas.databinding.ActivityMainBinding
import pt.iscode.gestorcandidaturas.interfaces.OnApplicationItemClickListener
import pt.iscode.gestorcandidaturas.repositories.ApplicationRepository
import pt.iscode.gestorcandidaturas.repositories.CompanyRepository
import pt.iscode.gestorcandidaturas.repositories.StatusRepository
import pt.iscode.gestorcandidaturas.viewModels.ApplicationViewModel
import pt.iscode.gestorcandidaturas.viewModels.ApplicationViewModelFactory

class MainActivity : AppCompatActivity(), OnApplicationItemClickListener {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val db = AppDatabase.getDatabase(this)

        val applicationRepository = ApplicationRepository(db.applicationDao())
        val companyRepository = CompanyRepository(db.companyDao())
        val statusRepository = StatusRepository(db.statusDao())

        val viewModelFactory = ApplicationViewModelFactory(
            applicationRepository,
            companyRepository,
            statusRepository
        )
        val viewModel = ViewModelProvider(this, viewModelFactory)[ApplicationViewModel::class.java]

        val adapter = ApplicationsAdapter(this)
        binding.applicationsListRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.applicationsListRecyclerView.adapter = adapter

        viewModel.applications.observe(this) { list ->
            adapter.submitList(list)
        }

        viewModel.loadAllData()

        binding.mainFloatBTN.setOnClickListener {

            startActivity(Intent(this, AddApplicationActivity::class.java))
        }

    }

    // RecyclerView Item click
    // opens details activity with selected application ID
    override fun onApplicationItemClick(applicationId: Int) {
        val intent = Intent(this, ApplicationDetailsActivity::class.java)
        intent.putExtra("applicationID", applicationId)
        startActivity(intent)
    }
}