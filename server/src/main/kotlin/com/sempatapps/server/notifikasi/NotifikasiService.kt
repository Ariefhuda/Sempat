package com.sempatapps.server.notifikasi

import com.sempatapps.server.auth.ApiException
import com.sempatapps.server.db.Notifikasi
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.Instant
import java.util.UUID

@Serializable
data class NotifikasiDto(
    val id: String,
    val judul: String,
    val pesan: String,
    val dibaca: Boolean,
    /** ISO-8601 UTC, mis. 2026-11-22T05:00:00Z */
    val dibuatPada: String,
)

/** Panggil dari dalam transaction yang sedang berjalan, supaya ikut batal kalau transaksinya gagal. */
fun tambahNotifikasi(userId: UUID, judul: String, pesan: String, waktu: Instant) {
    Notifikasi.insert {
        it[id] = UUID.randomUUID()
        it[Notifikasi.userId] = userId
        it[Notifikasi.judul] = judul.take(100)
        it[Notifikasi.pesan] = pesan.take(255)
        it[dibuatPada] = waktu
    }
}

class NotifikasiService(private val db: Database) {

    fun daftar(userId: UUID): List<NotifikasiDto> = transaction(db) {
        Notifikasi.selectAll()
            .where { Notifikasi.userId eq userId }
            .orderBy(Notifikasi.dibuatPada, SortOrder.DESC)
            .limit(100)
            .map {
                NotifikasiDto(
                    id = it[Notifikasi.id].toString(),
                    judul = it[Notifikasi.judul],
                    pesan = it[Notifikasi.pesan],
                    dibaca = it[Notifikasi.dibaca],
                    dibuatPada = it[Notifikasi.dibuatPada].toString(),
                )
            }
    }

    fun tandaiDibaca(userId: UUID, id: UUID) {
        val diubah = transaction(db) {
            Notifikasi.update({ (Notifikasi.id eq id) and (Notifikasi.userId eq userId) }) { it[dibaca] = true }
        }
        if (diubah == 0) throw ApiException(404, "Notifikasi tidak ditemukan")
    }
}
