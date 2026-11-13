package com.sempatapps.ai.ktp

import com.sempatapps.domain.model.KtpField
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class KtpParserTest {

    private val parser = KtpParser(today = { LocalDate.of(2026, 11, 9) })

    private fun d(label: String, text: String, conf: Float = 0.9f) = KtpDetection(label, text, conf)

    private val fullKtp = listOf(
        d("province_city", "PROVINSI KALIMANTAN TENGAH\nKABUPATEN KATINGAN"),
        d("nik", "6206101404780001"),
        d("name", "MUKHAMAD IDRUS"),
        d("birth", "KERAMAT, 14-04-1978"),
        d("gender", "LAKI-LAKI"),
        d("blood_type", "-"),
        d("address", "KAMPUNG KERAMAT"),
        d("religion", "SLAM"),
        d("job", "WIRASWASTA"),
    )

    @Test
    fun `KTP lengkap terpetakan ke kolom yang dipakai form`() {
        val r = parser.parse(fullKtp)
        assertEquals("6206101404780001", r.nik)
        assertEquals("MUKHAMAD IDRUS", r.nama)
        assertEquals("14-04-1978", r.tanggalLahir)
        assertEquals("KABUPATEN KATINGAN", r.kabupatenKota)
        assertTrue("tidak ada yang perlu dicek, dapat ${r.perluDicek}", r.perluDicek.isEmpty())
    }

    @Test
    fun `hanya kolom yang dipakai form yang perlu di-OCR`() {
        assertEquals(setOf("nik", "name", "birth", "province_city"), KtpParser.LABEL_DIPAKAI)
    }

    @Test
    fun `kolom yang tidak dipakai tidak membuat perlu dicek walau tidak terdeteksi`() {
        val r = parser.parse(fullKtp.filter { it.label in KtpParser.LABEL_DIPAKAI })
        assertTrue(r.perluDicek.isEmpty())
    }

    @Test
    fun `NIK salah baca huruf dirapikan jadi angka`() {
        assertEquals("3273014105900002", KtpParser.cleanNik(": 3273O14IO59OO0O2"))
        assertEquals("3201234567890123", KtpParser.cleanNik("NIK : 32O1 2345 6789 O123"))
    }

    @Test
    fun `judul kolom yang ikut terbaca dibuang`() {
        assertEquals("BUDI SANTOSO", KtpParser.cleanText("Nama : Budi  Santoso"))
    }

    @Test
    fun `tanggal lahir diambil dari kolom tempat tanggal lahir dan dinormalkan`() {
        assertEquals("05-01-1990", KtpParser.birthDate("JAKARTA, 5-1-199O"))
        assertEquals("", KtpParser.birthDate("BANDUNG, 31-02-1990"))
    }

    @Test
    fun `kota diambil dari baris kedua walau OCR satu baris`() {
        assertEquals("KOTA BANDUNG", KtpParser.city("PROVINSI JAWA BARAT KOTA BANDUNG"))
        assertEquals("JAKARTA SELATAN", KtpParser.city("PROVINSI DKI JAKARTA\nJAKARTA SELATAN"))
        assertEquals("KABUPATEN KATINGAN", KtpParser.city("PROVINSI KALIMANTAN TENGAH\nKAB. KATINGAN"))
    }

    @Test
    fun `tanggal lahir diisi dari NIK kalau tidak terbaca`() {
        // Perempuan: tanggal 14 + 40 = 54
        val r = parser.parse(listOf(d("nik", "3273015404950002")))
        assertEquals("14-04-1995", r.tanggalLahir)
        assertFalse(KtpField.TANGGAL_LAHIR in r.perluDicek)
    }

    @Test
    fun `tanggal lahir bertentangan dengan NIK ditandai perlu dicek`() {
        val r = parser.parse(listOf(d("nik", "3273015404950002"), d("birth", "BANDUNG, 15-04-1995")))
        assertTrue(KtpField.TANGGAL_LAHIR in r.perluDicek)
        assertTrue(KtpField.NIK in r.perluDicek)
    }

    @Test
    fun `kolom kosong dan skor rendah ditandai perlu dicek`() {
        val r = parser.parse(listOf(d("nik", "6206101404780001"), d("name", "BUDI", conf = 0.2f)))
        assertTrue(KtpField.NAMA in r.perluDicek)
        assertTrue(KtpField.KABUPATEN_KOTA in r.perluDicek)
        assertFalse(KtpField.NIK in r.perluDicek)
    }

    @Test
    fun `NIK kurang dari 16 digit ditandai`() {
        val r = parser.parse(listOf(d("nik", "62061014047800")))
        assertTrue(KtpField.NIK in r.perluDicek)
    }

    @Test
    fun `deteksi dobel memakai skor tertinggi`() {
        val r = parser.parse(listOf(d("name", "BUDl", 0.6f), d("name", "BUDI SANTOSO", 0.95f)))
        assertEquals("BUDI SANTOSO", r.nama)
    }
}
