package com.sempatapps.server.donasi

import com.sempatapps.server.auth.LokasiDto
import kotlinx.serialization.Serializable

@Serializable
data class BuatDonasiRequest(
    val nama: String,
    val kategori: String,
    /** Teks tampilan, mis. "2 bungkus". */
    val jumlah: String,
    val beratGram: Int,
    /** ISO-8601, mis. "2026-11-22T17:00:00Z" atau "2026-11-22T17:00:00+07:00". */
    val batasAmbil: String,
    val catatan: String = "",
    val fotoUrl: String? = null,
    /** Kosong = pakai lokasi di profil donatur. */
    val lokasi: LokasiDto? = null,
)

@Serializable
data class DonasiDto(
    val id: String,
    val donaturId: String,
    val namaDonatur: String,
    val nama: String,
    val kategori: String,
    val jumlah: String,
    val beratGram: Int,
    val catatan: String,
    val fotoUrl: String?,
    val alamatAmbil: String,
    val latitude: Double,
    val longitude: Double,
    /** Hanya terisi di daftar pangan terdekat (GET /donasi). */
    val jarakKm: Double? = null,
    val batasAmbil: String,
    /** TERSEDIA | DIPROSES | SELESAI | DIBATALKAN */
    val status: String,
    val dibuatPada: String,
)

@Serializable
data class AjukanRequest(
    val catatan: String = "",
    val buktiUrl: String? = null,
)

@Serializable
data class PengajuanDto(
    val id: String,
    val donasiId: String,
    val penerimaId: String,
    val namaPenerima: String,
    val catatan: String,
    val buktiUrl: String?,
    /** MENUNGGU | DISETUJUI | DITOLAK | DITERIMA | DIBATALKAN */
    val status: String,
    /**
     * Nomor HP pihak lain (donatur untuk penerima, penerima untuk donatur).
     * Baru terisi setelah pengajuan disetujui, untuk tombol chat/telepon.
     */
    val noHpKontak: String? = null,
    val dibuatPada: String,
    val diperbaruiPada: String,
    /** Ikut dikirim di GET /pengajuan/saya supaya app tidak perlu request lagi. */
    val donasi: DonasiDto? = null,
)

@Serializable
data class RingkasanDonaturDto(
    /** Total berat donasi yang sudah selesai diterima. */
    val totalBeratGram: Long,
    val jumlahSelesai: Long,
    val jumlahAktif: Long,
)
