package com.sempatapps.navigation

import com.sempatapps.domain.model.HomeRole
import kotlinx.serialization.Serializable

@Serializable
object SplashRoute

@Serializable
object OnboardingRoute

// ---- Daftar akun (semua langkah ada di dalam RegisterGraph, berbagi satu RegisterViewModel) ----

@Serializable
object RegisterGraph

/** Pilih peran: Donatur / Penerima. */
@Serializable
object RegisterRoute

@Serializable
object FotoKtpRoute

@Serializable
object HasilKtpRoute

@Serializable
object LokasiRoute

@Serializable
object PilihAlamatRoute

@Serializable
object KodeVerifikasiRoute

@Serializable
object BuatPasswordRoute

@Serializable
data class HomeRoute(val role: HomeRole)
