package com.sempatapps.ai.ktp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PotonganKtpTest {

    @Test
    fun `foto penuh selalu jadi kandidat pertama`() {
        val kandidat = potonganKandidat(3000, 4000)

        assertEquals(Potongan(0, 0, 3000, 4000), kandidat.first())
    }

    @Test
    fun `potongan tengah berbentuk kartu dan makin kecil`() {
        val kandidat = potonganKandidat(3000, 4000).drop(1)

        assertEquals(4, kandidat.size)
        kandidat.zipWithNext().forEach { (besar, kecil) -> assertTrue(kecil.lebar < besar.lebar) }
        kandidat.forEach {
            // Lebih lebar dari tinggi (kartu mendatar), di tengah, dan tidak keluar foto.
            assertTrue("$it", it.lebar > it.tinggi)
            assertTrue("$it", it.kiri >= 0 && it.atas >= 0 && it.kanan <= 3000 && it.bawah <= 4000)
            assertTrue("$it", kotlin.math.abs((3000 - it.kanan) - it.kiri) <= 1)
            assertTrue("$it", kotlin.math.abs((4000 - it.bawah) - it.atas) <= 1)
        }
    }

    @Test
    fun `foto mendatar tetap dipotong di dalam batas foto`() {
        potonganKandidat(4000, 1500).forEach {
            assertTrue("$it", it.kiri >= 0 && it.atas >= 0 && it.kanan <= 4000 && it.bawah <= 1500 && it.lebar > 0 && it.tinggi > 0)
        }
    }

    @Test
    fun `kotak dari potongan digeser balik ke koordinat foto asli`() {
        val kotak = DetectedBox("name", 0.9f, left = 10f, top = 20f, right = 110f, bottom = 40f)

        val digeser = kotak.geser(Potongan(500, 1000, 2500, 2000))

        assertEquals(DetectedBox("name", 0.9f, 510f, 1020f, 610f, 1040f), digeser)
    }

    @Test
    fun `tiap kolom diambil dari kotak dengan skor tertinggi`() {
        val hasil = terbaikPerKolom(
            listOf(
                DetectedBox("name", 0.4f, 0f, 0f, 1f, 1f),
                DetectedBox("birth", 0.8f, 0f, 0f, 1f, 1f),
                DetectedBox("name", 0.9f, 5f, 5f, 6f, 6f),
                DetectedBox("birth", 0.2f, 0f, 0f, 1f, 1f),
            ),
        )

        assertEquals(setOf("name" to 0.9f, "birth" to 0.8f), hasil.map { it.label to it.confidence }.toSet())
    }
}
