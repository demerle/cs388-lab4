package deeroot.mobile.lab4

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// Room entity representing one cached campground row in the local database.
@Entity(tableName = "campground_table")
data class CampgroundEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "name")
    val name: String?,
    @ColumnInfo(name = "description")
    val description: String?,
    @ColumnInfo(name = "latLong")
    val latLong: String?,
    @ColumnInfo(name = "imageUrl")
    val imageUrl: String?
)
