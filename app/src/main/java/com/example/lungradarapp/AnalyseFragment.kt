package com.example.lungradarapp

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts.*
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

class AnalyseFragment : Fragment(R.layout.fragment_analyse) {

    /* ---------- constants & enum ---------- */
    private enum class LastAction { NONE, CAMERA, GALLERY }
    private val storagePermission =
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_IMAGES
        else Manifest.permission.READ_EXTERNAL_STORAGE

    /* ---------- UI ---------- */
    private lateinit var ivLungs: ImageView
    private lateinit var selectedImageView: ImageView
    private lateinit var btnStartAnalysis: Button
    private lateinit var progressBar: ProgressBar

    /* ---------- state ---------- */
    private var currentPhotoPath: String? = null
    private var lastAction: LastAction = LastAction.NONE
    private lateinit var classifier: LungCancerClassifier

    /* ---------- activity‑result launchers ---------- */
    private val permissionLauncher =
        registerForActivityResult(RequestMultiplePermissions()) { grants ->
            if (grants.values.all { it }) {
                when (lastAction) {
                    LastAction.CAMERA  -> launchCamera()
                    LastAction.GALLERY -> openGallery()
                    else               -> Unit
                }
            } else {
                Toast.makeText(requireContext(), "Permissions denied", Toast.LENGTH_SHORT).show()
            }
        }

    private val takePictureLauncher =
        registerForActivityResult(TakePicture()) { success ->
            if (success) currentPhotoPath?.let { path ->
                decodeSampledBitmap(path, 800, 800)?.let(::updateImagePreview)
            }
        }

    private val pickImageLauncher =
        registerForActivityResult(GetContent()) { uri: Uri? ->
            uri ?: return@registerForActivityResult
            getBitmapFromUri(uri)?.let {
                updateImagePreview(it)
                currentPhotoPath = getPathFromUri(uri)
            }
        }

    /* Receive result from ReviewNormal / ReviewCancer */
    private val reviewLauncher =
        registerForActivityResult(StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) resetPreview()
        }

    /* ---------- lifecycle ---------- */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_analyse, container, false).also { root ->

        classifier = LungCancerClassifier(requireContext())

        ivLungs           = root.findViewById(R.id.ivLungs)
        selectedImageView = root.findViewById(R.id.selectedImageView)
        btnStartAnalysis  = root.findViewById(R.id.btnStartAnalysis)
        progressBar       = root.findViewById(R.id.progressBar)

        root.findViewById<Button>(R.id.btnUseCamera).setOnClickListener {
            lastAction = LastAction.CAMERA
            if (hasAllPermissions()) launchCamera() else requestAppPermissions()
        }

        root.findViewById<TextView>(R.id.tvSelectImages).setOnClickListener {
            lastAction = LastAction.GALLERY
            if (hasAllPermissions()) openGallery() else requestAppPermissions()
        }

        btnStartAnalysis.setOnClickListener {
            currentPhotoPath?.let { path ->
                decodeSampledBitmap(path, 800, 800)?.let(::processImage)
            } ?: Toast.makeText(requireContext(), "Please select an image first!", Toast.LENGTH_SHORT).show()
        }

        /* restore after rotation */
        savedInstanceState?.getString("imagePath")?.let { path ->
            currentPhotoPath = path
            decodeSampledBitmap(path, 800, 800)?.let(::updateImagePreview)
        }
        lastAction = savedInstanceState?.getString("lastAction")?.let { LastAction.valueOf(it) }
            ?: LastAction.NONE
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("imagePath", currentPhotoPath)
        outState.putString("lastAction", lastAction.name)
    }

    override fun onDestroy() {
        super.onDestroy()
        (classifier as? AutoCloseable)?.close()   // tidy resources
    }

    /* ---------- permission helpers ---------- */
    private fun hasAllPermissions(): Boolean =
        ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(requireContext(), storagePermission) ==
                PackageManager.PERMISSION_GRANTED

    private fun requestAppPermissions() =
        permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA, storagePermission))

    /* ---------- camera & gallery ---------- */
    private fun launchCamera() {
        val photoFile = createImageFile()
        val uri = FileProvider.getUriForFile(
            requireContext(), "${requireContext().packageName}.fileprovider", photoFile)
        takePictureLauncher.launch(uri)
    }

    private fun openGallery() = pickImageLauncher.launch("image/*")

    /* ---------- preview helpers ---------- */
    private fun updateImagePreview(bitmap: Bitmap) {
        ivLungs.visibility = View.GONE
        selectedImageView.visibility = View.VISIBLE
        selectedImageView.setImageBitmap(bitmap)
    }

    private fun resetPreview() {
        /* restore original lungs icon */
        currentPhotoPath = null
        selectedImageView.setImageDrawable(null)
        selectedImageView.visibility = View.GONE
        ivLungs.visibility = View.VISIBLE
    }

    /* ---------- analysis ---------- */
    private fun processImage(bitmap: Bitmap) {
        btnStartAnalysis.isEnabled = false
        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val (label, conf) = classifier.classify(bitmap)
                val highRisk = listOf("adenocarcinoma", "large-cell", "squamous")
                val risk = if (label.lowercase(Locale.ENGLISH) in highRisk) "High Risk" else "Low Risk"
                val imagePath = saveBitmapToFile(bitmap)

                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    btnStartAnalysis.isEnabled = true

                    val dest = if (risk == "High Risk") ReviewCancerActivity::class.java
                    else ReviewNormalActivity::class.java
                    reviewLauncher.launch(Intent(requireContext(), dest).apply {
                        putExtra("IMAGE_PATH", imagePath)
                        putExtra("CANCER_TYPE", label)
                        putExtra("CONFIDENCE", conf)
                        putExtra("RISK", risk)
                    })
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    btnStartAnalysis.isEnabled = true
                    Toast.makeText(requireContext(), "Analysis failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } finally {
                bitmap.recycle()
            }
        }
    }
    private fun decodeSampledBitmap(path: String, reqW: Int, reqH: Int): Bitmap? {
        val file = File(path)
        if (!file.exists()) {
            Toast.makeText(requireContext(), "Image file does not exist.", Toast.LENGTH_SHORT).show()
            return null
        }

        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
            BitmapFactory.decodeFile(path, this)

            val scaleFactor = (outWidth / reqW).coerceAtLeast(outHeight / reqH).coerceAtLeast(1)
            inSampleSize = scaleFactor
            inJustDecodeBounds = false
        }

        return BitmapFactory.decodeFile(path, options)
    }
    private fun createImageFile(): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timestamp}_", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }
    private fun getBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            val inputStream: InputStream? = requireContext().contentResolver.openInputStream(uri)
            inputStream?.use { BitmapFactory.decodeStream(it) }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Failed to load image", Toast.LENGTH_SHORT).show()
            null
        }
    }
    private fun saveBitmapToFile(bitmap: Bitmap): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val file = File(requireContext().cacheDir, "processed_$timestamp.jpg")
        file.outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
        }
        return file.absolutePath
    }
    private fun getPathFromUri(uri: Uri): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        requireContext().contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            return if (cursor.moveToFirst()) cursor.getString(columnIndex) else null
        }
        return null
    }


}
