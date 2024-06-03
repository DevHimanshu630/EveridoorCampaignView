package com.everidoor.campaign

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject


class MainActivity : AppCompatActivity() {
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        window.decorView.windowInsetsController?.let {
            it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
            it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        window.attributes.layoutInDisplayCutoutMode =
            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        setContentView(R.layout.activity_main)

        val sharedPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        if (isLoggedIn) {
            val token = sharedPreferences.getString("token", null)
            val username = sharedPreferences.getString("username", null)
            if (token != null && username != null) {
                val intent = Intent(this, homepage::class.java).apply {
                    putExtra("TOKEN", token)
                    putExtra("USERNAME", username)
                }
                startActivity(intent)
                finish()
                return
            }
        }

        usernameEditText = findViewById(R.id.login_username)
        passwordEditText = findViewById(R.id.login_password)
        loginButton = findViewById(R.id.login_button)
        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()
            if (username.isNotEmpty() && password.isNotEmpty()) {
                loginUser(username, password)
            } else {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT)
                    .show()
            }
        }

    }

    private fun loginUser(username: String, password: String) {
        val client = OkHttpClient()

        val mediaType = "application/json".toMediaType()
        val body = """
        {
            "username": "$username",
            "password": "$password"
        }
    """.trimIndent().toRequestBody(mediaType)

        val request = Request.Builder()
            .url("https://qdp72jc1-4000.inc1.devtunnels.ms/android_login") // Replace with your server URL
            .post(body)
            .addHeader("Content-Type", "application/json")
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()
                Log.d("LoginResponse", response.toString())
                Log.d("LoginResponse", responseBody ?: "No response found")
                withContext(Dispatchers.Main) {
                    when (response.code) {
                        200 -> {
                            val jsonObject = JSONObject(responseBody)
                            val token = jsonObject.getString("token")
                            val username = jsonObject.getString("username")


                            val sharedPreferences =
                                getSharedPreferences("loginPrefs", MODE_PRIVATE)
                            val editor = sharedPreferences.edit()
                            editor.putString("token", token)
                            editor.putString("username", username)
                            editor.putBoolean("isLoggedIn", true)
                            editor.apply()

                            val intent = Intent(this@MainActivity, homepage::class.java).apply {
                                putExtra("TOKEN", token)
                                putExtra("USERNAME", username)
                            }
                            startActivity(intent)
                            finish()
                        }

                        201 -> {
                            Toast.makeText(
                                this@MainActivity,
                                "Incorrect Username",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        401 -> {
                            Toast.makeText(
                                this@MainActivity,
                                "Incorrect Password",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        202 -> {
                            Toast.makeText(
                                this@MainActivity,
                                "Please enter all the required values",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        500 -> {
                            throw Exception("Internal Server Error")
                        }

                        else -> {
                            throw Exception("Unexpected response code: ${response.code}")
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("LoginError", "An error occurred", e)
                    Toast.makeText(
                        this@MainActivity,
                        "Server Error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}