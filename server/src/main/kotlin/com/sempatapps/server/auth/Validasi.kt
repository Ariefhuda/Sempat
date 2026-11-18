package com.sempatapps.server.auth

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle

/** "0812-3456-7890", "+62 812..." → "6281234567890". */
fun normalisasiNoHp(input: String): String {
    val angka = input.filter { it.isDigit() }
    val hasil = when {
        angka.startsWith("62") -> angka
        angka.startsWith("0") -> "62" + angka.drop(1)
        angka.startsWith("8") -> "62$angka"
        else -> angka
    }
    if (!hasil.matches(Regex("^628\\d{7,11}$"))) throw ApiException(400, "Nomor HP tidak valid")
    return hasil
}

fun validasiNik(nik: String): String {
    val angka = nik.filter { it.isDigit() }
    if (angka.length != 16) throw ApiException(400, "NIK harus 16 angka")
    return angka
}

/** "1271010101980001" → "1271 •••• •••• 0001" (sama dengan tampilan di Figma). */
fun samarkanNik(nik: String): String = "${nik.take(4)} •••• •••• ${nik.takeLast(4)}"

private val FORMAT_TANGGAL = DateTimeFormatter.ofPattern("dd-MM-uuuu").withResolverStyle(ResolverStyle.STRICT)

fun parseTanggalLahir(input: String): LocalDate = try {
    LocalDate.parse(input, FORMAT_TANGGAL)
} catch (_: DateTimeParseException) {
    throw ApiException(400, "Tanggal lahir harus berformat dd-MM-yyyy")
}

fun validasiPassword(password: String) {
    if (password.length < 8) throw ApiException(400, "Password minimal 8 huruf atau angka")
    // bcrypt cuma membaca 72 byte pertama
    if (password.toByteArray().size > 72) throw ApiException(400, "Password terlalu panjang")
}
