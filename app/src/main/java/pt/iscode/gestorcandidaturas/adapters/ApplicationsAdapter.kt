package pt.iscode.gestorcandidaturas.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import pt.iscode.gestorcandidaturas.R
import pt.iscode.gestorcandidaturas.StatusTranslator
import pt.iscode.gestorcandidaturas.interfaces.OnApplicationItemClickListener
import pt.iscode.gestorcandidaturas.models.ApplicationsValues

class ApplicationsAdapter(
    private val listener: OnApplicationItemClickListener
) : RecyclerView.Adapter<ApplicationsAdapter.ApplicationViewHolder>() {

    private var applicationsList: List<ApplicationsValues> = emptyList()

    fun submitList(newList: List<ApplicationsValues>) {
        applicationsList = newList
        notifyDataSetChanged()
    }

    inner class ApplicationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val companyName: TextView = itemView.findViewById(R.id.companyName)
        private val jobTitle: TextView = itemView.findViewById(R.id.jobPosition)
        private val statusName: TextView = itemView.findViewById(R.id.statusName)
        private val notes: TextView = itemView.findViewById(R.id.notes)
        private val applicationDate: TextView = itemView.findViewById(R.id.applicationDate)
        private val jobLocation: TextView = itemView.findViewById(R.id.jobLocation)

        fun bind(application: ApplicationsValues) {
            companyName.text = application.companyName
            jobTitle.text = application.jobTitle
            statusName.text = StatusTranslator.translate(itemView.context, application.status)
            notes.text = application.notes
            applicationDate.text = application.applicationDate
            jobLocation.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_location_pin, 0, 0, 0)
            jobLocation.text = application.applicationLocation

            itemView.setOnClickListener {
                listener.onApplicationItemClick(application.applicationId)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApplicationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.application_item, parent, false)
        return ApplicationViewHolder(view)
    }

    override fun onBindViewHolder(holder: ApplicationViewHolder, position: Int) {
        holder.bind(applicationsList[position])
    }

    override fun getItemCount(): Int = applicationsList.size
}
