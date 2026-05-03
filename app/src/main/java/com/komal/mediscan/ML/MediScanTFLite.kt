package com.komal.mediscan.ML

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

object MediScanTFLite {

    private var interpreter: Interpreter? = null
    private var scalerMean  = floatArrayOf()
    private var scalerScale = floatArrayOf()
    private var testNameClasses = listOf<String>()

    private val normalRanges = mapOf(
        "hemoglobin"        to Pair(11.5f, 17.5f),
        "wbc"               to Pair(4.0f,  11.0f),
        "rbc"               to Pair(3.8f,  5.8f),
        "platelets"         to Pair(150f,  400f),
        "glucose"           to Pair(70f,   100f),
        "creatinine"        to Pair(0.5f,  1.2f),
        "urea"              to Pair(7.0f,  20.0f),
        "sodium"            to Pair(135f,  145f),
        "potassium"         to Pair(3.5f,  5.0f),
        "total_bilirubin"   to Pair(0.2f,  1.2f),
        "sgpt_alt"          to Pair(7.0f,  56.0f),
        "sgot_ast"          to Pair(10.0f, 40.0f),
        "tsh"               to Pair(0.4f,  4.0f),
        "hba1c"             to Pair(4.0f,  5.6f),
        "cholesterol_total" to Pair(125f,  200f),
        "hdl"               to Pair(40f,   60f),
        "ldl"               to Pair(50f,   100f),
        "triglycerides"     to Pair(50f,   150f),
        "uric_acid"         to Pair(3.0f,  7.0f),
        "vitamin_d"         to Pair(20.0f, 50.0f),
        "vitamin_b12"       to Pair(200f,  900f),
        "iron"              to Pair(60f,   170f),
        "calcium"           to Pair(8.5f,  10.5f)
    )

    data class TFLitePrediction(
        val testName: String,
        val value: Float,
        val status: String,        // "Low" | "Normal" | "High"
        val confidence: Float,     // 0.0 to 1.0
        val normalRange: String
    )

    fun initialize(context: Context) {
        if (interpreter != null) return

        // Load model
        interpreter = Interpreter(loadModelFile(context))

        // Load scaler
        val scalerJson = JSONObject(
            context.assets.open("scaler_params.json").bufferedReader().readText()
        )
        val meanArr  = scalerJson.getJSONArray("mean")
        val scaleArr = scalerJson.getJSONArray("scale")
        scalerMean  = FloatArray(meanArr.length())  { meanArr.getDouble(it).toFloat() }
        scalerScale = FloatArray(scaleArr.length()) { scaleArr.getDouble(it).toFloat() }

        // Load test name classes
        val arr = JSONArray(
            context.assets.open("test_name_classes.json").bufferedReader().readText()
        )
        testNameClasses = List(arr.length()) { arr.getString(it) }
    }

    private fun loadModelFile(context: Context): MappedByteBuffer {
        val fd = context.assets.openFd("mediscan_model.tflite")
        return FileInputStream(fd.fileDescriptor).channel.map(
            FileChannel.MapMode.READ_ONLY, fd.startOffset, fd.declaredLength
        )
    }

    fun predict(rawTestName: String, value: Float): TFLitePrediction {
        val interp = interpreter
            ?: throw IllegalStateException("Call initialize() first")

        // Normalize test name to match training key format
        val testKey = rawTestName.lowercase()
            .trim()
            .replace(" ", "_")
            .replace("/", "_")
            .replace("-", "_")

        val range      = normalRanges[testKey] ?: normalRanges.entries
            .firstOrNull { testKey.contains(it.key) || it.key.contains(testKey) }
            ?.value ?: Pair(0f, 1f)

        val normalLow  = range.first
        val normalHigh = range.second
        val testEnc    = testNameClasses.indexOf(testKey).toFloat().coerceAtLeast(0f)
        val valueNorm  = (value - normalLow) / (normalHigh - normalLow)

        val rawFeatures = floatArrayOf(
            testEnc,
            value,
            normalLow,
            normalHigh,
            valueNorm,
            if (value < normalLow) 1f else 0f,
            if (value > normalHigh) 1f else 0f,
            (valueNorm - 0.5f) * 2f
        )

        // Apply StandardScaler (same as Python's scaler.transform)
        val scaled = FloatArray(rawFeatures.size) { i ->
            (rawFeatures[i] - scalerMean[i]) / scalerScale[i]
        }

        val input  = Array(1) { scaled }
        val output = Array(1) { FloatArray(3) }
        interp.run(input, output)

        val probs     = output[0]
        val predIndex = probs.indices.maxByOrNull { probs[it] } ?: 1
        val labels    = listOf("Low", "Normal", "High")

        return TFLitePrediction(
            testName    = rawTestName,
            value       = value,
            status      = labels[predIndex],
            confidence  = probs[predIndex],
            normalRange = "$normalLow – $normalHigh"
        )
    }

    fun close() {
        interpreter?.close()
        interpreter = null
    }
}