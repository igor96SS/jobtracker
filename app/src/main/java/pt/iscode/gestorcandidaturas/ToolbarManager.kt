package pt.iscode.gestorcandidaturas

import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class ToolbarManager(private val activity: AppCompatActivity) {

    fun setup(title: String, showEdit: Boolean = false, showDelete: Boolean = false,
        onEditClick: (() -> Unit)? = null,
        onDeleteClick: (() -> Unit)? = null,
    ) {
        val toolbar = activity.findViewById<Toolbar>(R.id.toolbar)
        activity.setSupportActionBar(toolbar)
        activity.supportActionBar?.setDisplayShowTitleEnabled(false)

        val titleText = activity.findViewById<TextView>(R.id.labelText)
        val buttonBack = activity.findViewById<ImageButton>(R.id.buttonBack)
        val buttonDelete = activity.findViewById<ImageButton>(R.id.buttonDelete)
        val buttonEdit = activity.findViewById<ImageButton>(R.id.buttonEdit)

        titleText.text = title
        buttonEdit.visibility = if (showEdit) View.VISIBLE else View.GONE
        buttonDelete.visibility = if (showDelete) View.VISIBLE else View.GONE

        //Back button click
        buttonBack.setOnClickListener {
            activity.onBackPressedDispatcher.onBackPressed()
        }

        //Button edit click
        buttonEdit.setOnClickListener {
            onEditClick?.invoke()
        }

        //Button delete click
        buttonDelete.setOnClickListener {
            onDeleteClick?.invoke()
        }
    }
}
