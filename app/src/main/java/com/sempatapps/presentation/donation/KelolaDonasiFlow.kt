package com.sempatapps.presentation.donation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview

private enum class LayarKelolaDonasi {
    PANGAN_SAYA,
    DETAIL_DONASI,
    CHAT_PENERIMA,
    TELEPON_PENERIMA,
    PROFIL_DONATUR,
    PILIH_ALAMAT,
}

/**
 * Pengatur alur lengkap Tugas Nomor 3: "Kelola Donasi (Donatur)".
 * Menghubungkan semua 18 frame (13 layar + 5 pop-up) di dalam satu alur interaktif:
 * - Pangan Saya (Tab Aktif, Proses, Selesai)
 * - Detail Donasi Pangan (Menunggu Konfirmasi, Disiapkan, Siap Diambil, Selesai + 3 Pop-up)
 * - Pesan Donatur (Chat Aktif & Sesi Berakhir)
 * - Telepon Penerima
 * - Profil Donatur (+ 2 Pop-up) & Pilih Alamat
 */
@Composable
fun KelolaDonasiFlow(
    mulaiDariProfil: Boolean = false,
    onKeluarAkun: () -> Unit = {},
) {
    val daftarDonasi = remember { mutableStateListOf(*sampleDonasiDonaturList.toTypedArray()) }
    var layarAktif by rememberSaveable {
        mutableStateOf(if (mulaiDariProfil) LayarKelolaDonasi.PROFIL_DONATUR else LayarKelolaDonasi.PANGAN_SAYA)
    }
    var selectedItemId by rememberSaveable { mutableStateOf(sampleDonasiDonaturList.first().id) }
    var alamatUsaha by rememberSaveable { mutableStateOf("Jalan Jamin Ginting Medan, Sumatera Utara") }

    val selectedItem = daftarDonasi.firstOrNull { it.id == selectedItemId } ?: sampleDonasiDonaturList.first()

    when (layarAktif) {
        LayarKelolaDonasi.PANGAN_SAYA -> {
            PanganSayaScreen(
                items = daftarDonasi,
                onItemClick = { item ->
                    selectedItemId = item.id
                    layarAktif = LayarKelolaDonasi.DETAIL_DONASI
                },
            )
        }

        LayarKelolaDonasi.DETAIL_DONASI -> {
            DetailDonasiDonaturScreen(
                item = selectedItem,
                onBackClick = { layarAktif = LayarKelolaDonasi.PANGAN_SAYA },
                onLihatPenerimaClick = {
                    // Simulasi memilih penerima -> pindah ke tab Proses (Disiapkan)
                    val index = daftarDonasi.indexOfFirst { it.id == selectedItem.id }
                    if (index != -1) {
                        daftarDonasi[index] = daftarDonasi[index].copy(
                            tab = PanganTab.PROSES,
                            tahap = TahapBantuan.DISIAPKAN,
                            namaPenerima = "Panti Asuhan\nFasilkom",
                        )
                    }
                },
                onPesanClick = {
                    layarAktif = LayarKelolaDonasi.CHAT_PENERIMA
                },
                onTelpClick = {
                    layarAktif = LayarKelolaDonasi.TELEPON_PENERIMA
                },
                onStatusUpdated = { tahapBaru ->
                    val index = daftarDonasi.indexOfFirst { it.id == selectedItem.id }
                    if (index != -1) {
                        daftarDonasi[index] = daftarDonasi[index].copy(tahap = tahapBaru)
                    }
                },
                onDonasiCancelled = {
                    daftarDonasi.removeAll { it.id == selectedItem.id }
                    layarAktif = LayarKelolaDonasi.PANGAN_SAYA
                },
            )
        }

        LayarKelolaDonasi.CHAT_PENERIMA -> {
            PesanDonaturScreen(
                item = selectedItem,
                sesiBerakhir = selectedItem.tahap == TahapBantuan.SELESAI,
                onBackClick = { layarAktif = LayarKelolaDonasi.DETAIL_DONASI },
                onTelpClick = { layarAktif = LayarKelolaDonasi.TELEPON_PENERIMA },
                onRincianClick = { layarAktif = LayarKelolaDonasi.DETAIL_DONASI },
            )
        }

        LayarKelolaDonasi.TELEPON_PENERIMA -> {
            TeleponPenerimaScreen(
                namaPenerima = selectedItem.namaPenerima ?: "Panti Asuhan\nFasilkom",
                onEndCallClick = { layarAktif = LayarKelolaDonasi.CHAT_PENERIMA },
            )
        }

        LayarKelolaDonasi.PROFIL_DONATUR -> {
            ProfilDonaturScreen(
                alamatUsaha = alamatUsaha,
                onBukaPilihAlamat = { layarAktif = LayarKelolaDonasi.PILIH_ALAMAT },
                onKeluarAkun = onKeluarAkun,
            )
        }

        LayarKelolaDonasi.PILIH_ALAMAT -> {
            PilihAlamatDonaturScreen(
                initialQuery = alamatUsaha,
                onBackClick = { layarAktif = LayarKelolaDonasi.PROFIL_DONATUR },
                onKonfirmasiAlamat = { alamatBaru ->
                    alamatUsaha = alamatBaru
                    layarAktif = LayarKelolaDonasi.PROFIL_DONATUR
                },
            )
        }
    }
}

@Preview(name = "Alur Lengkap Kelola Donasi", showBackground = true, widthDp = 402, heightDp = 874)
@Composable
private fun KelolaDonasiFlowPreview() {
    KelolaDonasiFlow()
}
