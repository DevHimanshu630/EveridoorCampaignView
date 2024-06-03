package com.everidoor.campaign

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject

class homepage : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var campaignAdapter: CampaignAdapter
    private lateinit var campaigns: MutableList<Campaign>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_homepage)
        val token = intent.getStringExtra("TOKEN") ?: ""
        val username = intent.getStringExtra("USERNAME") ?: ""

        recyclerView = findViewById(R.id.recycler_view)
        campaigns = mutableListOf()
        campaignAdapter = CampaignAdapter(campaigns,this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = campaignAdapter

        fetchCampaigns(token, username)

        val logoutButton: ImageView = findViewById(R.id.logout)
        logoutButton.setOnClickListener {
            showLogoutConfirmationDialog()
        }

    }
    private fun fetchCampaigns(token: String, username: String) {
        val client = OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build()
        val request = Request.Builder()
            .url("https://qdp72jc1-4000.inc1.devtunnels.ms/advertiser/getAdCampaignRequests?username=$username")
            .addHeader("Authorization", "Bearer $token")
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()
                Log.d("CampaignResponse", responseBody ?: "No response found")

                if (response.isSuccessful) {
                    val jsonObject = JSONObject(responseBody)
                    val jsonArray = jsonObject.getJSONArray("result")
                    for (i in 0 until jsonArray.length()) {
                        val campaignObject = jsonArray.getJSONObject(i)
                        val selectedScreens = campaignObject.getJSONArray("selectedScreens")
                        val screens = mutableListOf<Screen>()
                        for (j in 0 until selectedScreens.length()) {
                            val screenObject = selectedScreens.getJSONObject(j)
                            val screen = Screen(
                                screenObject.getString("screenId"),
                                screenObject.getString("screenName"),
                                screenObject.getString("type"),
                                screenObject.getString("type_val"),
                                screenObject.getString("username")
                            )
                            screens.add(screen)
                        }
                        val campaign = Campaign(
                            campaignObject.getString("campaignRequestId"),
                            campaignObject.getString("campaignName"),
                            campaignObject.getString("fromDate"),
                            campaignObject.getString("toDate"),
                            selectedScreens.length(),
                            screens,
                            campaignObject.getString("username")
                        )
                        campaigns.add(campaign)
                    }
                    withContext(Dispatchers.Main) {
                        campaignAdapter.notifyDataSetChanged()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@homepage,
                            "Failed to fetch campaigns",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("CampaignError", "An error occurred", e)
                    Toast.makeText(
                        this@homepage,
                        "An error occurred: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun showLogoutConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Are you sure you want to logout?")
            .setCancelable(false)
            .setPositiveButton("Yes") { dialog, id ->
                logout()
            }
            .setNegativeButton("No") { dialog, id ->
                dialog.dismiss()
            }
        val alert = builder.create()
        alert.show()
    }

    private fun logout() {
        val sharedPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}