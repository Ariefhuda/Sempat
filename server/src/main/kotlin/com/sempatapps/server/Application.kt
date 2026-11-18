package com.sempatapps.server

import com.sempatapps.server.auth.ApiException
import com.sempatapps.server.auth.AuthService
import com.sempatapps.server.auth.CekOtpRequest
import com.sempatapps.server.auth.DaftarRequest
import com.sempatapps.server.auth.ErrorResponse
import com.sempatapps.server.auth.KirimOtpRequest
import com.sempatapps.server.auth.TokenService
import com.sempatapps.server.db.connectDatabase
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
        module(auth)
    }.start(wait = true)
}

fun Application.module(auth: AuthService) {
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
        }
    }
}
