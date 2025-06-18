package com.example.lungradarapp

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class ReviewCancerActivity : AppCompatActivity() {
    private val urlHealthy   = "https://www.lung.org/lung-health-diseases/lung-disease-lookup/lung-cancer/treatment/stay-healthy"
    private val urlScreening = "https://www.uspreventiveservicestaskforce.org/uspstf/recommendation/lung-cancer-screening"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.review_cancer)

        val imagePath = intent.getStringExtra("IMAGE_PATH")
        val cancerType = intent.getStringExtra("CANCER_TYPE") ?: "Unknown"
        val confidence = intent.getFloatExtra("CONFIDENCE", 0f)
        val risk = intent.getStringExtra("RISK") ?: "Low Risk"

        val imageView: ImageView = findViewById(R.id.iv_stars)
        val confidenceTv: TextView = findViewById(R.id.tv_confidence_score)
        val analysisTv: TextView = findViewById(R.id.tv_analysis)
        val adviceTv: TextView = findViewById(R.id.tv_additional_message)
        val btnBack: ImageButton = findViewById(R.id.btn_back)

        val rowHealthy   : LinearLayout = findViewById(R.id.ll_resource_healthy)
        val rowScreening : LinearLayout = findViewById(R.id.ll_resource_screening)

        rowHealthy.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(urlHealthy)))
        }

        rowScreening.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(urlScreening)))
        }
        imagePath?.let { File(it) }
            ?.takeIf(File::exists)
            ?.let { imageView.setImageBitmap(BitmapFactory.decodeFile(it.path)) }
            ?: imageView.setImageResource(R.drawable.cancer)

        confidenceTv.text = getString(R.string.confidence_format, confidence * 100)
        analysisTv.text   = getString(R.string.analysis_result, cancerType)
        adviceTv.text     = if (risk == "High Risk")
            getString(R.string.high_risk_advice)
        else getString(R.string.low_risk_advice)

        btnBack.setOnClickListener { sendResultAndFinish() }


    }

    override fun onBackPressed() {
        super.onBackPressed()
        sendResultAndFinish()
    }

    private fun sendResultAndFinish() {
        setResult(RESULT_OK)
        finish()
    }
}
