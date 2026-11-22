package com.sempatapps.server.profil

import com.sempatapps.server.auth.ApiException
import com.sempatapps.server.auth.LokasiDto
import com.sempatapps.server.db.Lokasi
import com.sempatapps.server.db.Users
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.upsert
import java.time.format.DateTimeFormatter
import java.util.UUID

@Serializable
data class ProfilDto(
    val id: String,
    val peran: String,
    val nama: String,
    val noHp: String,
    /** NIK tersamar, mis. "1271 •••• •••• 0001". NIK utuh tidak pernah disimpan. */
    val nik: String,
    /** dd-MM-yyyy */
    val tanggalLahir: String,
    val kabupatenKota: String,
    val lokasi: LokasiDto?,
)

class ProfilService(private val db: Database) {

    fun ambil(userId: UUID): ProfilDto = transaction(db) {
        val u = Users.selectAll().where { Users.id eq userId }.singleOrNull()
            ?: throw ApiException(401, "Akun tidak ditemukan. Silakan masuk lagi.")
        ProfilDto(
            id = userId.toString(),
            peran = u[Users.peran],
            nama = u[Users.nama],
            noHp = u[Users.noHp],
            nik = u[Users.nikTampil],
            tanggalLahir = u[Users.tanggalLahir].format(FORMAT_TANGGAL),
            kabupatenKota = u[Users.kabupatenKota],
            lokasi = lokasi(userId),
        )
    }

    fun ubahLokasi(userId: UUID, lokasi: LokasiDto): ProfilDto {
        validasiLokasi(lokasi)
        transaction(db) {
            Lokasi.upsert {
                it[Lokasi.userId] = userId
                it[latitude] = lokasi.latitude
                it[longitude] = lokasi.longitude
                it[alamat] = lokasi.alamat.trim().take(255)
                it[patokan] = lokasi.patokan.trim().take(255)
            }
        }
        return ambil(userId)
    }

    companion object {
        private val FORMAT_TANGGAL = DateTimeFormatter.ofPattern("dd-MM-yyyy")

        /** Harus dipanggil di dalam transaction. */
        fun lokasi(userId: UUID): LokasiDto? =
            Lokasi.selectAll().where { Lokasi.userId eq userId }.singleOrNull()?.let {
                LokasiDto(it[Lokasi.latitude], it[Lokasi.longitude], it[Lokasi.alamat], it[Lokasi.patokan])
            }
    }
}

fun validasiLokasi(lokasi: LokasiDto) {
    if (lokasi.latitude !in -90.0..90.0 || lokasi.longitude !in -180.0..180.0) {
        throw ApiException(400, "Lokasi tidak valid")
    }
    if (lokasi.alamat.isBlank()) throw ApiException(400, "Alamat wajib diisi")
}
