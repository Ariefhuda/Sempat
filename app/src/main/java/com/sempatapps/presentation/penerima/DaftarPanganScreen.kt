package com.example.sempat

import com.example.sempat.ui.theme.Teal
import com.example.sempat.ui.theme.TealGelap
import com.example.sempat.ui.theme.Orange
import com.example.sempat.ui.theme.Latar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


private data class KategoriUi(val nama: String, val gambar: Int)

private val daftarKategori = listOf(
    KategoriUi("Kebutuhan Pokok", R.drawable.kategori_kebutuhan_pokok),
    KategoriUi("Sayur & Buah", R.drawable.kategori_sayur_buah),
    KategoriUi("Makanan Siap Saji", R.drawable.kategori_makanan_siap_saji),
    KategoriUi("Roti & Kue", R.drawable.kategori_roti_kue),
    KategoriUi("Lainnya", R.drawable.kategori_lainnya)
)

@Composable
fun DaftarPanganScreen(
    daftar: List<Pangan> = panganContoh,
    onMintaBantuan: () -> Unit = {},
    onPanganClick: (Pangan) -> Unit = {}
) {
    var cari by remember { mutableStateOf("") }
    var kategoriTerpilih by remember { mutableStateOf<String?>(null) }

    // Saring berdasarkan kategori + kata pencarian, lalu urutkan dari yang terdekat
    val hasil = daftar
        .filter { kategoriTerpilih == null || it.kategori == kategoriTerpilih }
        .filter {
            it.nama.contains(cari, ignoreCase = true) ||
                    it.donatur.contains(cari, ignoreCase = true)
        }
        .sortedBy { it.jarakKm }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Latar),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Judul
        item {
            Column {
                Text("Pangan", color = Teal, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
                Text("Temukan pangan yang tersedia di sekitarmu", fontSize = 12.sp)
            }
        }

        // Kolom cari
        item {
            OutlinedTextField(
                value = cari,
                onValueChange = { cari = it },
                placeholder = { Text("Cari Pangan", fontSize = 13.sp) },
                trailingIcon = { Icon(Icons.Default.Search, null, tint = Teal) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Teal,
                    unfocusedBorderColor = Teal,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Banner Minta Bantuan
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.horizontalGradient(listOf(Teal, Color(0xFF3DDBC5))))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Butuh Bantuan Pangan?", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(
                        "Dapatkan pangan dari donasi yang tersedia",
                        color = Color.White,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = onMintaBantuan,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp)
                    ) {
                        Text("Minta Bantuan", color = TealGelap, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Kategori
        item {
            Column {
                Text("Berdasarkan Kategori", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(daftarKategori) { kat ->
                        val dipilih = kategoriTerpilih == kat.nama
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (dipilih) Teal else Color.White
                            ),
                            border = BorderStroke(1.dp, Teal),
                            modifier = Modifier
                                .width(88.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { kategoriTerpilih = if (dipilih) null else kat.nama }
                        ) {
                            Column(
                                Modifier
                                    .padding(8.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Image(
                                    painter = painterResource(kat.gambar),
                                    contentDescription = kat.nama,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.size(44.dp)
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    kat.nama,
                                    fontSize = 10.sp,
                                    fontWeight = if (dipilih) FontWeight.Bold else FontWeight.Medium,
                                    color = if (dipilih) Color.White else Color.Unspecified,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        // Daftar pangan
        if (hasil.isEmpty()) {
            item {
                Text(
                    "Pangan tidak ditemukan",
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp)
                )
            }
        } else {
            items(hasil) { pangan ->
                KartuPanganDaftar(pangan, onClick = { onPanganClick(pangan) })
            }
        }

        // Ruang di bawah supaya tidak tertutup tombol Tanya Yuk
        item { Spacer(Modifier.height(96.dp)) }
    }
}

@Composable
private fun KartuPanganDaftar(pangan: Pangan, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Teal.copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Row(Modifier.padding(10.dp)) {
            // Gambar + badge jarak
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Teal.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(gambarKategori(pangan.kategori)),
                    contentDescription = pangan.nama,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(62.dp)
                )
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .clip(RoundedCornerShape(bottomEnd = 10.dp))
                        .background(Orange)
                        .padding(horizontal = 6.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.LocationOn, null, tint = Color.White, modifier = Modifier.size(11.dp))
                    Text("${pangan.jarakKm} Km", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(pangan.kategori, fontSize = 10.sp, color = Color.Gray)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        pangan.nama,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text("${pangan.beratGram}g", fontSize = 11.sp, color = Teal)
                }
                Spacer(Modifier.height(4.dp))
                Text("Donatur", fontSize = 9.sp, color = Color.Gray)
                Text(pangan.donatur, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Gratis", color = TealGelap, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                    Spacer(Modifier.weight(1f))
                    Text(
                        teksSisaWaktu(pangan.sisaMenit),
                        color = Orange,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
