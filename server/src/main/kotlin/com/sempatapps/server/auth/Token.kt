package com.sempatapps.server.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import java.time.Duration
import java.time.Instant
import java.util.Date
import java.util.UUID

/**
 * Dua jenis token:
 * - token verifikasi: bukti nomor HP sudah lolos OTP, umurnya pendek, cuma dipakai untuk /auth/daftar.
 * - token sesi: dipakai app untuk request berikutnya (header Authorization: Bearer ...).
 */
class TokenService(secret: String) {
    private val algorithm = Algorithm.HMAC256(secret)
    val verifier: com.auth0.jwt.interfaces.JWTVerifier = JWT.require(algorithm).withIssuer(ISSUER).build()

    fun buatTokenVerifikasi(noHp: String): String = JWT.create()
        .withIssuer(ISSUER)
        .withClaim("tujuan", "daftar")
        .withClaim("noHp", noHp)
        .withExpiresAt(Date.from(Instant.now() + UMUR_VERIFIKASI))
        .sign(algorithm)

    /** @return nomor HP yang sudah terverifikasi, atau null kalau token tidak sah / kedaluwarsa. */
    fun cekTokenVerifikasi(token: String): String? = try {
        val jwt = verifier.verify(token)
        if (jwt.getClaim("tujuan").asString() == "daftar") jwt.getClaim("noHp").asString() else null
    } catch (_: JWTVerificationException) {
        null
    }

    fun buatTokenSesi(userId: UUID, peran: String): String = JWT.create()
        .withIssuer(ISSUER)
        .withSubject(userId.toString())
        .withClaim("tujuan", "sesi")
        .withClaim("peran", peran)
        .withExpiresAt(Date.from(Instant.now() + UMUR_SESI))
        .sign(algorithm)

    companion object {
        const val ISSUER = "sempat"
        private val UMUR_VERIFIKASI = Duration.ofMinutes(15)
        private val UMUR_SESI = Duration.ofDays(90)
    }
}
