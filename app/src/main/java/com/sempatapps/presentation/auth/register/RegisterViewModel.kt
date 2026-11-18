package com.sempatapps.presentation.auth.register

import android.net.Uri
import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sempatapps.ai.ktp.FotoKtpLoader
import com.sempatapps.ai.ktp.KtpReader
import com.sempatapps.data.location.AlamatGeocoder
import com.sempatapps.data.location.Koordinat
import com.sempatapps.data.remote.ApiException
import com.sempatapps.data.repository.AuthRepository
import com.sempatapps.domain.model.DataDaftar
import com.sempatapps.domain.model.HomeRole
import com.sempatapps.domain.model.KtpData
import com.sempatapps.domain.model.KtpField
import com.sempatapps.domain.model.TitikLokasi
import com.sempatapps.domain.model.kolomKtpSalah
import com.sempatapps.domain.model.noHpValid
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class StatusScan { BELUM, MEMBACA, GAGAL }

data class RegisterUiState(
    val data: DataDaftar = DataDaftar(peran = HomeRole.DONOR),
    val statusScan: StatusScan = StatusScan.BELUM,
    /** true kalau pengguna pilih "Isi Data Sendiri" (KTP tidak terbaca). */
    val isiManual: Boolean = false,
    val setujuKetentuan: Boolean = true,
    val kode: String = "",
    val kodeTerisiOtomatis: Boolean = false,
    /** Waktu (SystemClock.elapsedRealtime) kapan tombol "Kirim Ulang" boleh dipakai lagi. */
    val bisaKirimUlangPada: Long = 0L,
    val tokenVerifikasi: String? = null,
    val sedangProses: Boolean = false,
    val pesanError: String? = null,
    val daftarBerhasil: Boolean = false,
)

/**
 * Dipakai bersama oleh semua layar daftar (di-scope ke RegisterGraph di AppNavGraph),
 * jadi isian tidak hilang waktu pindah langkah.
 */
@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val ktpReader: KtpReader,
    private val fotoLoader: FotoKtpLoader,
    private val authRepository: AuthRepository,
    private val geocoder: AlamatGeocoder,
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterUiState())
    val state = _state.asStateFlow()

    private val data get() = _state.value.data

    fun pilihPeran(peran: HomeRole) = updateData { it.copy(peran = peran) }

    // ---- Foto KTP ----

    fun uriFotoBaru(): Uri = fotoLoader.buatUriBaru()

    fun bacaKtp(uri: Uri, onTerbaca: () -> Unit) {
        _state.update { it.copy(statusScan = StatusScan.MEMBACA) }
        viewModelScope.launch {
            val hasil = try {
                ktpReader.read(fotoLoader.muat(uri))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Gagal baca KTP", e)
                null
            } finally {
                // Sesuai janji di layar: foto KTP tidak disimpan.
                fotoLoader.hapusSemua()
            }

            if (hasil == null || (hasil.nik.isBlank() && hasil.nama.isBlank())) {
                _state.update { it.copy(statusScan = StatusScan.GAGAL) }
            } else {
                _state.update { it.copy(statusScan = StatusScan.BELUM, isiManual = false, data = it.data.copy(ktp = hasil)) }
                onTerbaca()
            }
        }
    }

    fun fotoDibatalkan() {
        fotoLoader.hapusSemua()
        _state.update { it.copy(statusScan = if (it.statusScan == StatusScan.MEMBACA) StatusScan.BELUM else it.statusScan) }
    }

    fun isiDataSendiri() {
        _state.update {
            it.copy(
                statusScan = StatusScan.BELUM,
                isiManual = true,
                data = it.data.copy(ktp = KtpData(perluDicek = KtpField.entries.toSet())),
            )
        }
    }

    // ---- Hasil Baca KTP ----

    fun ubahNoHp(noHp: String) = updateData { it.copy(noHp = noHp.filter(Char::isDigit).take(13)) }

    fun ubahKtp(field: KtpField, nilai: String) = updateData {
        val ktp = it.ktp
        it.copy(
            ktp = when (field) {
                KtpField.NIK -> ktp.copy(nik = nilai.filter(Char::isDigit).take(16))
                KtpField.NAMA -> ktp.copy(nama = nilai.uppercase())
                KtpField.TANGGAL_LAHIR -> ktp.copy(tanggalLahir = nilai.take(10))
                KtpField.KABUPATEN_KOTA -> ktp.copy(kabupatenKota = nilai.uppercase())
            },
        )
    }

    /** Kolom yang isinya belum valid. Kosong = boleh lanjut. */
    fun kolomSalah(): Set<KtpField> = kolomKtpSalah(data.ktp)

    fun noHpValid(): Boolean = noHpValid(data.noHp)

    // ---- Lokasi ----

    fun pilihLokasi(titik: TitikLokasi) = updateData { it.copy(lokasi = titik) }

    /** Titik awal peta: lokasi yang sudah dipilih, kalau belum ada pakai kota dari KTP. */
    suspend fun titikAwalPeta(): Koordinat? =
        data.lokasi?.let { Koordinat(it.latitude, it.longitude) }
            ?: data.ktp.kabupatenKota.takeIf { it.isNotBlank() }?.let { geocoder.cari(it) }

    suspend fun cariTempat(nama: String): Koordinat? = geocoder.cari(nama)

    suspend fun alamatDari(latitude: Double, longitude: Double): String =
        geocoder.alamatDari(latitude, longitude).orEmpty()

    fun ubahPatokan(patokan: String) = updateData { it.copy(patokan = patokan.take(255)) }

    fun ubahSetuju(setuju: Boolean) = _state.update { it.copy(setujuKetentuan = setuju) }

    /** Tombol "Buat Akun": minta kode OTP, lalu lanjut ke layar Kode Verifikasi. */
    fun buatAkun(onKodeTerkirim: () -> Unit) = proses {
        kirimKode()
        onKodeTerkirim()
    }

    // ---- Kode Verifikasi ----

    fun ubahKode(kode: String) = _state.update { it.copy(kode = kode.filter(Char::isDigit).take(6), kodeTerisiOtomatis = false, pesanError = null) }

    fun kirimUlang(onTerkirim: () -> Unit) = proses {
        kirimKode()
        onTerkirim()
    }

    fun cekKode(onBenar: () -> Unit) = proses {
        val token = authRepository.cekOtp(data.noHp, _state.value.kode)
        _state.update { it.copy(tokenVerifikasi = token) }
        onBenar()
    }

    private suspend fun kirimKode() {
        val otp = authRepository.kirimOtp(data.noHp)
        _state.update {
            it.copy(
                kode = otp.kodeDev.orEmpty(),
                kodeTerisiOtomatis = otp.kodeDev != null,
                bisaKirimUlangPada = SystemClock.elapsedRealtime() + otp.kirimUlangDetik * 1000L,
            )
        }
    }

    // ---- Buat Password ----

    fun simpanPassword(password: String) = proses {
        val token = _state.value.tokenVerifikasi ?: throw ApiException("Verifikasi nomor HP dulu.")
        authRepository.daftar(token, data, password)
        _state.update { it.copy(daftarBerhasil = true) }
    }

    fun hapusError() = _state.update { it.copy(pesanError = null) }

    /** Jalankan aksi ke server: tampilkan loading, tangkap error jadi pesan di layar. */
    private fun proses(aksi: suspend () -> Unit) {
        if (_state.value.sedangProses) return
        _state.update { it.copy(sedangProses = true, pesanError = null) }
        viewModelScope.launch {
            try {
                aksi()
            } catch (e: ApiException) {
                _state.update { it.copy(pesanError = e.message) }
            } finally {
                _state.update { it.copy(sedangProses = false) }
            }
        }
    }

    private inline fun updateData(crossinline ubah: (DataDaftar) -> DataDaftar) =
        _state.update { it.copy(data = ubah(it.data)) }

    private companion object {
        const val TAG = "RegisterViewModel"
    }
}
