package deeroot.mobile.lab4

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide

class DetailActivity : AppCompatActivity() {
    private lateinit var campgroundNameTV: TextView
    private lateinit var campgroundDescriptionTV: TextView
    private lateinit var campgroundLatLongTV: TextView
    private lateinit var campgroundImageIV: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detail)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.detail)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Find the remaining Views for the screen
        campgroundNameTV = findViewById(R.id.campgroundName)
        campgroundDescriptionTV = findViewById(R.id.campgroundDescription)
        campgroundLatLongTV = findViewById(R.id.campgroundLocation)
        campgroundImageIV = findViewById(R.id.campgroundImage)

        // Get the extra from the Intent
        val campground = intent.getSerializableExtra(CAMPGROUND_EXTRA) as Campground

        // Set the name, description, and latLong information from the campground
        campgroundNameTV.text = campground.name
        campgroundDescriptionTV.text = campground.description
        campgroundLatLongTV.text = campground.latLong

        // Load the image using Glide
        Glide.with(this)
            .load(campground.imageUrl)
            .into(campgroundImageIV)
    }
}
