package com.sempatapps.domain.model

/** Kolom KTP yang dipakai di form pendaftaran (layar "Hasil Baca KTP"). */
enum class KtpField {
    NIK,
    NAMA,
    TANGGAL_LAHIR,
    KABUPATEN_KOTA,
    ALAMAT,
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
    /** Alamat jalan dari KTP, mis. "JL SETIA BUDI NO 5, RT/RW 001/002". Opsional, tidak wajib. */
    val alamat: String = "",
    val kelurahan: String = "",
    val kecamatan: String = "",
    val perluDicek: Set<KtpField> = emptySet(),
)
