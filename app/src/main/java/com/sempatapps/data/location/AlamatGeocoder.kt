package com.sempatapps.data.location

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

data class Koordinat(val latitude: Double, val longitude: Double)

/**
 * Cari koordinat dari alamat dan sebaliknya, pakai data OpenStreetMap lewat Nominatim.
 *
 * Aturan pemakaian Nominatim (operations.osmfoundation.org/policies/nominatim):
 * - wajib mengirim User-Agent yang jelas (di sini identitas app, bukan data pribadi);
 * - maksimal 1 permintaan per detik, jadi permintaan diantrikan.
 *
 * Semua fungsi mengembalikan null kalau tidak ketemu atau gagal (misalnya tidak ada internet).
 */
@Singleton
class AlamatGeocoder @Inject constructor() {

    private val client = HttpClient(OkHttp) {
        install(HttpTimeout) {
            requestTimeoutMillis = 10_000
            connectTimeoutMillis = 5_000
        }
    }
    private val json = Json { ignoreUnknownKeys = true }
    private val antrean = Mutex()
    private var terakhirMs = 0L

    /** Koordinat dari nama tempat atau alamat, mis. "KOTA MEDAN" atau "Jl Setia Budi 5, Medan". */
    suspend fun cari(alamat: String): Koordinat? {
        if (alamat.isBlank()) return null
        val teks = ambil("search") {
            parameter("q", alamat)
            parameter("format", "json")
            parameter("limit", 1)
            parameter("countrycodes", "id")
        } ?: return null
        val hasil = runCatching { json.decodeFromString<List<HasilCari>>(teks) }.getOrNull()
        return hasil?.firstOrNull()?.let { Koordinat(it.lat.toDouble(), it.lon.toDouble()) }
    }

    /** Alamat yang dikenali OpenStreetMap untuk satu titik di peta. */
    suspend fun alamatDari(latitude: Double, longitude: Double): String? {
        val teks = ambil("reverse") {
            parameter("lat", latitude)
            parameter("lon", longitude)
            parameter("format", "json")
            parameter("zoom", 18)
        } ?: return null
        return runCatching { json.decodeFromString<HasilBalik>(teks).displayName }
            .getOrNull()
            ?.takeIf { it.isNotBlank() }
    }

    private suspend fun ambil(jalur: String, parameter: io.ktor.client.request.HttpRequestBuilder.() -> Unit): String? =
        antrean.withLock {
            val sisa = JEDA_MS - (System.currentTimeMillis() - terakhirMs)
            if (sisa > 0) delay(sisa)
            try {
                client.get("$BASE/$jalur") {
                    header(HttpHeaders.UserAgent, USER_AGENT)
                    header(HttpHeaders.AcceptLanguage, "id")
                    parameter()
                }.bodyAsText()
            } catch (_: Exception) {
                null
            } finally {
                terakhirMs = System.currentTimeMillis()
            }
        }

    @Serializable
    private data class HasilCari(val lat: String, val lon: String)

    @Serializable
    private data class HasilBalik(@SerialName("display_name") val displayName: String? = null)

    private companion object {
        const val BASE = "https://nominatim.openstreetmap.org"
        const val JEDA_MS = 1_100L
        // Identitas app saja. Jangan isi dengan nama atau email pribadi.
        const val USER_AGENT = "com.sempatapps.app/1.0 (Android)"
    }
}
