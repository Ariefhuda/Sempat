package com.sempatapps.server

/** Semua setelan server dibaca dari environment variable (lihat .env.example). */
data class Config(
    val port: Int,
    val dbUrl: String,
    val dbUser: String,
    val dbPassword: String,
    val jwtSecret: String,
    /** Kunci untuk meng-hash NIK. Kalau diganti, NIK lama tidak bisa dicocokkan lagi. */
    val nikSecret: String,
    /**
     * true = kode OTP ikut dikirim di response (dan dicetak di log) supaya bisa dites tanpa SMS/WhatsApp.
     * Matikan kalau sudah pakai pengirim OTP sungguhan.
     */
    val otpDevMode: Boolean,
) {
    companion object {
        fun fromEnv(): Config = Config(
            port = env("PORT", "8080").toInt(),
            dbUrl = env("DB_URL", "jdbc:postgresql://localhost:5432/sempat"),
            dbUser = env("DB_USER", "sempat"),
            dbPassword = env("DB_PASSWORD"),
            jwtSecret = env("JWT_SECRET"),
            nikSecret = env("NIK_SECRET"),
            otpDevMode = env("OTP_DEV_MODE", "false").toBoolean(),
        )

        private fun env(name: String, default: String? = null): String =
            System.getenv(name)?.takeIf { it.isNotBlank() }
                ?: default
                ?: error("Environment variable $name belum diisi")
    }
}
