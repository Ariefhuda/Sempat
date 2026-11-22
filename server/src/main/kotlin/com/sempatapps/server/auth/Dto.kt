package com.sempatapps.server.auth

import kotlinx.serialization.Serializable

@Serializable
data class KirimOtpRequest(val noHp: String)

@Serializable
data class KirimOtpResponse(
    val berlakuDetik: Int,
    val kirimUlangDetik: Int,
    /** Hanya terisi kalau OTP_DEV_MODE=true. */
    val kodeDev: String? = null,
)

@Serializable
data class CekOtpRequest(val noHp: String, val kode: String)

@Serializable
data class CekOtpResponse(val tokenVerifikasi: String)

@Serializable
data class LokasiDto(
    val latitude: Double,
    val longitude: Double,
    val alamat: String,
    val patokan: String = "",
)

@Serializable
data class DaftarRequest(
    val tokenVerifikasi: String,
    val peran: String,
    val nik: String,
    val nama: String,
    /** dd-MM-yyyy, sama dengan format KtpData di app. */
    val tanggalLahir: String,
    val kabupatenKota: String,
    val lokasi: LokasiDto,
    val password: String,
)

@Serializable
data class UserDto(
    val id: String,
    val peran: String,
    val nama: String,
    val noHp: String,
)

@Serializable
data class SesiResponse(val token: String, val user: UserDto)

@Serializable
data class ErrorResponse(val pesan: String)

/** Dilempar dari mana saja, diubah jadi response {"pesan": ...} oleh StatusPages. */
class ApiException(val status: Int, override val message: String) : RuntimeException(message)

@Serializable
data class MasukRequest(val noHp: String, val password: String)
