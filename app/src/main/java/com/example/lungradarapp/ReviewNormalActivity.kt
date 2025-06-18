package com.example.lungradarapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import java.io.File

class ReviewNormalActivity : AppCompatActivity() {

    /* ---------------------- UI refs ---------------------- */
    private lateinit var imageView: ImageView
    private lateinit var confidenceTv: TextView
    private lateinit var analysisTv: TextView
    private lateinit var adviceTv: TextView
    private lateinit var btnBack: ImageButton
    private lateinit var rowHealthy: LinearLayout
    private lateinit var rowScreening: LinearLayout

    /* ---------------------- URLs ------------------------- */
    private val urlHealthy   =
        "https://www.lung.org/lung-health-diseases/lung-disease-lookup/lung-cancer/treatment/stay-healthy"
    private val urlScreening =
        "https://www.uspreventiveservicestaskforce.org/uspstf/recommendation/lung-cancer-screening"

    /* ---------------------- lifecycle -------------------- */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.review_normal)

        bindViews()
        setListeners()
        populateUi()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        sendResultAndFinish()    // call directly so RESULT_OK is set
    }

    /* ---------------------- helpers ---------------------- */
    private fun bindViews() {
        imageView      = findViewById(R.id.iv_stars)
        confidenceTv   = findViewById(R.id.tv_confidence_score)
        analysisTv     = findViewById(R.id.tv_analysis)
        adviceTv       = findViewById(R.id.tv_additional_message)
        btnBack        = findViewById(R.id.btn_back)
        rowHealthy     = findViewById(R.id.ll_resource_healthy)
        rowScreening   = findViewById(R.id.ll_resource_screening)
    }

    private fun setListeners() {
        btnBack.setOnClickListener { sendResultAndFinish() }

        rowHealthy.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(urlHealthy)))
        }

        rowScreening.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(urlScreening)))
        }
    }

    private fun populateUi() {
        val imagePath  = intent.getStringExtra("IMAGE_PATH")
        val confidence = intent.getFloatExtra("CONFIDENCE", 0f)

        Glide.with(this)
            .load(imagePath?.takeIf { File(it).exists() }?.let { File(it) })
            .placeholder(R.drawable.normal)
            .error(R.drawable.normal)
            .into(imageView)

        confidenceTv.text = getString(R.string.confidence_format, confidence * 100)
        analysisTv.text   = getString(R.string.analysis_normal)
        adviceTv.text     = getString(R.string.healthy_lifestyle_message)
    }

    private fun sendResultAndFinish() {
        setResult(RESULT_OK)
        finish()
    }
}
