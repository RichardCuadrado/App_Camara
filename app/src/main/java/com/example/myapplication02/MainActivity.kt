package com.example.myapplication02

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

data class PhotoData(val path: String, val info: String)

class MainActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var captureButton: Button
    private lateinit var infoText: TextView

    private lateinit var photoFile: File
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val sharedPrefs by lazy {
        getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
    }

    @SuppressLint("MissingPermission")
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            imageView.setImageURI(Uri.fromFile(photoFile))
            getLocationAndShowInfo(photoFile.absolutePath)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.imageView)
        captureButton = findViewById(R.id.captureButton)
        infoText = findViewById(R.id.infoText)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        loadHistory()

        captureButton.setOnClickListener {
            if (checkPermissions()) {
                takePhoto()
            } else {
                requestPermissions()
            }
        }
    }

    private fun takePhoto() {
        photoFile = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "photo_${System.currentTimeMillis()}.jpg")
        val photoUri = FileProvider.getUriForFile(this, "$packageName.fileprovider", photoFile)
        cameraLauncher.launch(photoUri)
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun getLocationAndShowInfo(path: String) {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            val lat = location?.latitude ?: 0.0
            val alt = location?.altitude ?: 0.0
            val dateTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

            val info = "📍 Lat: $lat | ⛰️ Alt: $alt m | 🕒 $dateTime"
            infoText.text = info

            savePhotoData(PhotoData(path, info))
            loadHistory()
        }.addOnFailureListener {
            infoText.text = "No se pudo obtener la ubicación 📵"
        }
    }

    private fun checkPermissions(): Boolean {
        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        return permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION),
            100
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 100 && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            takePhoto()
        } else {
            Toast.makeText(this, "Se requieren permisos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun savePhotoData(photo: PhotoData) {
        val jsonArray = JSONArray(sharedPrefs.getString("photoHistory", "[]"))
        val obj = JSONObject().apply {
            put("path", photo.path)
            put("info", photo.info)
        }
        jsonArray.put(obj)

        sharedPrefs.edit()
            .putString("photoHistory", jsonArray.toString())
            .apply()
    }

    private fun loadHistory() {
        val jsonArray = JSONArray(sharedPrefs.getString("photoHistory", "[]"))
        val builder = StringBuilder("📷 Historial de fotos:\n\n")

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            builder.append("Foto ${i + 1}:\n${obj.getString("info")}\n\n")
        }

        if (jsonArray.length() == 0) {
            builder.append("Sin registros todavía")
        }

        infoText.text = builder.toString()
    }
}
