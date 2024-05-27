package com.dicoding.asclepius.view

import android.content.Intent
import android.content.Intent.ACTION_GET_CONTENT
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.asclepius.R
import com.dicoding.asclepius.databinding.ActivityMainBinding
import com.yalantis.ucrop.UCrop
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var currentImageUri: Uri? = null
    private var croppedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners() // Set up button listeners
    }

    private fun setupListeners() {
        // Set listener for gallery button to open image gallery
        binding.galleryButton.setOnClickListener {
            openGallery()
        }
        // Set listener for analyze button to analyze the selected image
        binding.analyzeButton.setOnClickListener {
            analyzeCurrentImage()
        }
    }

    private fun openGallery() {
        // Create an intent to open the image gallery
        val intent = Intent(ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        // Show a chooser dialog to let the user select an image
        val chooser = Intent.createChooser(intent, "Choose a Picture")
        launcherIntentGallery.launch(chooser)
    }

    // Handle the result from the image gallery activity
    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let {
                currentImageUri = it // Set the selected image URI
                displayImage() // Display the selected image
                startImageCropping(it) // Start the image cropping activity
            }
        }
    }

    private fun displayImage() {
        // Display the selected image in the ImageView
        currentImageUri?.let {
            Log.d("Image URI", "displayImage: $it")
            binding.previewImageView.setImageURI(it)
        }
    }

    private fun startImageCropping(sourceUri: Uri) {
        // Create a destination URI for the cropped image
        val fileName = "cropped_img_${System.currentTimeMillis()}.jpg"
        val destinationUri = Uri.fromFile(File(cacheDir, fileName))
        // Start the UCrop activity to crop the image
        UCrop.of(sourceUri, destinationUri).start(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == UCrop.REQUEST_CROP) {
            handleCropResult(resultCode, data) // Handle the result of the crop activity
        }
    }

    private fun handleCropResult(resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            val resultUri = UCrop.getOutput(data!!)
            resultUri?.let {
                displayCroppedImage(it) // Display the cropped image
            } ?: showToast("Failed to crop image")
        } else if (resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(data!!)
            showToast("Crop error: ${cropError?.message}")
        }
    }

    private fun displayCroppedImage(uri: Uri) {
        // Display the cropped image in the ImageView
        binding.previewImageView.setImageURI(uri)
        croppedImageUri = uri // Save the cropped image URI
    }

    private fun analyzeCurrentImage() {
        currentImageUri?.let {
            navigateToResultActivity(it) // Navigate to the result activity with the image URI
        } ?: run {
            showToast(getString(R.string.image_classifier_failed))
        }
    }

    private fun navigateToResultActivity(uri: Uri) {
        // Create an intent to start the ResultActivity with the image URI
        val intent = Intent(this, ResultActivity::class.java).apply {
            putExtra(ResultActivity.EXTRA_IMAGE_URI, uri.toString())
        }
        startActivityForResult(intent, REQUEST_RESULT)
    }

    private fun showToast(message: String) {
        // Show a toast message
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val REQUEST_RESULT = 1001
    }
}
