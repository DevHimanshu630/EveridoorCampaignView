package com.everidoor.campaign

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class ScreenActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var screenAdapter: ScreenAdapter
    private lateinit var screens: MutableList<Screen>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_screen)

        val campaignId = intent.getStringExtra("CAMPAIGN_ID") ?: ""
        val username = intent.getStringExtra("USERNAME") ?: ""
        screens = intent.getParcelableArrayListExtra<Screen>("SCREENS")?.toMutableList() ?: mutableListOf()

        recyclerView = findViewById(R.id.recycler_view1)
        screenAdapter = ScreenAdapter(screens, this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = screenAdapter

        // Optional: Fetch screens from the server using campaignId if screens list is empty
        if (screens.isEmpty()) {
            fetchScreens(campaignId)
        }
    }

    private fun fetchScreens(campaignId: String) {
        val client = OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build()
        val request = Request.Builder()
            .url("https://qdp72jc1-4000.inc1.devtunnels.ms/advertiser/getScreens?campaignId=$campaignId")
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()
                Log.d("ScreenResponse", responseBody ?: "No response found")

                if (response.isSuccessful) {
                    val jsonObject = JSONObject(responseBody)
                    val jsonArray = jsonObject.getJSONArray("screens")
                    for (i in 0 until jsonArray.length()) {
                        val screenObject = jsonArray.getJSONObject(i)
                        val screen = Screen(
                            screenObject.getString("screenId"),
                            screenObject.getString("screenName"),
                            screenObject.getString("type"),
                            screenObject.getString("typeVal"),
                            screenObject.getString("username")
                        )
                        screens.add(screen)
                    }
                    withContext(Dispatchers.Main) {
                        screenAdapter.notifyDataSetChanged()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@ScreenActivity,
                            "Failed to fetch screens",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("ScreenError", "An error occurred", e)
                    Toast.makeText(
                        this@ScreenActivity,
                        "An error occurred: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}
