package com.sempatapps.domain.model

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle

// Aturan isian form daftar. Server juga mengecek hal yang sama (server/.../auth/Validasi.kt).

/** Nomor HP Indonesia: boleh diawali 0, 62, atau langsung 8. Contoh: 081234567890. */
fun noHpValid(noHp: String): Boolean {
    val angka = noHp.filter(Char::isDigit).removePrefix("62").removePrefix("0")
    return angka.startsWith("8") && angka.length in 9..12
}

/** Kolom KTP yang isinya belum benar. Kosong = boleh lanjut. */
fun kolomKtpSalah(ktp: KtpData, hariIni: LocalDate = LocalDate.now()): Set<KtpField> = buildSet {
    if (ktp.nik.length != 16 || !ktp.nik.all(Char::isDigit)) add(KtpField.NIK)
    if (ktp.nama.isBlank()) add(KtpField.NAMA)
    if (!tanggalLahirValid(ktp.tanggalLahir, hariIni)) add(KtpField.TANGGAL_LAHIR)
    if (ktp.kabupatenKota.isBlank()) add(KtpField.KABUPATEN_KOTA)
}

private val FORMAT_TANGGAL: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd-MM-uuuu").withResolverStyle(ResolverStyle.STRICT)

private fun tanggalLahirValid(tanggal: String, hariIni: LocalDate): Boolean = try {
    LocalDate.parse(tanggal, FORMAT_TANGGAL).isBefore(hariIni)
} catch (_: DateTimeParseException) {
    false
}
