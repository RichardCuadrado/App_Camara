package com.example.myapplication02.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication02.databinding.ActivityMainBinding
import com.example.myapplication02.model.FotoEntity
import com.example.myapplication02.viewmodel.FotoViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: FotoViewModel
    private lateinit var adapter: FotoAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var currentPhotoPath: String? = null
    private var currentLat: Double? = null
    private var currentLon: Double? = null

    // Launcher para abrir la cámara
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && currentPhotoPath != null) {
            val file = File(currentPhotoPath!!)
            val uri = Uri.fromFile(file)

            // Mostrar la foto en el ImageView
            binding.imageView.setImageBitmap(BitmapFactory.decodeFile(file.absolutePath))

            // Guardar en la BD con ubicación
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val foto = FotoEntity(
                uri = uri.toString(),
                latitude = currentLat,
                longitude = currentLon,
                fechaHora = sdf.format(Date())
            )
            viewModel.guardarFoto(foto)

            binding.infoText.text = "Foto guardada en: ${foto.fechaHora}\n" +
                    "Lat: ${currentLat ?: "N/A"} - Lon: ${currentLon ?: "N/A"}"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ViewModel
        viewModel = ViewModelProvider(this)[FotoViewModel::class.java]

        // RecyclerView + Adapter
        adapter = FotoAdapter(emptyList())
        binding.rvFotos.layoutManager = LinearLayoutManager(this)
        binding.rvFotos.adapter = adapter

        // Location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Observar datos de Room
        viewModel.fotos.observe(this) { fotos ->
            adapter.update(fotos)
        }

        // Botón para tomar foto
        binding.captureButton.setOnClickListener {
            checkCameraPermissionAndOpen()
        }
    }

    private fun checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            checkLocationPermissionAndGet()
            openCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                100
            )
        }
    }

    private fun checkLocationPermissionAndGet() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    currentLat = location.latitude
                    currentLon = location.longitude
                }
            }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                200
            )
        }
    }

    private fun openCamera() {
        val photoFile = File(externalCacheDir, "IMG_${System.currentTimeMillis()}.jpg")
        currentPhotoPath = photoFile.absolutePath

        val photoUri: Uri = FileProvider.getUriForFile(
            this,
            "${applicationContext.packageName}.fileprovider",
            photoFile
        )
        takePictureLauncher.launch(photoUri)
    }
}
