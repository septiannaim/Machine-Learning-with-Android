package com.dicoding.asclepius.view

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.dicoding.asclepius.databinding.ActivityResultBinding
import com.dicoding.asclepius.helper.ImageClassifierHelper
import org.tensorflow.lite.task.vision.classifier.Classifications

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve the image URI from the intent
        val imageUriString = intent.getStringExtra(EXTRA_IMAGE_URI)
        if (imageUriString != null) {
            val imageUri = Uri.parse(imageUriString)
            displayImage(imageUri) // Display the selected image

            // Initialize the image classifier helper and classify the image
            val imageClassifierHelper = ImageClassifierHelper(
                context = this,
                classifierListener = object : ImageClassifierHelper.ClassifierListener {
                    override fun onError(errorMessage: String) {
                        // Display error message using Toast
                        Toast.makeText(this@ResultActivity, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
                    }

                    override fun onResults(results: List<Classifications>?, inferenceTime: Long) {
                        results?.let { showResults(it) } // Show classification results
                    }
                }
            )
            imageClassifierHelper.classifyStaticImage(imageUri)
        } else {
            // Display error message using Toast and close the activity
            Toast.makeText(this, "No image URI provided", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun displayImage(uri: Uri) {
        // Log and display the selected image
        Log.d(EXTRA_TAG, "Displaying image: $uri")
        binding.resultImage.setImageURI(uri)
    }

    private fun showResults(results: List<Classifications>) {
        // Display the top classification result
        val topResult = results[0]
        val label = topResult.categories[0].label
        val score = topResult.categories[0].score

        // Format the score to a percentage string
        fun Float.formatToString(): String {
            return String.format("%.2f%%", this * 100)
        }

        // Set the result text with the label and confidence score
        binding.resultText.text = "$label ${score.formatToString()}"
    }

    companion object {
        const val EXTRA_IMAGE_URI = "img_uri"
        const val EXTRA_TAG = "imagePicker"
    }
}
