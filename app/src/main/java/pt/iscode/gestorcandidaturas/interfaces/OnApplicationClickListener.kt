package pt.iscode.gestorcandidaturas.interfaces

import pt.iscode.gestorcandidaturas.models.ApplicationsValues

interface OnApplicationClickListener {
    fun onApplicationClick(application: ApplicationsValues)
}