package com.sempatapps.server.db

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.timestamp

object Users : Table("users") {
    val id = uuid("id")
    val peran = varchar("peran", 16) // DONOR / RECIPIENT, sama dengan HomeRole di app
    val noHp = varchar("no_hp", 20).uniqueIndex()

    // NIK tidak disimpan utuh. nikHash untuk cek "NIK sudah terdaftar", nikTampil untuk ditampilkan di profil.
    val nikHash = varchar("nik_hash", 64).uniqueIndex()
    val nikTampil = varchar("nik_tampil", 24)

    val nama = varchar("nama", 100)
    val tanggalLahir = date("tanggal_lahir")
    val kabupatenKota = varchar("kabupaten_kota", 100)
    val passwordHash = varchar("password_hash", 72)
    val dibuatPada = timestamp("dibuat_pada")

    override val primaryKey = PrimaryKey(id)
}

object Lokasi : Table("lokasi") {
    val userId = uuid("user_id").references(Users.id)
    val latitude = double("latitude")
    val longitude = double("longitude")
    val alamat = varchar("alamat", 255)
    val patokan = varchar("patokan", 255)

    override val primaryKey = PrimaryKey(userId)
}

/** Satu nomor HP hanya punya satu kode aktif. Kirim ulang = kode lama ditimpa. */
object KodeOtp : Table("kode_otp") {
    val noHp = varchar("no_hp", 20)
    val kodeHash = varchar("kode_hash", 72)
    val berlakuSampai = timestamp("berlaku_sampai")
    val dikirimPada = timestamp("dikirim_pada")
    val salahCoba = integer("salah_coba").default(0)

    override val primaryKey = PrimaryKey(noHp)
}
