package com.sempatapps.navigation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.sempatapps.R
import com.sempatapps.presentation.penerima.AbuTeks
import com.sempatapps.presentation.penerima.AjukanBantuanScreen
import com.sempatapps.presentation.penerima.BerandaScreen
import com.sempatapps.presentation.penerima.ChatbotScreen
import com.sempatapps.presentation.penerima.DaftarPanganScreen
import com.sempatapps.presentation.penerima.DetailPanganScreen
import com.sempatapps.presentation.penerima.Latar
import com.sempatapps.presentation.penerima.Lokasi
import com.sempatapps.presentation.penerima.NotificationHelper
import com.sempatapps.presentation.penerima.NotifikasiScreen
import com.sempatapps.presentation.penerima.PenerimaViewModel
import com.sempatapps.presentation.penerima.PilihAlamatScreen
import com.sempatapps.presentation.penerima.PilihLokasiScreen
import com.sempatapps.presentation.penerima.ProfilScreen
import com.sempatapps.presentation.penerima.TealGelap
import com.sempatapps.presentation.penerima.TealMuda
import com.sempatapps.presentation.penerima.TombolTanyaYuk
import com.sempatapps.presentation.penerima.lokasiContoh
import com.sempatapps.presentation.penerima.panganContoh
import com.sempatapps.presentation.penerima.pesanAwalChat
import com.sempatapps.presentation.penerima.userContoh
import kotlinx.serialization.Serializable

// ---- Rute layar penerima (bagian 4: cari & minta bantuan) ----
// Tiga tab di bottom bar:
@Serializable internal object BerandaPenerimaRoute
@Serializable internal object PanganPenerimaRoute
@Serializable internal object ProfilPenerimaRoute

// Layar di atas tab (bottom bar disembunyikan):
/** Pangan contoh belum punya id, jadi dicari lewat namanya. Nanti diganti id donasi dari API. */
@Serializable internal data class DetailPanganRoute(val nama: String)
@Serializable internal object AjukanBantuanRoute
@Serializable internal object NotifikasiPenerimaRoute
@Serializable internal object PilihLokasiPenerimaRoute
@Serializable internal object PilihAlamatPenerimaRoute
@Serializable internal object ChatbotRoute

/**
 * Semua layar penerima, dengan bottom bar Beranda / Pangan / Profil.
 * Dipanggil dari [AppNavGraph] waktu yang masuk adalah penerima.
 *
 * @param onKeluar dipanggil setelah sesi dihapus (tombol Keluar di Profil).
 */
@Composable
fun PenerimaApp(onKeluar: () -> Unit, viewModel: PenerimaViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val nama by viewModel.nama.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Dipakai bersama beberapa layar, jadi disimpan di sini (belum dari server).
    val daftarLokasi = remember { mutableStateListOf(*lokasiContoh.toTypedArray()) }
    var lokasiTerpilih by rememberSaveable { mutableIntStateOf(0) }
    val alamatTerpilih = daftarLokasi.getOrNull(lokasiTerpilih)?.alamat ?: userContoh.alamat
    val pesanChat = remember { pesanAwalChat() }

    val izinNotifikasi = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { boleh ->
        if (boleh) tesNotifikasi(context)
    }

    val entry by navController.currentBackStackEntryAsState()
    val tujuan = entry?.destination
    val tabAktif = when {
        tujuan == null -> null
        tujuan.hasRoute<BerandaPenerimaRoute>() -> Tab.BERANDA
        tujuan.hasRoute<PanganPenerimaRoute>() -> Tab.PANGAN
        tujuan.hasRoute<ProfilPenerimaRoute>() -> Tab.PROFIL
        else -> null
    }

    Scaffold(
        containerColor = Latar,
        bottomBar = {
            if (tabAktif != null) {
                BottomBarPenerima(aktif = tabAktif, onPilih = { navController.bukaTab(it) })
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = BerandaPenerimaRoute,
            modifier = Modifier.padding(padding),
        ) {
            composable<BerandaPenerimaRoute> {
                Box(Modifier.fillMaxSize()) {
                    BerandaScreen(
                        namaPengguna = nama.ifBlank { "Pengguna" },
                        lokasi = alamatTerpilih,
                        onNotifikasi = { navController.navigate(NotifikasiPenerimaRoute) },
                        onLokasi = { navController.navigate(PilihLokasiPenerimaRoute) },
                        onPanganClick = { navController.navigate(DetailPanganRoute(it.nama)) },
                    )
                    TombolTanyaYuk(
                        onClick = { navController.navigate(ChatbotRoute) },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp),
                    )
                }
            }
            composable<PanganPenerimaRoute> {
                DaftarPanganScreen(
                    onMintaBantuan = { navController.navigate(AjukanBantuanRoute) },
                    onPanganClick = { navController.navigate(DetailPanganRoute(it.nama)) },
                )
            }
            composable<ProfilPenerimaRoute> {
                ProfilScreen(
                    user = userContoh.copy(nama = nama.ifBlank { userContoh.nama }),
                    alamatUtama = alamatTerpilih,
                    onUbahAlamat = { navController.navigate(PilihLokasiPenerimaRoute) },
                    onKeluar = { viewModel.keluar(onKeluar) },
                )
            }

            composable<DetailPanganRoute> { e ->
                val nama = e.toRoute<DetailPanganRoute>().nama
                val pangan = panganContoh.firstOrNull { it.nama == nama }
                if (pangan == null) {
                    // Data contoh berubah dan nama tidak ditemukan lagi: kembali saja.
                    LaunchedEffect(Unit) { navController.popBackStack() }
                } else {
                    DetailPanganScreen(
                        pangan = pangan,
                        onKembali = { navController.popBackStack() },
                        onLihatStatus = { navController.lihatStatus() },
                    )
                }
            }
            composable<AjukanBantuanRoute> {
                AjukanBantuanScreen(
                    onKembali = { navController.popBackStack() },
                    onKeBeranda = { navController.bukaTab(Tab.BERANDA) },
                    onLihatStatus = { navController.lihatStatus() },
                )
            }
            composable<NotifikasiPenerimaRoute> {
                NotifikasiScreen(
                    onKembali = { navController.popBackStack() },
                    onTesNotifikasi = {
                        val perluIzin = Build.VERSION.SDK_INT >= 33 &&
                            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
                            PackageManager.PERMISSION_GRANTED
                        if (perluIzin) izinNotifikasi.launch(Manifest.permission.POST_NOTIFICATIONS) else tesNotifikasi(context)
                    },
                )
            }
            composable<PilihLokasiPenerimaRoute> {
                PilihLokasiScreen(
                    daftar = daftarLokasi,
                    terpilih = lokasiTerpilih,
                    onPilih = { lokasiTerpilih = it },
                    onTambah = { navController.navigate(PilihAlamatPenerimaRoute) },
                    onKembali = { navController.popBackStack() },
                )
            }
            composable<PilihAlamatPenerimaRoute> {
                PilihAlamatScreen(
                    onKembali = { navController.popBackStack() },
                    onKonfirmasi = { alamat ->
                        daftarLokasi += Lokasi(nama = "Alamat ${daftarLokasi.size + 1}", alamat = alamat)
                        lokasiTerpilih = daftarLokasi.lastIndex
                        navController.popBackStack()
                    },
                )
            }
            composable<ChatbotRoute> {
                ChatbotScreen(onKembali = { navController.popBackStack() }, pesan = pesanChat)
            }
        }
    }
}

private enum class Tab(val ikon: Int, val label: String, val rute: Any) {
    BERANDA(R.drawable.ic_home, "Beranda", BerandaPenerimaRoute),
    PANGAN(R.drawable.ic_food, "Pangan", PanganPenerimaRoute),
    PROFIL(R.drawable.ic_profile, "Profil", ProfilPenerimaRoute),
}

/** Pindah tab seperti bottom bar umumnya: tiap tab ingat posisinya, back dari tab mana pun kembali ke Beranda. */
private fun NavHostController.bukaTab(tab: Tab) {
    navigate(tab.rute) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/** Belum ada layar status pengajuan; sementara status dilihat lewat Notifikasi, dengan back ke Beranda. */
private fun NavHostController.lihatStatus() {
    navigate(NotifikasiPenerimaRoute) {
        popUpTo(BerandaPenerimaRoute)
    }
}

private fun tesNotifikasi(context: Context) {
    NotificationHelper.createChannel(context)
    NotificationHelper.show(context, "Pangan siap diambil", "Ini contoh notifikasi dari SEMPAT.")
}

@Composable
private fun BottomBarPenerima(aktif: Tab, onPilih: (Tab) -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .background(Latar)
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 6.dp),
    ) {
        Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(38.dp), color = Color.White, shadowElevation = 9.dp) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(70.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Tab.entries.forEach { tab ->
                    val dipilih = tab == aktif
                    Column(
                        Modifier
                            .widthIn(min = 58.dp)
                            .clickable { onPilih(tab) },
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            Modifier
                                .size(if (dipilih) 44.dp else 32.dp)
                                .background(if (dipilih) TealMuda else Color.Transparent, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Image(
                                painterResource(tab.ikon),
                                contentDescription = tab.label,
                                modifier = Modifier.size(if (dipilih) 29.dp else 25.dp),
                                colorFilter = ColorFilter.tint(if (dipilih) TealGelap else AbuTeks),
                            )
                        }
                        if (!dipilih) Text(tab.label, color = AbuTeks, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
