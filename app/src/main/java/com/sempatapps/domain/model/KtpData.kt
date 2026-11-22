package com.sempatapps.domain.model

/** Kolom KTP yang dipakai di form pendaftaran (layar "Hasil Baca KTP"). */
enum class KtpField {
    NIK,
    NAMA,
    TANGGAL_LAHIR,
    KABUPATEN_KOTA,
}

/**
 * Hasil baca KTP yang sudah dirapikan, siap mengisi form.
 *
 * Hanya kolom yang dipakai aplikasi yang diambil; kolom lain (golongan darah, agama, dst.)
 * sengaja tidak dibaca supaya data pribadi yang disimpan seminimal mungkin.
 *
 * Semua nilai bisa kosong kalau kolomnya tidak terbaca. [perluDicek] berisi kolom yang
 * kosong, tidak lolos validasi, atau bertentangan dengan NIK — tandai kolom ini di form
 * supaya pengguna memeriksanya.
 */
data class KtpData(
    val nik: String = "",
    val nama: String = "",
    /** Format dd-MM-yyyy. */
    val tanggalLahir: String = "",
    /** Mis. "KOTA MEDAN" atau "KABUPATEN KATINGAN". */
    val kabupatenKota: String = "",
    val perluDicek: Set<KtpField> = emptySet(),
)
