package com.sempatapps.data.repository

import com.sempatapps.data.local.Sesi
import com.sempatapps.data.local.SessionStore
import com.sempatapps.data.remote.DaftarRequest
import com.sempatapps.data.remote.LokasiDto
import com.sempatapps.data.remote.SempatApi
import com.sempatapps.domain.model.DataDaftar
import javax.inject.Inject
import javax.inject.Singleton

data class OtpTerkirim(val kirimUlangDetik: Int, val kodeDev: String?)

interface AuthRepository {
    suspend fun kirimOtp(noHp: String): OtpTerkirim

    /** @return token verifikasi, dipakai di [daftar]. */
    suspend fun cekOtp(noHp: String, kode: String): String

    /** Daftar akun baru lalu simpan sesinya di HP. */
    suspend fun daftar(tokenVerifikasi: String, data: DataDaftar, password: String)
}

@Singleton
class ApiAuthRepository @Inject constructor(
    private val api: SempatApi,
    private val sessionStore: SessionStore,
) : AuthRepository {

    override suspend fun kirimOtp(noHp: String): OtpTerkirim {
        val response = api.kirimOtp(noHp)
        return OtpTerkirim(response.kirimUlangDetik, response.kodeDev)
    }

    override suspend fun cekOtp(noHp: String, kode: String): String =
        api.cekOtp(noHp, kode).tokenVerifikasi

    override suspend fun daftar(tokenVerifikasi: String, data: DataDaftar, password: String) {
        val lokasi = requireNotNull(data.lokasi) { "Lokasi belum dipilih" }
        val sesi = api.daftar(
            DaftarRequest(
                tokenVerifikasi = tokenVerifikasi,
                peran = data.peran.name,
                nik = data.ktp.nik,
                nama = data.ktp.nama,
                tanggalLahir = data.ktp.tanggalLahir,
                kabupatenKota = data.ktp.kabupatenKota,
                lokasi = LokasiDto(lokasi.latitude, lokasi.longitude, lokasi.alamat, data.patokan),
                password = password,
            ),
        )
        sessionStore.simpan(Sesi(sesi.token, data.peran, sesi.user.nama))
    }
}
