package com.sempatapps.server

import com.sempatapps.server.auth.AuthService
import com.sempatapps.server.auth.CekOtpRequest
import com.sempatapps.server.auth.CekOtpResponse
import com.sempatapps.server.auth.DaftarRequest
import com.sempatapps.server.auth.KirimOtpRequest
import com.sempatapps.server.auth.KirimOtpResponse
import com.sempatapps.server.auth.LokasiDto
import com.sempatapps.server.auth.MasukRequest
import com.sempatapps.server.auth.SesiResponse
import com.sempatapps.server.auth.TokenService
import com.sempatapps.server.db.SEMUA_TABEL
import com.sempatapps.server.donasi.AjukanRequest
import com.sempatapps.server.donasi.BuatDonasiRequest
import com.sempatapps.server.donasi.DonasiDto
import com.sempatapps.server.donasi.PengajuanDto
import com.sempatapps.server.donasi.RingkasanDonaturDto
import com.sempatapps.server.notifikasi.NotifikasiDto
import com.sempatapps.server.profil.ProfilDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
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
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DonasiRoutesTest {

    private fun ApplicationTestBuilder.siapkan(): HttpClient {
        val db = Database.connect("jdbc:h2:mem:${UUID.randomUUID()};MODE=MySQL;DB_CLOSE_DELAY=-1", "org.h2.Driver")
        transaction(db) { SchemaUtils.create(*SEMUA_TABEL) }
        val tokens = TokenService("rahasia-test")
        val auth = AuthService(db, tokens, nikSecret = "nik-test", otpDevMode = true)
        application { module(Layanan.buat(db, tokens, auth)) }
        return createClient { install(ContentNegotiation) { json() } }
    }

    private suspend fun HttpClient.kirim(path: String, body: Any? = null, token: String? = null) = post(path) {
        token?.let { bearerAuth(it) }
        contentType(ContentType.Application.Json)
        setBody(body ?: emptyMap<String, String>())
    }

    private suspend fun HttpClient.ambil(path: String, token: String?) = get(path) { token?.let { bearerAuth(it) } }

    /** Daftar user baru lewat API, kembalikan token sesinya. */
    private suspend fun HttpClient.daftar(noHp: String, nik: String, peran: String, lat: Double = 3.5952, lng: Double = 98.6722): String {
        val kode = assertNotNull(kirim("/auth/otp/kirim", KirimOtpRequest(noHp)).body<KirimOtpResponse>().kodeDev)
        val verif = kirim("/auth/otp/cek", CekOtpRequest(noHp, kode)).body<CekOtpResponse>().tokenVerifikasi
        val req = DaftarRequest(verif, peran, nik, "User $peran", "12-05-1998", "KOTA MEDAN", LokasiDto(lat, lng, "Jl. Contoh, Medan"), "rahasia123")
        return kirim("/auth/daftar", req).body<SesiResponse>().token
    }

    private fun roti() = BuatDonasiRequest(
        nama = "Roti Enak",
        kategori = "Roti & Kue",
        jumlah = "10 bungkus",
        beratGram = 2500,
        batasAmbil = Instant.now().plus(1, ChronoUnit.DAYS).toString(),
    )

    @Test
    fun `masuk dengan nomor HP dan password`() = testApplication {
        val client = siapkan()
        client.daftar("081111111111", "1271010101980001", "DONOR")

        val ok = client.kirim("/auth/masuk", MasukRequest("+62 811-1111-1111", "rahasia123"))
        val salah = client.kirim("/auth/masuk", MasukRequest("081111111111", "salah12345"))
        val tidakAda = client.kirim("/auth/masuk", MasukRequest("089999999999", "rahasia123"))

        assertEquals(HttpStatusCode.OK, ok.status)
        assertEquals("DONOR", ok.body<SesiResponse>().user.peran)
        assertEquals(HttpStatusCode.Unauthorized, salah.status)
        assertEquals(HttpStatusCode.Unauthorized, tidakAda.status)
    }

    @Test
    fun `profil butuh token sesi`() = testApplication {
        val client = siapkan()
        val token = client.daftar("081111111111", "1271010101980001", "DONOR")

        assertEquals(HttpStatusCode.Unauthorized, client.ambil("/me", null).status)
        assertEquals(HttpStatusCode.Unauthorized, client.ambil("/me", "token-palsu").status)
        val profil = client.ambil("/me", token).body<ProfilDto>()
        assertEquals("1271 •••• •••• 0001", profil.nik)
        assertEquals("Jl. Contoh, Medan", profil.lokasi?.alamat)
    }

    @Test
    fun `alur lengkap donasi sampai diterima`() = testApplication {
        val client = siapkan()
        val donatur = client.daftar("081111111111", "1271010101980001", "DONOR")
        val penerimaA = client.daftar("082222222222", "1271010101980002", "RECIPIENT", lat = 3.60)
        val penerimaB = client.daftar("083333333333", "1271010101980003", "RECIPIENT", lat = 3.61)

        // Donatur membuat donasi (lokasi diambil dari profil)
        val buat = client.kirim("/donasi", roti(), donatur)
        assertEquals(HttpStatusCode.Created, buat.status)
        val donasi = buat.body<DonasiDto>()
        assertEquals("TERSEDIA", donasi.status)

        // Penerima melihat pangan terdekat
        val terdekat = client.ambil("/donasi", penerimaA).body<List<DonasiDto>>()
        assertEquals(listOf(donasi.id), terdekat.map { it.id })
        assertNotNull(terdekat.single().jarakKm)

        // Dua penerima mengajukan; tidak bisa dobel
        val ajuA = client.kirim("/donasi/${donasi.id}/pengajuan", AjukanRequest("untuk keluarga"), penerimaA).body<PengajuanDto>()
        val ajuB = client.kirim("/donasi/${donasi.id}/pengajuan", AjukanRequest(), penerimaB).body<PengajuanDto>()
        assertEquals("MENUNGGU", ajuA.status)
        assertNull(ajuA.noHpKontak, "nomor donatur belum dibuka sebelum disetujui")
        assertEquals(HttpStatusCode.Conflict, client.kirim("/donasi/${donasi.id}/pengajuan", AjukanRequest(), penerimaA).status)

        // Donatur melihat pengajuan lalu menyetujui A
        assertEquals(2, client.ambil("/donasi/${donasi.id}/pengajuan", donatur).body<List<PengajuanDto>>().size)
        val disetujui = client.kirim("/pengajuan/${ajuA.id}/setujui", token = donatur).body<PengajuanDto>()
        assertEquals("DISETUJUI", disetujui.status)
        assertEquals("6282222222222", disetujui.noHpKontak)
        assertEquals("DIPROSES", client.ambil("/donasi/${donasi.id}", donatur).body<DonasiDto>().status)

        // B otomatis ditolak; donasi hilang dari daftar terdekat
        val milikB = client.ambil("/pengajuan/saya", penerimaB).body<List<PengajuanDto>>().single()
        assertEquals(ajuB.id, milikB.id)
        assertEquals("DITOLAK", milikB.status)
        assertTrue(client.ambil("/donasi", penerimaB).body<List<DonasiDto>>().isEmpty())

        // A konfirmasi diterima -> donasi selesai
        val milikA = client.ambil("/pengajuan/saya", penerimaA).body<List<PengajuanDto>>().single()
        assertEquals("6281111111111", milikA.noHpKontak)
        assertEquals("Roti Enak", milikA.donasi?.nama)
        assertEquals("DITERIMA", client.kirim("/pengajuan/${ajuA.id}/diterima", token = penerimaA).body<PengajuanDto>().status)
        assertEquals("SELESAI", client.ambil("/donasi/${donasi.id}", donatur).body<DonasiDto>().status)

        // Ringkasan & notifikasi donatur
        assertEquals(RingkasanDonaturDto(2500, 1, 0), client.ambil("/donasi/saya/ringkasan", donatur).body())
        val notif = client.ambil("/notifikasi", donatur).body<List<NotifikasiDto>>()
        assertEquals("Pangan sudah diterima", notif.first().judul)
        assertEquals(HttpStatusCode.NoContent, client.kirim("/notifikasi/${notif.first().id}/baca", token = donatur).status)
        assertTrue(client.ambil("/notifikasi", donatur).body<List<NotifikasiDto>>().first().dibaca)
    }

    @Test
    fun `penerima batal setelah disetujui membuat donasi tersedia lagi`() = testApplication {
        val client = siapkan()
        val donatur = client.daftar("081111111111", "1271010101980001", "DONOR")
        val penerima = client.daftar("082222222222", "1271010101980002", "RECIPIENT")
        val donasi = client.kirim("/donasi", roti(), donatur).body<DonasiDto>()
        val aju = client.kirim("/donasi/${donasi.id}/pengajuan", AjukanRequest(), penerima).body<PengajuanDto>()
        client.kirim("/pengajuan/${aju.id}/setujui", token = donatur)

        assertEquals("DIBATALKAN", client.kirim("/pengajuan/${aju.id}/batal", token = penerima).body<PengajuanDto>().status)

        assertEquals("TERSEDIA", client.ambil("/donasi/${donasi.id}", donatur).body<DonasiDto>().status)
    }

    @Test
    fun `peran dan kepemilikan dicek`() = testApplication {
        val client = siapkan()
        val donatur = client.daftar("081111111111", "1271010101980001", "DONOR")
        val donaturLain = client.daftar("084444444444", "1271010101980004", "DONOR")
        val penerima = client.daftar("082222222222", "1271010101980002", "RECIPIENT")
        val donasi = client.kirim("/donasi", roti(), donatur).body<DonasiDto>()
        val aju = client.kirim("/donasi/${donasi.id}/pengajuan", AjukanRequest(), penerima).body<PengajuanDto>()

        assertEquals(HttpStatusCode.Forbidden, client.kirim("/donasi", roti(), penerima).status)
        assertEquals(HttpStatusCode.Forbidden, client.kirim("/donasi/${donasi.id}/pengajuan", AjukanRequest(), donatur).status)
        assertEquals(HttpStatusCode.Forbidden, client.kirim("/pengajuan/${aju.id}/setujui", token = donaturLain).status)
        assertEquals(HttpStatusCode.Forbidden, client.kirim("/donasi/${donasi.id}/batal", token = donaturLain).status)
        assertEquals(HttpStatusCode.Forbidden, client.ambil("/donasi/${donasi.id}/pengajuan", donaturLain).status)
        assertEquals(HttpStatusCode.Conflict, client.kirim("/pengajuan/${aju.id}/diterima", token = penerima).status)
        assertEquals(HttpStatusCode.NotFound, client.ambil("/donasi/bukan-uuid", donatur).status)
    }

    @Test
    fun `donasi dengan batas ambil lampau ditolak`() = testApplication {
        val client = siapkan()
        val donatur = client.daftar("081111111111", "1271010101980001", "DONOR")

        val response = client.kirim("/donasi", roti().copy(batasAmbil = "2020-01-01T00:00:00Z"), donatur)

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }
}
