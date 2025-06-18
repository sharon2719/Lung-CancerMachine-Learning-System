package com.example.lungradarapp

data class Result(
    val timestamp: Long,
    val imagePath: String,
    val result: String,
    val confidence: Double,
    val risk: String
)


