package com.example.sempat

import com.example.sempat.ui.theme.Teal
import com.example.sempat.ui.theme.TealGelap
import com.example.sempat.ui.theme.Orange
import com.example.sempat.ui.theme.Latar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
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
import androidx.compose.ui.window.Dialog


@Composable
fun DetailPanganScreen(
    pangan: Pangan,
    onKembali: () -> Unit = {},
    onLihatStatus: () -> Unit = {}
) {
    var dialogKonfirmasi by remember { mutableStateOf(false) }
    var dialogBerhasil by remember { mutableStateOf(false) }
    val sudahHabis = pangan.sisaMenit <= 0

    Column(Modifier.fillMaxSize().background(Latar)) {

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // Gambar besar, tombol kembali, badge jarak, nama
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Teal.copy(alpha = 0.25f), Teal.copy(alpha = 0.5f))
                        )
                    )
            ) {
                Image(
                    painter = painterResource(gambarKategori(pangan.kategori)),
                    contentDescription = pangan.nama,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(150.dp)
                )

                IconButton(
                    onClick = onKembali,
                    modifier = Modifier
                        .padding(12.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .align(Alignment.TopStart)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Teal)
                }

                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopEnd)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Orange)
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.LocationOn, null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Text("${pangan.jarakKm} Km", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.35f))
                        .padding(16.dp)
                ) {
                    Text(pangan.kategori, color = Color.White, fontSize = 12.sp)
                    Text(pangan.nama, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
                }
            }

            Column(Modifier.padding(16.dp)) {

                // Batas pengambilan
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Orange.copy(alpha = 0.12f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text("Batas Pengambilan", fontSize = 11.sp, color = Color.Gray)
                        Text(
                            if (sudahHabis) "Waktu pengambilan habis" else teksSisaWaktu(pangan.sisaMenit),
                            color = Orange,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }

                // Informasi pangan
                Spacer(Modifier.height(20.dp))
                Judul("Informasi Pangan")
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        BarisInfo("Harga", "Gratis", tebal = true)
                        BarisInfo("Berat", "${pangan.beratGram} gram")
                        BarisInfo("Status Konsumsi", pangan.statusKonsumsi, pil = true)
                        BarisInfo("Masa Tahan", pangan.masaTahan)
                        BarisInfo("Dipublikasikan", pangan.tanggalPublikasi)
                    }
                }

                // Donatur
                Spacer(Modifier.height(20.dp))
                Judul("Donatur")
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(R.drawable.avatar_donatur),
                            contentDescription = pangan.donatur,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(pangan.donatur, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Donatur", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                }

                // Lokasi pengambilan (self pick-up)
                Spacer(Modifier.height(20.dp))
                Judul("Lokasi Pengambilan")
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Teal),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(Icons.Default.LocationOn, null, tint = Teal, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(pangan.alamatPengambilan, fontSize = 13.sp)
                                Spacer(Modifier.height(4.dp))
                                Text("Pangan diambil sendiri di lokasi donatur.", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }

        // Tombol di bawah
        Surface(shadowElevation = 8.dp, color = Color.White) {
            Button(
                onClick = { dialogKonfirmasi = true },
                enabled = !sudahHabis,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Teal),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(48.dp)
            ) {
                Text(
                    if (sudahHabis) "Waktu Pengambilan Habis" else "Ajukan Pengambilan",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

    if (dialogKonfirmasi) {
        PopupDuaTombol(
            gambar = Aset.ilustrasiBatalkan,
            judul = "Ajukan Pengambilan?",
            isi = "Kamu akan mengambil ${pangan.nama} langsung di lokasi donatur sebelum batas waktu habis.",
            teksKiri = "Batal",
            onKiri = { dialogKonfirmasi = false },
            teksKanan = "Ya, Ajukan",
            onKanan = { dialogKonfirmasi = false; dialogBerhasil = true },
            onDismiss = { dialogKonfirmasi = false }
        )
    }

    if (dialogBerhasil) {
        PopupDuaTombol(
            gambar = Aset.ilustrasiPengajuanBerhasil,
            judul = "Pengajuan Berhasil Dikirim!",
            isi = "Tunggu persetujuan donatur. Setelah disetujui, datang ke lokasi untuk mengambil pangan.",
            teksKiri = "Kembali",
            onKiri = { dialogBerhasil = false; onKembali() },
            teksKanan = "Lihat Status",
            onKanan = { dialogBerhasil = false; onLihatStatus() }
        )
    }
}

@Composable
private fun Judul(teks: String) {
    Text(teks, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun BarisInfo(label: String, nilai: String, tebal: Boolean = false, pil: Boolean = false) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 13.sp, modifier = Modifier.weight(1f))
        if (pil) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Teal.copy(alpha = 0.15f))
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
                Text(nilai, color = TealGelap, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        } else {
            Text(
                nilai,
                fontSize = 13.sp,
                fontWeight = if (tebal) FontWeight.ExtraBold else FontWeight.SemiBold,
                color = if (tebal) TealGelap else Color.Unspecified
            )
        }
    }
}

@Composable
private fun PopupDuaTombol(
    gambar: Int,
    judul: String,
    isi: String,
    teksKiri: String,
    onKiri: () -> Unit,
    teksKanan: String,
    onKanan: () -> Unit,
    onDismiss: () -> Unit = {}
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(gambar),
                    contentDescription = null,
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                )
                Spacer(Modifier.height(12.dp))
                Text(judul, fontWeight = FontWeight.Bold, fontSize = 17.sp, textAlign = TextAlign.Center)
                Spacer(Modifier.height(8.dp))
                Text(isi, fontSize = 13.sp, textAlign = TextAlign.Center)
                Spacer(Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = onKiri,
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, Teal)
                    ) { Text(teksKiri, color = Teal) }
                    Button(
                        onClick = onKanan,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Teal)
                    ) { Text(teksKanan) }
                }
            }
        }
    }
}
