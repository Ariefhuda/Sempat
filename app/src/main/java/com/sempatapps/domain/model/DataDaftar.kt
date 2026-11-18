package com.sempatapps.domain.model

data class TitikLokasi(
    val latitude: Double,
    val longitude: Double,
    /** Alamat hasil reverse-geocode titik di peta. Bisa kosong kalau tidak ketemu. */
    val alamat: String,
)

/** Semua isian form daftar yang dikumpulkan dari langkah ke langkah. */
data class DataDaftar(
    val peran: HomeRole,
    val noHp: String = "",
    val ktp: KtpData = KtpData(),
    val lokasi: TitikLokasi? = null,
    val patokan: String = "",
)
