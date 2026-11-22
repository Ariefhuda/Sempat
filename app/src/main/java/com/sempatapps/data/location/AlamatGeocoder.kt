package com.sempatapps.data.location

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

data class Koordinat(val latitude: Double, val longitude: Double)

/**
 * Cari koordinat dari nama tempat dan sebaliknya, pakai Geocoder bawaan Android (gratis,
 * butuh internet). Semua fungsi mengembalikan null kalau tidak ketemu atau gagal.
 */
@Singleton
class AlamatGeocoder @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val geocoder = Geocoder(context, Locale("id", "ID"))

    suspend fun cari(namaTempat: String): Koordinat? =
        hasil { geocodeNama(namaTempat) }?.let { Koordinat(it.latitude, it.longitude) }

    suspend fun alamatDari(latitude: Double, longitude: Double): String? =
        hasil { geocodeTitik(latitude, longitude) }?.getAddressLine(0)

    private suspend fun hasil(cari: suspend () -> List<Address>): Address? {
        if (!Geocoder.isPresent()) return null
        return try {
            cari().firstOrNull()
        } catch (_: IOException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    private suspend fun geocodeNama(nama: String): List<Address> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine { cont ->
                geocoder.getFromLocationName(nama, 1, object : Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: MutableList<Address>) = cont.resume(addresses)
                    override fun onError(errorMessage: String?) = cont.resume(emptyList())
                })
            }
        } else {
            @Suppress("DEPRECATION")
            withContext(Dispatchers.IO) { geocoder.getFromLocationName(nama, 1).orEmpty() }
        }

    private suspend fun geocodeTitik(latitude: Double, longitude: Double): List<Address> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine { cont ->
                geocoder.getFromLocation(latitude, longitude, 1, object : Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: MutableList<Address>) = cont.resume(addresses)
                    override fun onError(errorMessage: String?) = cont.resume(emptyList())
                })
            }
        } else {
            @Suppress("DEPRECATION")
            withContext(Dispatchers.IO) { geocoder.getFromLocation(latitude, longitude, 1).orEmpty() }
        }
}
