package com.komal.mediscan

import com.google.gson.Gson
import com.komal.mediscan.BuildConfig
import com.komal.mediscan.MVVM.AnalysisResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object OpenAIService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun analyzeReport(ocrText: String): AnalysisResult {
        return withContext(Dispatchers.IO) {

            val prompt = """
JSON format:
{
  "patientSummary": "2-3 sentence overall summary in very simple language",
  "testResults": [
    {
      "testName": "Hemoglobin",
      "value": "11.2 g/dL",
      "normalRange": "13.5-17.5 g/dL",
      "status": "Low",
      "explanation": "Plain language explanation here."
    }
  ]
}

Rules:
- status must be exactly one of: Normal, High, Low
- Skip tests where the value is unreadable or missing
- Keep explanations under 3 sentences, very simple
- Do not use medical jargon

OCR Text:
${ocrText.take(3000)}
            """.trimIndent()

            val requestBody = JSONObject().apply {
                put("model", "gpt-4o")
                put("max_tokens", 2000)
                put("temperature", 0.1)
                put("messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", prompt)
                    })
                })
            }
            val request = Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .post(
                    requestBody.toString()
                        .toRequestBody("application/json; charset=utf-8".toMediaType())
                )
                .addHeader("Authorization", "Bearer ${BuildConfig.OPENAI_API_KEY}")
                .addHeader("Content-Type", "application/json")
                .build()

            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: "Unknown error"
                throw Exception("OpenAI API error ${response.code}: $errorBody")
            }

            val responseBody = response.body?.string()
                ?: throw Exception("Empty response from OpenAI")

            // Parse the API wrapper
            val jsonResponse = JSONObject(responseBody)
            val content = jsonResponse
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
                .trim()

            // Strip markdown fences if GPT adds them despite instructions
            val cleanJson = content
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            try {
                Gson().fromJson(cleanJson, AnalysisResult::class.java)
                    ?: throw Exception("Failed to parse response")
            } catch (e: Exception) {
                throw Exception("Parse error: ${e.message}\nRaw: $cleanJson")
            }
        }
    }
}