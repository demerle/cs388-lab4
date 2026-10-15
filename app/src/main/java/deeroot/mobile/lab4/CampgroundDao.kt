package deeroot.mobile.lab4

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// DAO that provides access to the cached campground table.
@Dao
interface CampgroundDao {
    @Query("SELECT * FROM campground_table")
    fun getAll(): Flow<List<CampgroundEntity>>

    @Insert
    fun insertAll(campgrounds: List<CampgroundEntity>)

    @Query("DELETE FROM campground_table")
    fun deleteAll()
}
