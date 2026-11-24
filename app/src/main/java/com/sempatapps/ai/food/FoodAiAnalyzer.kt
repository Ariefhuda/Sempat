package com.sempatapps.ai.food

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.sempatapps.BuildConfig
import com.sempatapps.domain.model.FoodCheckResult
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.io.ByteArrayOutputStream

class FoodAiBusyException(message: String, cause: Throwable? = null) : Exception(message, cause)

@Serializable
private data class GeminiFoodVerdict(
    val kondisi: String = "Layak",
    val masaTahan: String = "2 Hari",
    val layakDidonasikan: Boolean = true,
    val rekomendasi: String = "Kondisi pangan masih baik dan cocok didonasikan ke yang membutuhkan",
)

object FoodAiAnalyzer {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private val httpClient by lazy {
        HttpClient(OkHttp) {
            install(HttpTimeout) {
                requestTimeoutMillis = 25_000
                connectTimeoutMillis = 12_000
                socketTimeoutMillis = 25_000
            }
        }
    }

    private val models = listOf("gemini-2.5-flash", "gemini-2.0-flash")

    suspend fun analyzeFood(
        context: Context,
        foodName: String,
        category: String,
        weightLabel: String,
        imageUri: String?,
        fallbackDrawableRes: Int,
        fallbackImageName: String,
    ): FoodCheckResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY.trim()
        if (apiKey.isEmpty() || apiKey.startsWith("ISI_API_KEY")) {
            throw FoodAiBusyException("API key Gemini belum diisi di local.properties")
        }

        val jpegBase64 = encodeImageToBase64(context, imageUri, fallbackDrawableRes)
            ?: throw FoodAiBusyException("Gagal membaca gambar pangan")

        val prompt = """
            Kamu adalah asisten pemeriksa kelayakan donasi pangan aplikasi SEMPAT di Indonesia.
            Analisis foto pangan berikut bersama informasi dari donatur:
            - Nama pangan: $foodName
            - Kategori: $category
            - Berat: $weightLabel

            Berikan penilaian singkat dalam JSON valid SAJA tanpa markdown/penjelasan tambahan dengan format tepat:
            {
              "kondisi": "Layak" atau "Perlu Dicek" atau "Tidak Layak",
              "masaTahan": estimasi singkat misal "1 Hari", "2 Hari", atau "6 Jam",
              "layakDidonasikan": true atau false,
              "rekomendasi": kalimat singkat 1-2 kalimat berbahasa Indonesia tentang kondisi pangan dan saran donasi
            }
        """.trimIndent()

        val requestBody = buildJsonObject {
            put(
                "contents",
                buildJsonArray {
                    add(
                        buildJsonObject {
                            put("role", "user")
                            put(
                                "parts",
                                buildJsonArray {
                                    add(buildJsonObject { put("text", prompt) })
                                    add(
                                        buildJsonObject {
                                            put(
                                                "inline_data",
                                                buildJsonObject {
                                                    put("mime_type", "image/jpeg")
                                                    put("data", jpegBase64)
                                                },
                                            )
                                        },
                                    )
                                },
                            )
                        },
                    )
                },
            )
            put(
                "generationConfig",
                buildJsonObject {
                    put("temperature", 0.2)
                    put("responseMimeType", "application/json")
                },
            )
        }.toString()

        val isVertexExpressKey = apiKey.startsWith("AQ.")
        var lastError: Throwable? = null
        for (model in models) {
            try {
                val url = if (isVertexExpressKey) {
                    "https://aiplatform.googleapis.com/v1/publishers/google/models/$model:generateContent?key=$apiKey"
                } else {
                    "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
                }
                val response = httpClient.post(url) {
                    contentType(ContentType.Application.Json)
                    setBody(requestBody)
                }
                val raw = response.bodyAsText()
                if (!response.status.isSuccess()) {
                    Log.e("FoodAiAnalyzer", "Model $model failed HTTP ${response.status.value}: $raw")
                    lastError = FoodAiBusyException("Gemini HTTP ${response.status.value}")
                    if (response.status.value == 404) continue
                    throw lastError
                }

                val verdict = parseGeminiResponse(raw)
                return@withContext FoodCheckResult(
                    name = foodName,
                    category = category,
                    weightLabel = weightLabel,
                    condition = verdict.kondisi.ifBlank { "Layak" },
                    shelfLife = verdict.masaTahan.ifBlank { "2 Hari" },
                    isEligible = verdict.layakDidonasikan,
                    recommendation = verdict.rekomendasi.ifBlank {
                        "Kondisi pangan masih baik dan cocok didonasikan ke yang membutuhkan"
                    },
                    imageName = fallbackImageName,
                    imageUri = imageUri,
                )
            } catch (e: FoodAiBusyException) {
                lastError = e
                if (e.message?.contains("404") != true) break
            } catch (e: Exception) {
                Log.e("FoodAiAnalyzer", "Model $model exception", e)
                lastError = FoodAiBusyException("Gagal menghubungi AI Gemini", e)
                break
            }
        }
        throw lastError ?: FoodAiBusyException("AI sedang sibuk")
    }

    private fun parseGeminiResponse(raw: String): GeminiFoodVerdict {
        val root = json.parseToJsonElement(raw).jsonObject
        val candidates = root["candidates"] as? JsonArray
            ?: throw FoodAiBusyException("Respons AI kosong")
        val firstContent = candidates.firstOrNull()?.jsonObject?.get("content") as? JsonObject
        val parts = firstContent?.get("parts") as? JsonArray
        val text = parts?.firstOrNull()?.jsonObject?.get("text")?.jsonPrimitive?.contentOrNull
            ?: throw FoodAiBusyException("Teks respons AI kosong")

        val cleaned = text.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
        return json.decodeFromString<GeminiFoodVerdict>(cleaned)
    }

    private fun encodeImageToBase64(context: Context, imageUri: String?, fallbackRes: Int): String? {
        val bitmap = if (!imageUri.isNullOrBlank()) {
            decodeScaledBitmapFromUri(context, Uri.parse(imageUri))
        } else {
            BitmapFactory.decodeResource(context.resources, fallbackRes)
        } ?: return null

        val scaled = scaleDownIfNeeded(bitmap, maxSide = 1024)
        val out = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 80, out)
        if (scaled !== bitmap) scaled.recycle()
        bitmap.recycle()
        return Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
    }

    private fun decodeScaledBitmapFromUri(context: Context, uri: Uri): Bitmap? = runCatching {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        val maxDim = maxOf(bounds.outWidth, bounds.outHeight)
        var sample = 1
        while (maxDim / sample > 1600) sample *= 2
        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
    }.getOrNull()

    private fun scaleDownIfNeeded(src: Bitmap, maxSide: Int): Bitmap {
        val maxDim = maxOf(src.width, src.height)
        if (maxDim <= maxSide) return src
        val ratio = maxSide.toFloat() / maxDim.toFloat()
        return Bitmap.createScaledBitmap(
            src,
            (src.width * ratio).toInt().coerceAtLeast(1),
            (src.height * ratio).toInt().coerceAtLeast(1),
            true,
        )
    }
}
