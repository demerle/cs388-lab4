package deeroot.mobile.lab4

import android.app.Application

// Application class that holds the single Room database instance.
class CampgroundApplication : Application() {
    val db by lazy { AppDatabase.getInstance(this) }
}
