package com.sempatapps.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

// Bentuk data sama persis dengan DTO di server/src/main/kotlin/...

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
data class MasukRequest(val noHp: String, val password: String)

@Serializable
data class UserDto(val id: String, val peran: String, val nama: String, val noHp: String)

@Serializable
data class SesiResponse(val token: String, val user: UserDto)

@Serializable
data class ProfilDto(
    val id: String,
    val peran: String,
    val nama: String,
    val noHp: String,
    val nik: String,
    val tanggalLahir: String,
    val kabupatenKota: String,
    val lokasi: LokasiDto?,
)

@Serializable
data class BuatDonasiRequest(
    val nama: String,
    val kategori: String,
    val jumlah: String,
    val beratGram: Int,
    val batasAmbil: String,
    val catatan: String = "",
    val fotoUrl: String? = null,
    val lokasi: LokasiDto? = null,
)

@Serializable
data class DonasiDto(
    val id: String,
    val donaturId: String,
    val namaDonatur: String,
    val nama: String,
    val kategori: String,
    val jumlah: String,
    val beratGram: Int,
    val catatan: String,
    val fotoUrl: String?,
    val alamatAmbil: String,
    val latitude: Double,
    val longitude: Double,
    val jarakKm: Double? = null,
    val batasAmbil: String,
    val status: String,
    val dibuatPada: String,
)

@Serializable
data class RingkasanDonaturDto(
    val totalBeratGram: Long,
    val jumlahSelesai: Long,
    val jumlahAktif: Long,
)

@Serializable
data class NotifikasiDto(
    val id: String,
    val judul: String,
    val pesan: String,
    val dibaca: Boolean,
    val dibuatPada: String,
)

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

    suspend fun masuk(noHp: String, password: String): SesiResponse =
        post("auth/masuk", MasukRequest(noHp, password)).body()

    suspend fun profilSaya(token: String): ProfilDto =
        get("me", token).body()

    suspend fun donasiSaya(token: String): List<DonasiDto> =
        get("donasi/saya", token).body()

    suspend fun ringkasanDonatur(token: String): RingkasanDonaturDto =
        get("donasi/saya/ringkasan", token).body()

    suspend fun donasiTerdekat(token: String): List<DonasiDto> =
        get("donasi", token).body()

    suspend fun buatDonasi(token: String, request: BuatDonasiRequest): DonasiDto =
        post("donasi", request, token).body()

    suspend fun notifikasiSaya(token: String): List<NotifikasiDto> =
        get("notifikasi", token).body()

    suspend fun tandaiNotifikasiDibaca(token: String, id: String) {
        postNoBody("notifikasi/$id/baca", token)
    }

    private suspend fun get(path: String, token: String): HttpResponse =
        request {
            client.get(path) {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
        }

    private suspend inline fun <reified T : Any> post(path: String, body: T, token: String? = null): HttpResponse =
        request {
            client.post(path) {
                if (token != null) header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(body)
            }
        }

    private suspend fun postNoBody(path: String, token: String): HttpResponse =
        request {
            client.post(path) {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
        }

    private suspend fun request(call: suspend () -> HttpResponse): HttpResponse {
        val response = try {
            call()
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
