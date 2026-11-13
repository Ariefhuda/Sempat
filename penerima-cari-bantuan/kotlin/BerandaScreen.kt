package com.example.sempat

import com.example.sempat.ui.theme.Teal
import com.example.sempat.ui.theme.TealGelap
import com.example.sempat.ui.theme.Orange
import com.example.sempat.ui.theme.Latar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


// Bentuk data pangan (nanti disamakan dengan model data tim / Orang 2).
// Foto makanan nanti datang dari API; sementara dipakai ikon kategori.
data class Pangan(
    val nama: String,
    val kategori: String,
    val donatur: String,
    val beratGram: Int,
    val jarakKm: Double,
    val sisaMenit: Int,
    val statusKonsumsi: String = "Layak",
    val masaTahan: String = "2 Hari",
    val tanggalPublikasi: String = "13 Agustus 2026",
    val alamatPengambilan: String = "Jl. Setia Budi, Medan Sunggal, Sumatera Utara"
)

val panganContoh = listOf(
    Pangan("Roti Enak", "Roti & Kue", "Zahran Bakery", 250, 1.5, 150),
    Pangan("Roti Telur", "Roti & Kue", "Zahran Bakery", 250, 1.2, 45),
    Pangan(
        "Nasi Kotak", "Makanan Siap Saji", "Warung Bu Sri", 400, 2.1, 90,
        masaTahan = "1 Hari",
        alamatPengambilan = "Jl. Gatot Subroto No. 12, Medan"
    ),
    Pangan(
        "Sayur Segar", "Sayur & Buah", "Toko Hijau", 500, 3.0, 300,
        alamatPengambilan = "Jl. Sunggal No. 8, Medan"
    ),
    Pangan(
        "Beras 1 Kg", "Kebutuhan Pokok", "Toko Makmur", 1000, 2.6, 600,
        masaTahan = "30 Hari",
        alamatPengambilan = "Jl. Setia Budi No. 40, Medan"
    ),
    Pangan("Donat Kentang", "Roti & Kue", "Dapur Mama Rina", 300, 0.8, 200),
    Pangan("Buah Potong", "Sayur & Buah", "Toko Hijau", 350, 1.9, 120),
    Pangan(
        "Paket Camilan", "Lainnya", "Kedai Sinar", 450, 3.4, 400,
        masaTahan = "7 Hari",
        alamatPengambilan = "Jl. Gajah Mada No. 21, Medan"
    )
)

// Ikon kategori, juga dipakai sebagai gambar pangan sementara
fun gambarKategori(kategori: String): Int = when (kategori) {
    "Kebutuhan Pokok" -> R.drawable.kategori_kebutuhan_pokok
    "Sayur & Buah" -> R.drawable.kategori_sayur_buah
    "Makanan Siap Saji" -> R.drawable.kategori_makanan_siap_saji
    "Roti & Kue" -> R.drawable.kategori_roti_kue
    else -> R.drawable.kategori_lainnya
}

fun teksSisaWaktu(menit: Int): String {
    if (menit <= 0) return "Waktu habis"
    val jam = menit / 60
    val sisa = menit % 60
    return when {
        jam > 0 && sisa > 0 -> "Sisa $jam jam $sisa menit"
        jam > 0 -> "Sisa $jam jam"
        else -> "Sisa $sisa menit"
    }
}

@Composable
fun BerandaScreen(
    namaPengguna: String = "Pengguna",
    lokasi: String = "Jl. Setia Budi, Medan Sunggal, Sumatera Utara",
    daftar: List<Pangan> = panganContoh,
    onNotifikasi: () -> Unit = {},
    onLokasi: () -> Unit = {},
    onPanganClick: (Pangan) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Latar)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        // Header: logo + lonceng
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.logo_sempat),
                contentDescription = "SemPat",
                contentScale = ContentScale.Fit,
                alignment = Alignment.CenterStart,
                modifier = Modifier
                    .height(34.dp)
                    .weight(1f)
            )
            IconButton(
                onClick = onNotifikasi,
                modifier = Modifier
                    .size(40.dp)
                    .border(1.dp, Teal, CircleShape)
            ) {
                Icon(Icons.Default.Notifications, contentDescription = "Notifikasi", tint = Teal)
            }
        }

        // Banner sapaan
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFF6FF3E2), Teal)))
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Column(Modifier.align(Alignment.CenterStart)) {
                Text("Halo", color = Color.White, fontSize = 14.sp)
                Text(namaPengguna, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(4.dp))
                Text("Mau cari pangan apa\nhari ini?", color = Color.White, fontSize = 13.sp)
            }
            Image(
                painter = painterResource(R.drawable.beranda),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
            )
        }

        Spacer(Modifier.height(12.dp))

        // Kartu lokasi
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Teal),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .clickable(onClick = onLokasi)
        ) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, null, tint = Teal)
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text("Lokasi Saya", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(lokasi, fontSize = 11.sp, color = Color.Gray)
                }
                Icon(Icons.Default.KeyboardArrowRight, null, tint = Teal)
            }
        }

        Spacer(Modifier.height(20.dp))
        Text("Pangan Terdekat", fontWeight = FontWeight.Bold, fontSize = 17.sp)
        Spacer(Modifier.height(10.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(daftar.sortedBy { it.jarakKm }) { pangan ->
                KartuPangan(pangan, onClick = { onPanganClick(pangan) })
            }
        }

        // Ruang di bawah supaya tidak tertutup tombol Tanya Yuk
        Spacer(Modifier.height(96.dp))
    }
}

@Composable
fun KartuPangan(pangan: Pangan, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Teal.copy(alpha = 0.4f)),
        modifier = Modifier
            .width(175.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Column {
            // Gambar + badge jarak
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Teal.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(gambarKategori(pangan.kategori)),
                    contentDescription = pangan.nama,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(76.dp)
                )
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .clip(RoundedCornerShape(bottomStart = 12.dp))
                        .background(Orange)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.LocationOn, null, tint = Color.White, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(2.dp))
                    Text("${pangan.jarakKm} Km", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Column(Modifier.padding(10.dp)) {
                Text(pangan.kategori, fontSize = 10.sp, color = Color.Gray)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        pangan.nama,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text("${pangan.beratGram}g", fontSize = 10.sp, color = Teal)
                }
                Spacer(Modifier.height(4.dp))
                Text("Donatur", fontSize = 9.sp, color = Color.Gray)
                Text(pangan.donatur, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                Text("Gratis", color = TealGelap, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                Text(teksSisaWaktu(pangan.sisaMenit), color = Orange, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
