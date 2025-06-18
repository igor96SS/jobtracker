package pt.iscode.gestorcandidaturas

import android.content.Context
import androidx.core.content.ContextCompat

object StatusManager {

    private val translationsMap: Map<String, Int> = mapOf(
        "Applied" to R.string.status_applied,
        "Refused" to R.string.status_refused,
        "Interview" to R.string.status_interview,
        "Waiting" to R.string.status_waiting
    )

    private val colorMap: Map<Int, Int> = mapOf(
        1 to R.color.status_blue,
        2 to R.color.status_red,
        3 to R.color.status_yellow,
        4 to R.color.status_green
    )

    fun translate(context: Context, statusName: String): String {
        val resId = translationsMap[statusName] ?: return statusName
        return context.getString(resId)
    }

    fun getStatusColor(context: Context, statusId: Int): Int {
        val colorResId = colorMap[statusId] ?: R.color.status_gray
        return ContextCompat.getColor(context, colorResId)
    }
}


