package pt.iscode.gestorcandidaturas.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Status::class,
            parentColumns = ["id"],
            childColumns = ["statusID"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = Company::class,
            parentColumns = ["id"],
            childColumns = ["companyID"],
            onDelete = ForeignKey.RESTRICT
        )
    ]
)
data class Application(
    @PrimaryKey(autoGenerate = true) val id:Int = 0,
    @ColumnInfo(name = "jobTitle") val name: String,
    @ColumnInfo(name = "location") val location: String,
    @ColumnInfo(name = "date_applied") val dateApplied: LocalDate,
    @ColumnInfo(name = "application_url") val applicationURL: String?,
    @ColumnInfo(name = "status_id") val statusID: Int,
    @ColumnInfo(name = "company_id") val companyID: Int,
    @ColumnInfo(name = "notes") val notes: String?
)
