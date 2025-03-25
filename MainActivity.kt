package com.example.myapplication1

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var mapView: MapView  // Declare MapView
    private lateinit var tvCoordinates: TextView  // Declare TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Views
        mapView = findViewById(R.id.mapView)
        tvCoordinates = findViewById(R.id.tvCoordinates)

        findViewById<Button>(R.id.btnRestart).setOnClickListener {
            mapView.resetUnit()
        }

        // ✅ Merge Both Listeners into One
        mapView.setOnLocationChangeListener { x, y ->
            tvCoordinates.text = "X: %.2f, Y: %.2f".format(x, y)
            Log.d("MapView", "Unit moved to: X=$x, Y=$y")
            sendPositionToServer(x, y)  // Send data to Python server
        }
    }

    private val client = OkHttpClient()

    private fun sendPositionToServer(x: Float, y: Float) {
        val url = "http://172.20.10.3:5000/update_position"  // Update server IP if needed

        val json = JSONObject().apply {
            put("x", x)
            put("y", y)
        }

        val requestBody = RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), json.toString())

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("MapView", "Failed to send position: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("MapView", "Position sent successfully: X=$x, Y=$y")
            }
        })
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return mapView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event)
    }
}
