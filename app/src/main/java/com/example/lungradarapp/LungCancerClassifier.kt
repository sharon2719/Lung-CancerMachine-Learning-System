package com.example.lungradarapp

import android.content.Context
import android.graphics.Bitmap
import com.example.lungradarapp.ml.MobilenetModel
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.Closeable
import java.nio.ByteBuffer
import kotlin.math.min

/**
 * Wraps a single MobileNet model instance and can be reused across many calls.
 * Always call [close] from the host Fragment/Activity to release the TFLite interpreter.
 */
class LungCancerClassifier(context: Context) : Closeable {

    private val model: MobilenetModel = MobilenetModel.newInstance(context)

    private val inputShape = intArrayOf(1, 224, 224, 3)
    private val dataType   = DataType.FLOAT32
    private val labels     = arrayOf("normal", "adenocarcinoma", "large-cell", "squamous")

    /** Classifies one Bitmap and returns (label, confidence). */
    fun classify(image: Bitmap): Pair<String, Float> {
        val inputTensor = TensorBuffer.createFixedSize(inputShape, dataType)
        inputTensor.loadBuffer(preprocessImage(image))

        val outputTensor: TensorBuffer = model.process(inputTensor).outputFeature0AsTensorBuffer
        return postProcess(outputTensor)
    }

    /** Free the underlying TensorFlow Lite resources. */
    override fun close() {
        model.close()
    }

    // ---------- helpers ----------
    private fun preprocessImage(bitmap: Bitmap): ByteBuffer =
        TensorImage(dataType).apply {
            load(Bitmap.createScaledBitmap(bitmap, 224, 224, true))
        }.buffer

    private fun postProcess(tensor: TensorBuffer): Pair<String, Float> {
        val scores = tensor.floatArray
        val i      = scores.indices.maxByOrNull { scores[it] } ?: error("Empty output tensor")
        return labels[min(i, labels.lastIndex)] to scores[i]
    }
}
