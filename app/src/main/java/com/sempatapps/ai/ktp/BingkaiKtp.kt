package com.sempatapps.ai.ktp

import kotlin.math.max
import kotlin.math.roundToInt

/** Perbandingan lebar : tinggi KTP (85,6 × 54 mm). */
const val RASIO_KTP = 1.586f

/** Lebar bingkai di layar kamera, relatif terhadap lebar layar. */
const val LEBAR_BINGKAI = 0.86f

/**
 * Bingkai KTP di layar (di tengah, lebar [LEBAR_BINGKAI]) dipetakan ke potongan di gambar kamera.
 *
 * Gambar ditampilkan seperti PreviewView FILL_CENTER: diperbesar sampai menutupi layar, lalu
 * bagian yang lebih dipotong rata kiri-kanan / atas-bawah. [kelonggaran] menambah sedikit ruang di
 * sekeliling bingkai supaya KTP yang agak keluar garis tetap ikut terlihat.
 */
fun bingkaiDiGambar(
    lebarLayar: Int,
    tinggiLayar: Int,
    lebarGambar: Int,
    tinggiGambar: Int,
    kelonggaran: Float = 0.08f,
): Potongan {
    val skala = max(lebarLayar / lebarGambar.toFloat(), tinggiLayar / tinggiGambar.toFloat())
    val geserX = (lebarLayar - lebarGambar * skala) / 2f
    val geserY = (tinggiLayar - tinggiGambar * skala) / 2f

    val lebar = lebarLayar * LEBAR_BINGKAI * (1 + 2 * kelonggaran)
    val tinggi = lebarLayar * LEBAR_BINGKAI / RASIO_KTP * (1 + 2 * kelonggaran)
    val kiriLayar = (lebarLayar - lebar) / 2f
    val atasLayar = (tinggiLayar - tinggi) / 2f

    val kiri = ((kiriLayar - geserX) / skala).roundToInt().coerceIn(0, lebarGambar - 1)
    val atas = ((atasLayar - geserY) / skala).roundToInt().coerceIn(0, tinggiGambar - 1)
    val kanan = ((kiriLayar + lebar - geserX) / skala).roundToInt().coerceIn(kiri + 1, lebarGambar)
    val bawah = ((atasLayar + tinggi - geserY) / skala).roundToInt().coerceIn(atas + 1, tinggiGambar)
    return Potongan(kiri, atas, kanan, bawah)
}

/**
 * KTP dianggap ada di bingkai kalau NIK dan paling tidak [MIN_KOLOM_TERLIHAT] kolom KTP terlihat.
 * Satu-dua kotak saja sering muncul dari kertas/tulisan lain, jadi belum dihitung.
 */
fun ktpTerlihat(kolom: Set<String>): Boolean = "nik" in kolom && kolom.size >= MIN_KOLOM_TERLIHAT

const val MIN_KOLOM_TERLIHAT = 4
