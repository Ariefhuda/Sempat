package com.sempatapps.ai.ktp

import kotlin.math.min
import kotlin.math.roundToInt

/** Area persegi panjang di foto, dalam piksel. */
data class Potongan(val kiri: Int, val atas: Int, val kanan: Int, val bawah: Int) {
    val lebar get() = kanan - kiri
    val tinggi get() = bawah - atas
}

/** Rasio KTP: 85,6 × 54 mm. */
private const val RASIO_KARTU = 1.586f

/** Seberapa besar potongan tengah dibanding lebar foto (atau lebar kartu terbesar yang muat). */
private val SKALA_POTONGAN = listOf(0.85f, 0.65f, 0.5f, 0.38f)

/** Tinggi potongan sedikit dilebihkan supaya KTP yang agak miring/bergeser tetap masuk. */
private const val LONGGAR_TINGGI = 1.25f

/**
 * Model hanya membaca KTP yang memenuhi hampir seluruh gambar 640×640. Foto HP biasanya tegak dengan
 * KTP kecil di tengah, sehingga hurufnya terlalu kecil setelah diperkecil (KTP selebar 50% foto: skor ~0).
 *
 * Jadi deteksi dijalankan di foto penuh **dan** beberapa potongan tengah berbentuk kartu;
 * [terbaikPerKolom] lalu mengambil kotak terbaik tiap kolom dari semuanya.
 */
fun potonganKandidat(lebarFoto: Int, tinggiFoto: Int): List<Potongan> {
    val penuh = Potongan(0, 0, lebarFoto, tinggiFoto)
    // Lebar kartu terbesar yang muat di foto (foto mendatar yang pendek dibatasi tingginya).
    val lebarDasar = min(lebarFoto.toFloat(), tinggiFoto * RASIO_KARTU)
    val tengah = SKALA_POTONGAN.map { skala ->
        val lebar = lebarDasar * skala
        val tinggi = min(tinggiFoto.toFloat(), lebar / RASIO_KARTU * LONGGAR_TINGGI)
        val kiri = ((lebarFoto - lebar) / 2f).roundToInt()
        val atas = ((tinggiFoto - tinggi) / 2f).roundToInt()
        Potongan(kiri, atas, lebarFoto - kiri, tinggiFoto - atas)
    }
    return listOf(penuh) + tengah
}

/** Kotak hasil deteksi di dalam [potongan] → koordinat foto asli. */
fun DetectedBox.geser(potongan: Potongan): DetectedBox = copy(
    left = left + potongan.kiri,
    top = top + potongan.atas,
    right = right + potongan.kiri,
    bottom = bottom + potongan.atas,
)

/** Satu kotak per kolom: yang skornya paling tinggi dari semua potongan. */
fun terbaikPerKolom(kotak: List<DetectedBox>): List<DetectedBox> =
    kotak.groupBy { it.label }.values.map { sama -> sama.maxBy { it.confidence } }
