package pt.iscode.gestorcandidaturas.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
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
    private lateinit var viewModel: ApplicationViewModel

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
        viewModel = ViewModelProvider(this, viewModelFactory)[ApplicationViewModel::class.java]

        val adapter = ApplicationsAdapter(this)
        binding.applicationsListRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.applicationsListRecyclerView.adapter = adapter

        viewModel.applications.observe(this) { list ->
            adapter.submitList(list)
            updateUI(list.isEmpty())
        }

        viewModel.loadAllData()

        binding.addApplicationButton.setOnClickListener {
            startActivity(Intent(this, AddApplicationActivity::class.java))
        }

        binding.mainFloatBTN.setOnClickListener {
            startActivity(Intent(this, AddApplicationActivity::class.java))
        }

    }

    private fun updateUI(isEmpty: Boolean){
        if (isEmpty){
            binding.mainFloatBTN.visibility = View.GONE
            binding.linearLayout.visibility = View.GONE
            binding.centerContainer.visibility = View.VISIBLE
        }else{
            binding.mainFloatBTN.visibility = View.VISIBLE
            binding.linearLayout.visibility = View.VISIBLE
            binding.centerContainer.visibility = View.GONE
        }
    }

    //Update recyclerView after changes
    override fun onResume() {
        super.onResume()
        viewModel.loadAllData()
    }


    // RecyclerView Item click
    // opens details activity with selected application ID
    override fun onApplicationItemClick(applicationId: Int) {
        val intent = Intent(this, ApplicationDetailsActivity::class.java)
        intent.putExtra("applicationID", applicationId)
        startActivity(intent)
    }
}