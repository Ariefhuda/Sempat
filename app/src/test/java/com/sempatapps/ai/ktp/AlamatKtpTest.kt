package com.sempatapps.ai.ktp

import com.sempatapps.domain.model.KtpField
import org.junit.Assert.assertEquals
import org.junit.Test

class AlamatKtpTest {

    @Test
    fun `alamat dan RT RW digabung rapi`() {
        assertEquals("JL SETIA BUDI NO 5, RT/RW 001/002", KtpParser.alamatDari("JL SETIA BUDI NO 5", "001/002"))
    }

    @Test
    fun `judul kolom ikut terbaca dibuang`() {
        assertEquals("JL SETIA BUDI NO 5", KtpParser.alamatDari("Alamat : JL SETIA BUDI NO 5", ""))
        assertEquals("KEL SUNGGAL", KtpParser.cleanKolomAlamat("Kel/Desa : KEL SUNGGAL"))
    }

    @Test
    fun `alamat kosong tetap kosong`() {
        assertEquals("", KtpParser.alamatDari("", "001/002"))
    }

    @Test
    fun `parser mengisi alamat kelurahan dan kecamatan dari KTP`() {
        val hasil = KtpParser().parse(
            listOf(
                KtpDetection("nik", "1271010101900001", 0.9f),
                KtpDetection("name", "BUDI SANTOSO", 0.9f),
                KtpDetection("birth", "MEDAN, 01-01-1990", 0.9f),
                KtpDetection("province_city", "KOTA MEDAN", 0.9f),
                KtpDetection("address", "JL SETIA BUDI NO 5", 0.8f),
                KtpDetection("rt_rw", "001/002", 0.8f),
                KtpDetection("subdistrict", "SUNGGAL", 0.8f),
                KtpDetection("district", "MEDAN SUNGGAL", 0.8f),
            ),
        )

        assertEquals("JL SETIA BUDI NO 5, RT/RW 001/002", hasil.alamat)
        assertEquals("SUNGGAL", hasil.kelurahan)
        assertEquals("MEDAN SUNGGAL", hasil.kecamatan)
        assertEquals(emptySet<KtpField>(), hasil.perluDicek.intersect(setOf(KtpField.ALAMAT)))
    }
}
