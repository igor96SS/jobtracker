package pt.iscode.gestorcandidaturas.models

data class ApplicationsValues(val applicationId: Int, val companyName: String, val jobTitle: String, val statusId: Int? = 0, val status: String, val notes: String, val applicationDate: String,val applicationURL: String?, val applicationLocation: String)
