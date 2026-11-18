package com.sempatapps.server

import com.sempatapps.server.auth.AuthService
import com.sempatapps.server.auth.CekOtpRequest
import com.sempatapps.server.auth.CekOtpResponse
import com.sempatapps.server.auth.DaftarRequest
import com.sempatapps.server.auth.ErrorResponse
import com.sempatapps.server.auth.KirimOtpRequest
import com.sempatapps.server.auth.KirimOtpResponse
import com.sempatapps.server.auth.LokasiDto
import com.sempatapps.server.auth.SesiResponse
import com.sempatapps.server.auth.TokenService
import com.sempatapps.server.db.KodeOtp
import com.sempatapps.server.db.Lokasi
import com.sempatapps.server.db.Users
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AuthRoutesTest {

    private fun ApplicationTestBuilder.siapkan(): HttpClient {
        // Tiap test pakai database H2 baru di memori, mode MySQL.
        val db = Database.connect("jdbc:h2:mem:${UUID.randomUUID()};MODE=MySQL;DB_CLOSE_DELAY=-1", "org.h2.Driver")
        transaction(db) { SchemaUtils.create(Users, Lokasi, KodeOtp) }
        val auth = AuthService(db, TokenService("rahasia-test"), nikSecret = "nik-test", otpDevMode = true)
        application { module(auth) }
        return createClient { install(ContentNegotiation) { json() } }
    }

    private suspend fun HttpClient.postJson(path: String, body: Any) = post(path) {
        contentType(ContentType.Application.Json)
        setBody(body)
    }

    private fun daftarRequest(token: String, nik: String = "1271010101980001") = DaftarRequest(
        tokenVerifikasi = token,
        peran = "DONOR",
        nik = nik,
        nama = "Siti Rahma",
        tanggalLahir = "12-05-1998",
        kabupatenKota = "KOTA MEDAN",
        lokasi = LokasiDto(3.5952, 98.6722, "Jl. Jenggala, Medan", "depan masjid"),
        password = "rahasia123",
    )

    private suspend fun HttpClient.verifikasi(noHp: String): String {
        val kirim = postJson("/auth/otp/kirim", KirimOtpRequest(noHp)).body<KirimOtpResponse>()
        val kode = assertNotNull(kirim.kodeDev)
        return postJson("/auth/otp/cek", CekOtpRequest(noHp, kode)).body<CekOtpResponse>().tokenVerifikasi
    }

    @Test
    fun `daftar lengkap dari OTP sampai dapat token sesi`() = testApplication {
        val client = siapkan()
        val token = client.verifikasi("0812-3456-7890")

        val response = client.postJson("/auth/daftar", daftarRequest(token))

        assertEquals(HttpStatusCode.Created, response.status)
        val sesi = response.body<SesiResponse>()
        assertEquals("6281234567890", sesi.user.noHp)
        assertEquals("DONOR", sesi.user.peran)
    }

    @Test
    fun `kode salah ditolak`() = testApplication {
        val client = siapkan()
        val kirim = client.postJson("/auth/otp/kirim", KirimOtpRequest("081234567890")).body<KirimOtpResponse>()
        val salah = if (kirim.kodeDev == "000000") "111111" else "000000"

        val response = client.postJson("/auth/otp/cek", CekOtpRequest("081234567890", salah))

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals("Kode salah", response.body<ErrorResponse>().pesan)
    }

    @Test
    fun `kirim ulang terlalu cepat ditolak`() = testApplication {
        val client = siapkan()
        client.postJson("/auth/otp/kirim", KirimOtpRequest("081234567890"))

        val response = client.postJson("/auth/otp/kirim", KirimOtpRequest("081234567890"))

        assertEquals(HttpStatusCode.TooManyRequests, response.status)
    }

    @Test
    fun `NIK yang sama tidak bisa daftar dua kali`() = testApplication {
        val client = siapkan()
        client.postJson("/auth/daftar", daftarRequest(client.verifikasi("081111111111")))

        val response = client.postJson("/auth/daftar", daftarRequest(client.verifikasi("082222222222")))

        assertEquals(HttpStatusCode.Conflict, response.status)
    }

    @Test
    fun `nomor HP yang sudah terdaftar tidak bisa minta kode daftar`() = testApplication {
        val client = siapkan()
        client.postJson("/auth/daftar", daftarRequest(client.verifikasi("081234567890")))

        val response = client.postJson("/auth/otp/kirim", KirimOtpRequest("081234567890"))

        assertEquals(HttpStatusCode.Conflict, response.status)
    }

    @Test
    fun `token verifikasi palsu ditolak`() = testApplication {
        val client = siapkan()

        val response = client.postJson("/auth/daftar", daftarRequest("bukan-token"))

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
}
