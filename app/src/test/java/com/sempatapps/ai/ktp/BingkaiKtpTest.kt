package com.sempatapps.ai.ktp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class BingkaiKtpTest {

    @Test
    fun `gambar seukuran layar dipotong persis di bingkai plus kelonggaran`() {
        val p = bingkaiDiGambar(1000, 2000, 1000, 2000, kelonggaran = 0f)

        // Lebar 86% di tengah, tinggi = lebar / 1,586.
        assertEquals(70, p.kiri)
        assertEquals(930, p.kanan)
        assertTrue("$p", abs(p.tinggi - 542) <= 1)
        assertTrue("$p", abs((2000 - p.bawah) - p.atas) <= 1)
    }

    @Test
    fun `gambar kamera 3 banding 4 yang dipotong kiri-kanan dipetakan dengan skala yang sama`() {
        // Layar 1080x2400, preview 480x640 diperbesar 3,75x (menutupi tinggi), sisi kiri-kanan terpotong.
        val p = bingkaiDiGambar(1080, 2400, 480, 640, kelonggaran = 0f)
        val skala = 2400 / 640f
        val geserX = (1080 - 480 * skala) / 2f

        assertTrue("$p", abs(p.kiri - ((1080 * 0.07f - geserX) / skala)) <= 1)
        assertTrue("$p", abs(p.lebar - 1080 * LEBAR_BINGKAI / skala) <= 1)
        assertTrue("$p", abs(p.lebar / p.tinggi.toFloat() - RASIO_KTP) < 0.02f)
    }

    @Test
    fun `potongan tidak pernah keluar dari gambar`() {
        listOf(Triple(1080, 2400, 480 to 640), Triple(2400, 1080, 640 to 480), Triple(300, 300, 1920 to 1080)).forEach { (lw, lt, g) ->
            val p = bingkaiDiGambar(lw, lt, g.first, g.second, kelonggaran = 0.3f)
            assertTrue("$p", p.kiri >= 0 && p.atas >= 0 && p.kanan <= g.first && p.bawah <= g.second && p.lebar > 0 && p.tinggi > 0)
        }
    }

    @Test
    fun `KTP terlihat hanya kalau NIK dan cukup banyak kolom terdeteksi`() {
        assertTrue(ktpTerlihat(setOf("nik", "name", "birth", "address")))
        assertFalse(ktpTerlihat(setOf("name", "birth", "address", "gender", "job")))
        assertFalse(ktpTerlihat(setOf("nik", "name")))
    }
}
