package com.sempatapps.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ValidasiDaftarTest {

    private val hariIni = LocalDate.of(2026, 11, 18)
    private val ktpLengkap = KtpData(
        nik = "1271010101980001",
        nama = "SITI RAHMA",
        tanggalLahir = "12-05-1998",
        kabupatenKota = "KOTA MEDAN",
    )

    @Test
    fun `nomor HP dengan awalan 0, 62, atau 8 diterima`() {
        assertTrue(noHpValid("081234567890"))
        assertTrue(noHpValid("6281234567890"))
        assertTrue(noHpValid("81234567890"))
    }

    @Test
    fun `nomor HP terlalu pendek atau bukan 08 ditolak`() {
        assertFalse(noHpValid("0812"))
        assertFalse(noHpValid("0212345678"))
        assertFalse(noHpValid(""))
    }

    @Test
    fun `KTP lengkap tidak ada kolom salah`() {
        assertEquals(emptySet<KtpField>(), kolomKtpSalah(ktpLengkap, hariIni))
    }

    @Test
    fun `NIK kurang dari 16 angka ditandai`() {
        assertEquals(setOf(KtpField.NIK), kolomKtpSalah(ktpLengkap.copy(nik = "127101"), hariIni))
    }

    @Test
    fun `tanggal lahir yang tidak ada di kalender ditandai`() {
        assertEquals(setOf(KtpField.TANGGAL_LAHIR), kolomKtpSalah(ktpLengkap.copy(tanggalLahir = "31-02-1998"), hariIni))
    }

    @Test
    fun `tanggal lahir di masa depan ditandai`() {
        assertEquals(setOf(KtpField.TANGGAL_LAHIR), kolomKtpSalah(ktpLengkap.copy(tanggalLahir = "01-01-2030"), hariIni))
    }

    @Test
    fun `KTP kosong (isi manual) semua kolom ditandai`() {
        assertEquals(KtpField.entries.toSet(), kolomKtpSalah(KtpData(), hariIni))
    }
}
