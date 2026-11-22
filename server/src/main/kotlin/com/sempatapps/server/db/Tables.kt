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

/** Pangan yang dibagikan donatur. Status: lihat [StatusDonasi]. */
object Donasi : Table("donasi") {
    val id = uuid("id")
    val donaturId = uuid("donatur_id").references(Users.id).index()
    val nama = varchar("nama", 100)
    val kategori = varchar("kategori", 50)
    /** Teks bebas untuk ditampilkan, mis. "2 bungkus" atau "250 g". */
    val jumlah = varchar("jumlah", 50)
    /** Dipakai untuk ringkasan "sudah mendonasikan 8 kg" di beranda donatur. */
    val beratGram = integer("berat_gram")
    val catatan = varchar("catatan", 500).default("")
    val fotoUrl = varchar("foto_url", 500).nullable()
    val alamatAmbil = varchar("alamat_ambil", 255)
    val latitude = double("latitude")
    val longitude = double("longitude")
    val batasAmbil = timestamp("batas_ambil")
    val status = varchar("status", 16).index()
    val dibuatPada = timestamp("dibuat_pada")
    val diperbaruiPada = timestamp("diperbarui_pada")

    override val primaryKey = PrimaryKey(id)
}

/** Permintaan penerima atas satu donasi. Status: lihat [StatusPengajuan]. */
object Pengajuan : Table("pengajuan") {
    val id = uuid("id")
    val donasiId = uuid("donasi_id").references(Donasi.id).index()
    val penerimaId = uuid("penerima_id").references(Users.id).index()
    val catatan = varchar("catatan", 500).default("")
    val buktiUrl = varchar("bukti_url", 500).nullable()
    val status = varchar("status", 16)
    val dibuatPada = timestamp("dibuat_pada")
    val diperbaruiPada = timestamp("diperbarui_pada")

    override val primaryKey = PrimaryKey(id)
}

object Notifikasi : Table("notifikasi") {
    val id = uuid("id")
    val userId = uuid("user_id").references(Users.id).index()
    val judul = varchar("judul", 100)
    val pesan = varchar("pesan", 255)
    val dibaca = bool("dibaca").default(false)
    val dibuatPada = timestamp("dibuat_pada")

    override val primaryKey = PrimaryKey(id)
}

object StatusDonasi {
    const val TERSEDIA = "TERSEDIA"     // bisa diajukan penerima
    const val DIPROSES = "DIPROSES"     // satu pengajuan sudah disetujui, menunggu diambil
    const val SELESAI = "SELESAI"       // sudah diterima penerima
    const val DIBATALKAN = "DIBATALKAN"
}

object StatusPengajuan {
    const val MENUNGGU = "MENUNGGU"
    const val DISETUJUI = "DISETUJUI"
    const val DITOLAK = "DITOLAK"
    const val DITERIMA = "DITERIMA"     // penerima konfirmasi sudah menerima pangan
    const val DIBATALKAN = "DIBATALKAN"
    val AKTIF = listOf(MENUNGGU, DISETUJUI)
}

/** Urutan penting: tabel yang direferensikan dibuat lebih dulu. */
val SEMUA_TABEL = arrayOf(Users, Lokasi, KodeOtp, Donasi, Pengajuan, Notifikasi)
