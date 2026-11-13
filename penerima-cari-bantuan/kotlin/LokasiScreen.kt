package com.example.sempat

import com.example.sempat.ui.theme.Teal
import com.example.sempat.ui.theme.Orange
import com.example.sempat.ui.theme.Latar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog


// Bentuk data lokasi (nanti disamakan dengan model data tim)
data class Lokasi(val nama: String, val alamat: String)

val lokasiContoh = listOf(
    Lokasi("Lokasi Saya", "Jalan Setia Budi, Medan Sunggal, Sumatera Utara"),
    Lokasi("Lokasi Saya", "Jalan Dr. T. Mansur Nomor 9, Padang Bulan, Kecamatan Medan Baru, Kota Medan")
)

// ---------- Layar Pilih Lokasi (daftar alamat) ----------
@Composable
fun PilihLokasiScreen(
    daftar: List<Lokasi>,
    terpilih: Int,
    onPilih: (Int) -> Unit,
    onTambah: () -> Unit,
    onKembali: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Latar)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onKembali,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Teal.copy(alpha = 0.12f))
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Teal)
            }
            Spacer(Modifier.width(12.dp))
            Text("Pilih Lokasi", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(daftar) { index, lokasi ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(if (index == terpilih) 2.dp else 1.dp, Teal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onPilih(index) }
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = index == terpilih,
                            onClick = { onPilih(index) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = Orange,
                                unselectedColor = Orange
                            )
                        )
                        Icon(Icons.Default.LocationOn, null, tint = Teal)
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(lokasi.nama, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(lokasi.alamat, fontSize = 11.sp, color = Color.DarkGray)
                        }
                    }
                }
            }
        }

        Button(
            onClick = onTambah,
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = Teal),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(48.dp)
        ) {
            Text("Tambah Lokasi", fontWeight = FontWeight.SemiBold)
        }
    }
}

// ---------- Layar Pilih Alamat (peta masih placeholder) ----------
@Composable
fun PilihAlamatScreen(
    onKembali: () -> Unit,
    onKonfirmasi: (String) -> Unit
) {
    var cari by remember { mutableStateOf("") }
    var detail by remember { mutableStateOf("") }

    Box(Modifier.fillMaxSize()) {

        // Placeholder peta
        Box(
            Modifier
                .fillMaxSize()
                .background(Color(0xFFDCEFEC)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Orange)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("Pilih Lokasi", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Icon(Icons.Default.LocationOn, null, tint = Teal, modifier = Modifier.size(48.dp))
                Text("Peta menyusul", color = Color.Gray, fontSize = 12.sp)
            }
        }

        // Atas: tombol kembali + kolom cari
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onKembali,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Teal)
            }
            Spacer(Modifier.width(8.dp))
            OutlinedTextField(
                value = cari,
                onValueChange = { cari = it },
                placeholder = { Text("Cari Lokasi", fontSize = 13.sp) },
                trailingIcon = { Icon(Icons.Default.Search, null, tint = Teal) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Teal,
                    unfocusedBorderColor = Teal,
                    focusedContainerColor = Color(0xFFEFEFEF),
                    unfocusedContainerColor = Color(0xFFEFEFEF)
                ),
                modifier = Modifier.weight(1f)
            )
        }

        // Bawah: detail lokasi + konfirmasi
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .imePadding()
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(Color.White)
                .padding(16.dp)
        ) {
            Text("Detail Lokasi", fontSize = 12.sp)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = detail,
                onValueChange = { detail = it },
                placeholder = { Text("Masukkan Detail Lokasi", fontSize = 13.sp) },
                minLines = 3,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Teal,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color(0xFFE3E3E3),
                    unfocusedContainerColor = Color(0xFFE3E3E3)
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    val alamat = if (detail.isBlank()) cari.trim() else "${cari.trim()} (${detail.trim()})"
                    onKonfirmasi(alamat)
                },
                enabled = cari.isNotBlank(),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Teal),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Konfirmasi", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ---------- Pop-up satu tombol (dipakai "Lokasi Berhasil Ditambah") ----------
@Composable
fun PopupSatuTombol(
    gambar: Int,
    judul: String,
    isi: String,
    teksTombol: String,
    onTombol: () -> Unit
) {
    Dialog(onDismissRequest = onTombol) {
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
                Button(
                    onClick = onTombol,
                    colors = ButtonDefaults.buttonColors(containerColor = Teal),
                    modifier = Modifier.fillMaxWidth()
                ) { Text(teksTombol) }
            }
        }
    }
}
