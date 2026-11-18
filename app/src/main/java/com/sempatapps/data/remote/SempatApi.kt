package com.sempatapps.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

// Bentuk data sama persis dengan server/src/main/kotlin/.../auth/Dto.kt

@Serializable
data class KirimOtpRequest(val noHp: String)

@Serializable
data class KirimOtpResponse(val berlakuDetik: Int, val kirimUlangDetik: Int, val kodeDev: String? = null)

@Serializable
data class CekOtpRequest(val noHp: String, val kode: String)

@Serializable
data class CekOtpResponse(val tokenVerifikasi: String)

@Serializable
data class LokasiDto(val latitude: Double, val longitude: Double, val alamat: String, val patokan: String)

@Serializable
data class DaftarRequest(
    val tokenVerifikasi: String,
    val peran: String,
    val nik: String,
    val nama: String,
    val tanggalLahir: String,
    val kabupatenKota: String,
    val lokasi: LokasiDto,
    val password: String,
)

@Serializable
data class UserDto(val id: String, val peran: String, val nama: String, val noHp: String)

@Serializable
data class SesiResponse(val token: String, val user: UserDto)

@Serializable
private data class ErrorResponse(val pesan: String)

/** Error yang pesannya sudah siap ditampilkan ke pengguna. */
class ApiException(message: String) : Exception(message)

@Singleton
class SempatApi @Inject constructor(private val client: HttpClient) {

    suspend fun kirimOtp(noHp: String): KirimOtpResponse =
        post("auth/otp/kirim", KirimOtpRequest(noHp)).body()

    suspend fun cekOtp(noHp: String, kode: String): CekOtpResponse =
        post("auth/otp/cek", CekOtpRequest(noHp, kode)).body()

    suspend fun daftar(request: DaftarRequest): SesiResponse =
        post("auth/daftar", request).body()

    private suspend inline fun <reified T : Any> post(path: String, body: T): HttpResponse {
        val response = try {
            client.post(path) {
                contentType(ContentType.Application.Json)
                setBody(body)
            }
        } catch (e: IOException) {
            throw ApiException("Tidak bisa terhubung ke server. Cek koneksi internetmu.")
        }
        if (!response.status.isSuccess()) {
            val pesan = runCatching { response.body<ErrorResponse>().pesan }.getOrNull()
            throw ApiException(pesan ?: "Terjadi kesalahan (${response.status.value}). Coba lagi.")
        }
        return response
    }
}
