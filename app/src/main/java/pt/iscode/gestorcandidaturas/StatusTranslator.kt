package pt.iscode.gestorcandidaturas

import android.content.Context

object StatusTranslator {

    private val translationsMap: Map<String, Int> = mapOf(
        "Applied" to R.string.status_applied,
        "Refused" to R.string.status_refused,
        "Interview" to R.string.status_interview,
        "Waiting" to R.string.status_waiting
    )

    fun translate(context: Context, statusName: String): String {
        val resId = translationsMap[statusName] ?: return statusName
        return context.getString(resId)
    }
}

