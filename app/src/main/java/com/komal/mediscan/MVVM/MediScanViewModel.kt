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
import com.komal.mediscan.OpenAIService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MediScanViewModel : ViewModel() {

    var capturedImageUri by mutableStateOf<Uri?>(null)
    var inputType by mutableStateOf("image")
    var extractedText by mutableStateOf("")
    var editableText by mutableStateOf("")
    var analysisResult by mutableStateOf<AnalysisResult?>(null)
    var isProcessing by mutableStateOf(false)
    var processingPhase by mutableStateOf("Reading report...")
    var errorMessage by mutableStateOf<String?>(null)

    // ─── OCR (image) ────────────────────────────────────────────────────────────
    fun runOCR(context: Context, uri: Uri, onComplete: () -> Unit) {
        isProcessing = true
        processingPhase = "Reading report..."
        errorMessage = null

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
                editableText = visionText.text
                isProcessing = false
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

    // ─── PDF extraction using iText7 ────────────────────────────────────────────
    fun extractPdfText(context: Context, uri: Uri, onComplete: () -> Unit) {
        isProcessing = true
        processingPhase = "Reading PDF..."
        errorMessage = null

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                    ?: throw Exception("Cannot open PDF file")

                // iText7 — no init() call needed, works directly
                val reader = PdfReader(inputStream)
                val pdf = PdfDocument(reader)

                val sb = StringBuilder()
                for (i in 1..pdf.numberOfPages) {
                    sb.append(
                        PdfTextExtractor.getTextFromPage(pdf.getPage(i))
                    )
                    sb.append("\n")
                }

                pdf.close()
                inputStream.close()

                val text = sb.toString()

                withContext(Dispatchers.Main) {
                    extractedText = text
                    editableText = text
                    isProcessing = false
                    if (text.trim().length < 20) {
                        errorMessage =
                            "PDF has no readable text. It may be scanned — use camera instead."
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

    // ─── OpenAI analysis ────────────────────────────────────────────────────────
    fun analyzeWithOpenAI(onComplete: () -> Unit) {
        if (editableText.isBlank()) {
            errorMessage = "No text to analyze. Please go back and try again."
            return
        }

        isProcessing = true
        processingPhase = "Analyzing values..."
        errorMessage = null

        viewModelScope.launch {
            try {
                val result = OpenAIService.analyzeReport(editableText)
                analysisResult = result
                isProcessing = false
                onComplete()
            } catch (e: Exception) {
                errorMessage = when {
                    e.message?.contains("401") == true ->
                        "Invalid API key. Check local.properties."
                    e.message?.contains("429") == true ->
                        "Rate limit hit. Wait and try again."
                    e.message?.contains("insufficient_quota") == true ->
                        "OpenAI quota exceeded. Add billing at platform.openai.com."
                    e.message?.contains("Unable to resolve host") == true ->
                        "No internet connection."
                    else -> "Analysis failed: ${e.message}"
                }
                isProcessing = false
            }
        }
    }

    // ─── Reset ──────────────────────────────────────────────────────────────────
    fun reset() {
        capturedImageUri = null
        inputType = "image"
        extractedText = ""
        editableText = ""
        analysisResult = null
        isProcessing = false
        processingPhase = "Reading report..."
        errorMessage = null
    }
}

// ─── Data models ────────────────────────────────────────────────────────────────
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