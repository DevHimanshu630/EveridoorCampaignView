package com.everidoor.campaign

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.withContext
import org.json.JSONObject

class Display : AppCompatActivity() {
    private lateinit var videoView: VideoView
    private val TAG = "Display"
    private lateinit var mSocket: Socket

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
            window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        WindowCompat.setDecorFitsSystemWindows(window,false)
        setContentView(R.layout.activity_display)
        videoView = findViewById(R.id.videoView)
        hideSystemBars()

        val sharedPreferences = getSharedPreferences("loginPrefs", Context.MODE_PRIVATE)
        val screenId = intent.getStringExtra("screenId") ?: "default-screen-id"
        val username = intent.getStringExtra("username") ?: "default-username"

        Log.e(TAG, "Screen ID: $screenId")

        // Initialize socket connection
        SocketHandler.setSocket()
        SocketHandler.establishConnection()
        mSocket = SocketHandler.getSocket()

        mSocket.on(Socket.EVENT_CONNECT) {
            Log.e(TAG, "Connection Established: ${mSocket.connected()}")
            Log.e(TAG, "Socket ID: ${mSocket.id()}")

            // Emit join-device-room and get-current-video events after connection is established
            val screen = JSONObject().apply {
                put("username", username)
                put("screenId", screenId)
            }
            mSocket.emit("join-device-room", screen)
            mSocket.emit("get-current-video", screen)

            // Register the event listeners
            mSocket.on("ack-get-current-video", onAckGetCurrentVideo)
            mSocket.on("update-video-on-webpage", onUpdateVideoOnWebpage)
        }
    }

    private val onAckGetCurrentVideo = Emitter.Listener { args ->
        val jsonObject = args[0] as JSONObject
        Log.d(TAG, "Received ack-get-current-video: $jsonObject")
        val contentId = jsonObject.getString("contentId")
        val seekTo = jsonObject.getLong("seekTo")
        runOnUiThread {
            Log.d(TAG, "handleAckGetCurrentVideo: contentId = $contentId, seekTo = $seekTo")
            playVideo(contentId, seekTo)
        }
    }

    private val onUpdateVideoOnWebpage = Emitter.Listener { args ->
        val jsonObject = args[0] as JSONObject
        val contentId = jsonObject.getString("contentId")
        runOnUiThread {
            Log.d(TAG, "Update Video on webpage: contentId = $contentId")
            playVideo(contentId, 0)
        }
    }

    private fun playVideo(contentId: String, seekTo: Long) {
        val videoUrl = "https://s3.ap-south-1.amazonaws.com/everidoor2.0/Videos/${contentId}.mp4"
        Log.d(TAG, "Playing video from URL: $videoUrl")

        val uri = Uri.parse(videoUrl)
        videoView.setVideoURI(uri)
        videoView.setOnPreparedListener { mp ->
            mp.seekTo(seekTo.toInt())
            videoView.start()
        }
        videoView.setOnCompletionListener {
            Log.d(TAG, "Video completed")
            fetchNextVideo()
        }
    }

    private fun fetchNextVideo() {
        Log.d(TAG, "Fetching next video")
        val screen = JSONObject().apply {
            put("username", intent.getStringExtra("username") ?: "default-username")
            put("screenId", intent.getStringExtra("screenId") ?: "default-screen-id")
        }
        mSocket.emit("get-current-video", screen)
    }
    private fun hideSystemBars() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val controller = window.insetsController
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mSocket.off("ack-get-current-video", onAckGetCurrentVideo)
        mSocket.off("update-video-on-webpage", onUpdateVideoOnWebpage)
        SocketHandler.closeConnection()
    }
}
