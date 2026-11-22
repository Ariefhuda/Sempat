package com.sempatapps.server.auth

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import java.util.UUID

const val AUTH_SESI = "sesi"

/** Pengguna yang sedang login, diambil dari token sesi (header Authorization: Bearer ...). */
data class Pengguna(val id: UUID, val peran: String) {
    val donatur get() = peran == "DONOR"
    val penerima get() = peran == "RECIPIENT"

    fun wajibDonatur() {
        if (!donatur) throw ApiException(403, "Fitur ini hanya untuk donatur")
    }

    fun wajibPenerima() {
        if (!penerima) throw ApiException(403, "Fitur ini hanya untuk penerima")
    }
}

fun Application.pasangAuth(tokens: TokenService) {
    install(Authentication) {
        jwt(AUTH_SESI) {
            verifier(tokens.verifier)
            validate { cred ->
                val p = cred.payload
                val idValid = runCatching { UUID.fromString(p.subject) }.isSuccess
                if (p.getClaim("tujuan").asString() == "sesi" && idValid) JWTPrincipal(p) else null
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Sesi berakhir. Silakan masuk lagi."))
            }
        }
    }
}

fun ApplicationCall.pengguna(): Pengguna {
    val p = principal<JWTPrincipal>() ?: throw ApiException(401, "Silakan masuk dulu")
    return Pengguna(UUID.fromString(p.payload.subject), p.payload.getClaim("peran").asString())
}
