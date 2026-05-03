package com.komal.mediscan.MVVM

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor
import com.komal.mediscan.ML.MediScanTFLite
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MediScanViewModel : ViewModel() {

    var capturedImageUri  by mutableStateOf<Uri?>(null)
    var inputType         by mutableStateOf("image")
    var extractedText     by mutableStateOf("")
    var editableText      by mutableStateOf("")
    var localPredictions  by mutableStateOf<List<MediScanTFLite.TFLitePrediction>>(emptyList())
    var isProcessing      by mutableStateOf(false)
    var processingPhase   by mutableStateOf("Reading report...")
    var errorMessage      by mutableStateOf<String?>(null)

    // ── OCR ───────────────────────────────────────────────────────────────────
    fun runOCR(context: Context, uri: Uri, onComplete: () -> Unit) {
        isProcessing    = true
        processingPhase = "Reading report..."
        errorMessage    = null

        val image = try {
            InputImage.fromFilePath(context, uri)
        } catch (e: Exception) {
            errorMessage = "Cannot read image: ${e.message}"
            isProcessing = false
            return
        }

        TextRecognition
            .getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            .process(image)
            .addOnSuccessListener { visionText ->
                extractedText = visionText.text
                editableText  = visionText.text
                isProcessing  = false
                if (extractedText.trim().length < 20) {
                    errorMessage = "Very little text detected. Try better lighting."
                }
                onComplete()
            }
            .addOnFailureListener { e ->
                errorMessage = "OCR failed: ${e.message}"
                isProcessing = false
            }
    }

    // ── PDF ───────────────────────────────────────────────────────────────────
    fun extractPdfText(context: Context, uri: Uri, onComplete: () -> Unit) {
        isProcessing    = true
        processingPhase = "Reading PDF..."
        errorMessage    = null

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                    ?: throw Exception("Cannot open PDF file")

                val reader = PdfReader(inputStream)
                val pdf    = PdfDocument(reader)
                val sb     = StringBuilder()
                for (i in 1..pdf.numberOfPages) {
                    sb.append(PdfTextExtractor.getTextFromPage(pdf.getPage(i)))
                    sb.append("\n")
                }
                pdf.close()
                inputStream.close()

                val text = sb.toString()
                withContext(Dispatchers.Main) {
                    extractedText = text
                    editableText  = text
                    isProcessing  = false
                    if (text.trim().length < 20) {
                        errorMessage =
                            "PDF has no readable text. Try taking a photo instead."
                    }
                    onComplete()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    errorMessage = "PDF read failed: ${e.message}"
                    isProcessing = false
                }
            }
        }
    }

    // ── TFLite — runs everything locally, no internet needed ──────────────────
    fun runLocalML(context: Context, onComplete: () -> Unit) {
        isProcessing    = true
        processingPhase = "Analyzing values..."
        errorMessage    = null

        viewModelScope.launch(Dispatchers.Default) {
            try {
                MediScanTFLite.initialize(context)

                val testKeywordMap = mapOf(
                    "haemoglobin"  to "hemoglobin",
                    "hgb"          to "hemoglobin",
                    "hb"           to "hemoglobin",
                    "hemoglobin"   to "hemoglobin",
                    "glucose"      to "glucose",
                    "blood sugar"  to "glucose",
                    "fbs"          to "glucose",
                    "rbs"          to "glucose",
                    "creatinine"   to "creatinine",
                    "urea"         to "urea",
                    "bun"          to "urea",
                    "sodium"       to "sodium",
                    "na+"          to "sodium",
                    "potassium"    to "potassium",
                    "k+"           to "potassium",
                    "platelets"    to "platelets",
                    "plt"          to "platelets",
                    "wbc"          to "wbc",
                    "leucocytes"   to "wbc",
                    "white blood"  to "wbc",
                    "rbc"          to "rbc",
                    "red blood"    to "rbc",
                    "bilirubin"    to "total_bilirubin",
                    "sgpt"         to "sgpt_alt",
                    "alt"          to "sgpt_alt",
                    "sgot"         to "sgot_ast",
                    "ast"          to "sgot_ast",
                    "tsh"          to "tsh",
                    "thyroid"      to "tsh",
                    "hba1c"        to "hba1c",
                    "glycated"     to "hba1c",
                    "cholesterol"  to "cholesterol_total",
                    "hdl"          to "hdl",
                    "ldl"          to "ldl",
                    "triglycerides" to "triglycerides",
                    "tg"           to "triglycerides",
                    "uric acid"    to "uric_acid",
                    "uric"         to "uric_acid",
                    "vitamin d"    to "vitamin_d",
                    "vit d"        to "vitamin_d",
                    "vitamin b12"  to "vitamin_b12",
                    "vit b12"      to "vitamin_b12",
                    "b12"          to "vitamin_b12",
                    "iron"         to "iron",
                    "ferritin"     to "iron",
                    "calcium"      to "calcium",
                    "ca"           to "calcium"
                )

                val numberRegex = Regex("""(\d+\.?\d*)""")
                val results     = mutableListOf<MediScanTFLite.TFLitePrediction>()
                val seen        = mutableSetOf<String>()  // avoid duplicates

                for (line in editableText.lines()) {
                    val lower      = line.lowercase().trim()
                    if (lower.isBlank()) continue

                    val matchedKey = testKeywordMap.entries
                        .firstOrNull { lower.contains(it.key) }?.value
                        ?: continue

                    if (seen.contains(matchedKey)) continue  // skip duplicate tests
                    val value = numberRegex.find(line)?.value?.toFloatOrNull() ?: continue
                    if (value <= 0f) continue

                    try {
                        results.add(MediScanTFLite.predict(matchedKey, value))
                        seen.add(matchedKey)
                    } catch (_: Exception) { }
                }

                withContext(Dispatchers.Main) {
                    localPredictions = results
                    isProcessing     = false
                    if (results.isEmpty()) {
                        errorMessage =
                            "No test values detected. Please edit the text above " +
                                    "to make sure test names and numbers are visible."
                    }
                    onComplete()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    errorMessage = "Analysis failed: ${e.message}"
                    isProcessing = false
                }
            }
        }
    }

    // ── Reset ─────────────────────────────────────────────────────────────────
    fun reset() {
        capturedImageUri = null
        inputType        = "image"
        extractedText    = ""
        editableText     = ""
        localPredictions = emptyList()
        isProcessing     = false
        processingPhase  = "Reading report..."
        errorMessage     = null
    }
}

// ── Data models ───────────────────────────────────────────────────────────────
data class AnalysisResult(
    val patientSummary: String,
    val testResults: List<TestResult>
)

data class TestResult(
    val testName: String,
    val value: String,
    val normalRange: String,
    val status: String,
    val explanation: String
)