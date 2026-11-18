package com.sempatapps.server.auth

import com.sempatapps.server.db.KodeOtp
import com.sempatapps.server.db.Lokasi
import com.sempatapps.server.db.Users
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.upsert
import org.mindrot.jbcrypt.BCrypt
import org.slf4j.LoggerFactory
import java.security.SecureRandom
import java.time.Clock
import java.time.Duration
import java.util.UUID
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class AuthService(
    private val db: Database,
    private val tokens: TokenService,
    nikSecret: String,
    private val otpDevMode: Boolean,
    private val clock: Clock = Clock.systemUTC(),
) {
    private val log = LoggerFactory.getLogger(AuthService::class.java)
    private val random = SecureRandom()
    private val nikKey = SecretKeySpec(nikSecret.toByteArray(), "HmacSHA256")

    fun kirimOtp(request: KirimOtpRequest): KirimOtpResponse {
        val noHp = normalisasiNoHp(request.noHp)
        val sekarang = clock.instant()
        val kode = transaction(db) {
            val sudahTerdaftar = Users.selectAll().where { Users.noHp eq noHp }.any()
            if (sudahTerdaftar) throw ApiException(409, "Nomor HP sudah terdaftar. Silakan masuk.")

            val terakhir = KodeOtp.selectAll().where { KodeOtp.noHp eq noHp }.singleOrNull()
            if (terakhir != null && terakhir[KodeOtp.dikirimPada] + JEDA_KIRIM_ULANG > sekarang) {
                throw ApiException(429, "Tunggu sebentar sebelum kirim ulang kode")
            }

            val kode = (0 until 6).joinToString("") { random.nextInt(10).toString() }
            KodeOtp.upsert {
                it[KodeOtp.noHp] = noHp
                it[kodeHash] = BCrypt.hashpw(kode, BCrypt.gensalt())
                it[berlakuSampai] = sekarang + UMUR_KODE
                it[dikirimPada] = sekarang
                it[salahCoba] = 0
            }
            kode
        }

        // TODO: kirim lewat WhatsApp/SMS gateway. Sementara kode cuma muncul di log server.
        log.info("Kode OTP untuk {}: {}", noHp, kode)

        return KirimOtpResponse(
            berlakuDetik = UMUR_KODE.seconds.toInt(),
            kirimUlangDetik = JEDA_KIRIM_ULANG.seconds.toInt(),
            kodeDev = kode.takeIf { otpDevMode },
        )
    }

    fun cekOtp(request: CekOtpRequest): CekOtpResponse {
        val noHp = normalisasiNoHp(request.noHp)
        val cocok = transaction(db) {
            val baris = KodeOtp.selectAll().where { KodeOtp.noHp eq noHp }.singleOrNull()
                ?: throw ApiException(400, "Kode belum dikirim. Minta kode dulu.")
            if (baris[KodeOtp.salahCoba] >= MAKS_SALAH) {
                throw ApiException(429, "Terlalu banyak salah. Kirim ulang kode.")
            }
            if (baris[KodeOtp.berlakuSampai] < clock.instant()) {
                throw ApiException(400, "Kode sudah kedaluwarsa. Kirim ulang kode.")
            }
            val cocok = BCrypt.checkpw(request.kode, baris[KodeOtp.kodeHash])
            if (cocok) {
                KodeOtp.deleteWhere { KodeOtp.noHp eq noHp }
            } else {
                KodeOtp.update({ KodeOtp.noHp eq noHp }) { it[salahCoba] = baris[KodeOtp.salahCoba] + 1 }
            }
            cocok
        }
        if (!cocok) throw ApiException(400, "Kode salah")
        return CekOtpResponse(tokens.buatTokenVerifikasi(noHp))
    }

    fun daftar(request: DaftarRequest): SesiResponse {
        val noHp = tokens.cekTokenVerifikasi(request.tokenVerifikasi)
            ?: throw ApiException(401, "Verifikasi nomor HP sudah kedaluwarsa. Ulangi kirim kode.")
        if (request.peran !in PERAN) throw ApiException(400, "Peran tidak dikenal")
        val nik = validasiNik(request.nik)
        val nama = request.nama.trim().ifEmpty { throw ApiException(400, "Nama wajib diisi") }
        val kota = request.kabupatenKota.trim().ifEmpty { throw ApiException(400, "Kota wajib diisi") }
        val tanggalLahir = parseTanggalLahir(request.tanggalLahir)
        validasiPassword(request.password)
        val lokasi = request.lokasi
        if (lokasi.latitude !in -90.0..90.0 || lokasi.longitude !in -180.0..180.0) {
            throw ApiException(400, "Lokasi tidak valid")
        }

        val nikHash = hashNik(nik)
        val id = UUID.randomUUID()
        try {
            transaction(db) {
                if (Users.selectAll().where { Users.nikHash eq nikHash }.any()) {
                    throw ApiException(409, "NIK ini sudah terdaftar")
                }
                if (Users.selectAll().where { Users.noHp eq noHp }.any()) {
                    throw ApiException(409, "Nomor HP sudah terdaftar. Silakan masuk.")
                }
                Users.insert {
                    it[Users.id] = id
                    it[peran] = request.peran
                    it[Users.noHp] = noHp
                    it[Users.nikHash] = nikHash
                    it[nikTampil] = samarkanNik(nik)
                    it[Users.nama] = nama
                    it[Users.tanggalLahir] = tanggalLahir
                    it[kabupatenKota] = kota
                    it[passwordHash] = BCrypt.hashpw(request.password, BCrypt.gensalt())
                    it[dibuatPada] = clock.instant()
                }
                Lokasi.insert {
                    it[userId] = id
                    it[latitude] = lokasi.latitude
                    it[longitude] = lokasi.longitude
                    it[alamat] = lokasi.alamat.take(255)
                    it[patokan] = lokasi.patokan.take(255)
                }
            }
        } catch (e: ExposedSQLException) {
            // Dua request daftar bersamaan dengan data sama: yang kalah kena unique index.
            log.warn("Gagal simpan user baru", e)
            throw ApiException(409, "NIK atau nomor HP sudah terdaftar")
        }

        return SesiResponse(
            token = tokens.buatTokenSesi(id, request.peran),
            user = UserDto(id = id.toString(), peran = request.peran, nama = nama, noHp = noHp),
        )
    }

    private fun hashNik(nik: String): String {
        val mac = Mac.getInstance("HmacSHA256").apply { init(nikKey) }
        return mac.doFinal(nik.toByteArray()).joinToString("") { "%02x".format(it) }
    }

    companion object {
        val PERAN = setOf("DONOR", "RECIPIENT")
        private val UMUR_KODE = Duration.ofMinutes(5)
        private val JEDA_KIRIM_ULANG = Duration.ofSeconds(60)
        private const val MAKS_SALAH = 5
    }
}
