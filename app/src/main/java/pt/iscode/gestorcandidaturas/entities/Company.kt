package pt.iscode.gestorcandidaturas.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Company(
    @PrimaryKey(autoGenerate = true) val id:Int = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "website") val website: String,
    @ColumnInfo(name = "linkedin_url") val linkedinUrl: String?
)
