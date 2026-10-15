package deeroot.mobile.lab4

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codepath.asynchttpclient.AsyncHttpClient
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.Headers
import org.json.JSONException

class MainActivity : AppCompatActivity() {
    // List of campgrounds fetched from the server
    private val campgrounds = mutableListOf<Campground>()
    private lateinit var campgroundsRecyclerView: RecyclerView
    private lateinit var campgroundAdapter: CampgroundAdapter

    companion object {
        private const val TAG = "MainActivity"
        private val PARKS_API_KEY = BuildConfig.API_KEY
        private val CAMPGROUND_URL =
            "https://developer.nps.gov/api/v1/campgrounds?api_key=$PARKS_API_KEY"
        private const val MAX_RETRIES = 3        // extra attempts after the first one fails
        private const val RETRY_DELAY_MS = 2000L
    }

    private fun createJson() = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set up CampgroundAdapter with campgrounds
        campgroundsRecyclerView = findViewById(R.id.campgroundsRecyclerView)
        campgroundsRecyclerView.layoutManager = LinearLayoutManager(this)
        campgroundsRecyclerView.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        )
        campgroundAdapter = CampgroundAdapter(this, campgrounds)
        campgroundsRecyclerView.adapter = campgroundAdapter

        // Step 7: Load items from the database. The database is the single
        // source of truth, so the UI is updated whenever its contents change.
        lifecycleScope.launch {
            (application as CampgroundApplication).db.campgroundDao().getAll().collect { databaseList ->
                val mappedList = databaseList.map { entity ->
                    Campground(
                        entity.name,
                        entity.description,
                        entity.latLong,
                        listOf(CampgroundImage(entity.imageUrl, null))
                    )
                }
                campgrounds.clear()
                campgrounds.addAll(mappedList)
                campgroundAdapter.notifyDataSetChanged()
            }
        }

        fetchCampgrounds()
    }

    // Fetches the campground list from the NPS API. If the API is unavailable
    // (it occasionally returns transient errors like HTTP 500), we retry a few
    // times before telling the user instead of leaving a blank screen.
    private fun fetchCampgrounds(retryCount: Int = 0) {
        val client = AsyncHttpClient()
        client.get(CAMPGROUND_URL, object : JsonHttpResponseHandler() {
            override fun onFailure(
                statusCode: Int,
                headers: Headers?,
                response: String,
                throwable: Throwable?
            ) {
                Log.e(TAG, "Failed to fetch campgrounds: $statusCode")
                if (retryCount < MAX_RETRIES) {
                    Toast.makeText(
                        this@MainActivity,
                        "Campground server returned $statusCode — retrying (${retryCount + 1}/$MAX_RETRIES)…",
                        Toast.LENGTH_SHORT
                    ).show()
                    Handler(Looper.getMainLooper()).postDelayed(
                        { fetchCampgrounds(retryCount + 1) },
                        RETRY_DELAY_MS
                    )
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Couldn't load campgrounds. Check your connection and try again later.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onSuccess(
                statusCode: Int,
                headers: Headers,
                json: JsonHttpResponseHandler.JSON
            ) {
                Log.i(TAG, "Successfully fetched campgrounds: $json")
                try {
                    // Create the parsedJSON
                    val parsedJson = createJson().decodeFromString(
                        CampgroundResponse.serializer(),
                        json.jsonObject.toString()
                    )

                    // Instead of updating the UI directly, cache the fresh
                    // data in the database (delete old cache entries first).
                    // The Flow collector in onCreate() handles the UI update.
                    parsedJson.data?.let { list ->
                        lifecycleScope.launch(Dispatchers.IO) {
                            val dao = (application as CampgroundApplication).db.campgroundDao()
                            dao.deleteAll()
                            dao.insertAll(
                                list.map {
                                    CampgroundEntity(
                                        name = it.name,
                                        description = it.description,
                                        latLong = it.latLong,
                                        imageUrl = it.imageUrl
                                    )
                                }
                            )
                        }
                    }
                } catch (e: JSONException) {
                    Log.e(TAG, "Exception: $e")
                }
            }
        })
    }
}
