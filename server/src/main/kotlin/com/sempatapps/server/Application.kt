package com.sempatapps.server

import com.sempatapps.server.auth.ApiException
import com.sempatapps.server.auth.AuthService
import com.sempatapps.server.auth.CekOtpRequest
import com.sempatapps.server.auth.DaftarRequest
import com.sempatapps.server.auth.ErrorResponse
import com.sempatapps.server.auth.AUTH_SESI
import com.sempatapps.server.auth.KirimOtpRequest
import com.sempatapps.server.auth.LokasiDto
import com.sempatapps.server.auth.MasukRequest
import com.sempatapps.server.auth.TokenService
import com.sempatapps.server.auth.pasangAuth
import com.sempatapps.server.auth.pengguna
import com.sempatapps.server.db.connectDatabase
import com.sempatapps.server.donasi.AjukanRequest
import com.sempatapps.server.donasi.BuatDonasiRequest
import com.sempatapps.server.donasi.DonasiService
import com.sempatapps.server.notifikasi.NotifikasiService
import com.sempatapps.server.profil.ProfilService
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.RoutingCall
import io.ktor.server.routing.put
import org.jetbrains.exposed.sql.Database
import java.util.UUID
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json

fun main() {
    val config = Config.fromEnv()
    val db = connectDatabase(config.dbUrl, config.dbUser, config.dbPassword)
    val tokens = TokenService(config.jwtSecret)
    val auth = AuthService(db, tokens, config.nikSecret, config.otpDevMode)

    embeddedServer(Netty, port = config.port, host = "0.0.0.0") {
        module(Layanan.buat(db, tokens, auth))
    }.start(wait = true)
}

/** Semua service yang dipakai route. Test membuat versi sendiri dengan database H2. */
class Layanan(
    val tokens: TokenService,
    val auth: AuthService,
    val profil: ProfilService,
    val donasi: DonasiService,
    val notifikasi: NotifikasiService,
) {
    companion object {
        fun buat(db: Database, tokens: TokenService, auth: AuthService) =
            Layanan(tokens, auth, ProfilService(db), DonasiService(db), NotifikasiService(db))
    }
}

fun Application.module(layanan: Layanan) {
    val auth = layanan.auth
    pasangAuth(layanan.tokens)
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
    install(CallLogging)
    install(StatusPages) {
        exception<ApiException> { call, e ->
            call.respond(HttpStatusCode.fromValue(e.status), ErrorResponse(e.message))
        }
        exception<BadRequestException> { call, _ ->
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("Data yang dikirim tidak lengkap"))
        }
        exception<Throwable> { call, e ->
            call.application.environment.log.error("Error tak terduga", e)
            call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Terjadi kesalahan di server"))
        }
    }

    routing {
        get("/health") { call.respondText("ok") }

        route("/auth") {
            post("/otp/kirim") {
                call.respond(auth.kirimOtp(call.receive<KirimOtpRequest>()))
            }
            post("/otp/cek") {
                call.respond(auth.cekOtp(call.receive<CekOtpRequest>()))
            }
            post("/daftar") {
                call.respond(HttpStatusCode.Created, auth.daftar(call.receive<DaftarRequest>()))
            }
            post("/masuk") {
                call.respond(auth.masuk(call.receive<MasukRequest>()))
            }
        }

        // Semua route di bawah wajib header Authorization: Bearer <token sesi>
        authenticate(AUTH_SESI) {
            route("/me") {
                get { call.respond(layanan.profil.ambil(call.pengguna().id)) }
                put("/lokasi") {
                    call.respond(layanan.profil.ubahLokasi(call.pengguna().id, call.receive<LokasiDto>()))
                }
            }

            route("/donasi") {
                // Penerima: pangan terdekat. ?lat=&lng= opsional (default lokasi profil), ?radiusKm= default 25.
                get {
                    val q = call.request.queryParameters
                    val radius = q["radiusKm"]?.toDoubleOrNull()?.coerceIn(0.1, 200.0) ?: 25.0
                    call.respond(
                        layanan.donasi.terdekat(call.pengguna(), q["lat"]?.toDoubleOrNull(), q["lng"]?.toDoubleOrNull(), radius),
                    )
                }
                post {
                    call.respond(HttpStatusCode.Created, layanan.donasi.buat(call.pengguna(), call.receive<BuatDonasiRequest>()))
                }
                // Donatur: donasi miliknya, ?status=TERSEDIA|DIPROSES|SELESAI|DIBATALKAN
                get("/saya") {
                    val status = call.request.queryParameters["status"]?.uppercase()
                    if (status != null && status !in DonasiService.STATUS_DONASI) throw ApiException(400, "Status tidak dikenal")
                    call.respond(layanan.donasi.milikSaya(call.pengguna(), status))
                }
                get("/saya/ringkasan") { call.respond(layanan.donasi.ringkasan(call.pengguna())) }
                get("/{id}") { call.respond(layanan.donasi.detail(call.idParam())) }
                post("/{id}/batal") { call.respond(layanan.donasi.batalkanDonasi(call.pengguna(), call.idParam())) }
                get("/{id}/pengajuan") {
                    call.respond(layanan.donasi.pengajuanUntukDonasi(call.pengguna(), call.idParam()))
                }
                post("/{id}/pengajuan") {
                    call.respond(
                        HttpStatusCode.Created,
                        layanan.donasi.ajukan(call.pengguna(), call.idParam(), call.receive<AjukanRequest>()),
                    )
                }
            }

            route("/pengajuan") {
                get("/saya") { call.respond(layanan.donasi.pengajuanSaya(call.pengguna())) }
                post("/{id}/setujui") { call.respond(layanan.donasi.setujui(call.pengguna(), call.idParam())) }
                post("/{id}/tolak") { call.respond(layanan.donasi.tolak(call.pengguna(), call.idParam())) }
                post("/{id}/diterima") { call.respond(layanan.donasi.konfirmasiDiterima(call.pengguna(), call.idParam())) }
                post("/{id}/batal") { call.respond(layanan.donasi.batalkanPengajuan(call.pengguna(), call.idParam())) }
            }

            route("/notifikasi") {
                get { call.respond(layanan.notifikasi.daftar(call.pengguna().id)) }
                post("/{id}/baca") {
                    layanan.notifikasi.tandaiDibaca(call.pengguna().id, call.idParam())
                    call.respond(HttpStatusCode.NoContent)
                }
            }
        }
    }
}

private fun RoutingCall.idParam(): UUID =
    parameters["id"]?.let { runCatching { UUID.fromString(it) }.getOrNull() }
        ?: throw ApiException(404, "Data tidak ditemukan")
