package com.example.sempat

import com.example.sempat.ui.theme.Teal
import com.example.sempat.ui.theme.Orange
import com.example.sempat.ui.theme.Latar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog


// Bentuk data pengguna (nanti disamakan dengan model data tim)
data class User(
    val nama: String,
    val noHp: String,
    val kota: String,
    val peran: String,
    val alamat: String
)

// Akun contoh (karangan)
val userContoh = User(
    nama = "Budi Santoso",
    noHp = "0812-3456-7890",
    kota = "Medan, Sumatera Utara",
    peran = "Penerima",
    alamat = "Jalan Setia Budi, Medan Sunggal, Sumatera Utara"
)

@Composable
fun ProfilScreen(
    user: User = userContoh,
    alamatUtama: String = user.alamat,
    onUbahAlamat: () -> Unit = {},
    onKeluar: () -> Unit = {}
) {
    var tampilDialogKeluar by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Latar)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Profil", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(20.dp))

        // Foto profil bulat
        Image(
            painter = painterResource(R.drawable.avatar_penerima),
            contentDescription = "Foto profil",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(110.dp)
                .clip(CircleShape)
                .border(3.dp, Teal, CircleShape)
        )

        Spacer(Modifier.height(12.dp))
        Text(user.nama, color = Teal, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(user.peran, color = Orange, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.LocationOn, null, tint = Teal, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(user.kota, fontSize = 12.sp, color = Color.Gray)
        }

        // Informasi pengguna (tanpa kata sandi, karena masuk lewat OTP)
        Spacer(Modifier.height(24.dp))
        JudulBagian("Informasi Pengguna")
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            BarisInfo(Icons.Default.Person, user.nama)
            HorizontalDivider(color = Color(0xFFEEEEEE))
            BarisInfo(Icons.Default.Phone, user.noHp)
        }

        // Alamat saya (klik untuk membuka Pilih Lokasi)
        Spacer(Modifier.height(24.dp))
        JudulBagian("Alamat Saya")
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Teal),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = onUbahAlamat)
        ) {
            // Placeholder peta (nanti diganti peta sungguhan)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Teal.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Orange)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("Alamat Saya", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Icon(Icons.Default.LocationOn, null, tint = Teal, modifier = Modifier.size(32.dp))
                    Text("Peta menyusul", fontSize = 11.sp, color = Color.Gray)
                }
            }
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, null, tint = Teal, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(alamatUtama, fontSize = 13.sp, modifier = Modifier.weight(1f))
                Text("Ubah", color = Teal, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            }
        }

        Spacer(Modifier.height(24.dp))
        OutlinedButton(
            onClick = { tampilDialogKeluar = true },
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, Teal)
        ) {
            Text("Keluar dari Akun", color = Teal)
        }

        // Ruang supaya tidak tertutup tombol Tanya Yuk
        Spacer(Modifier.height(96.dp))
    }

    if (tampilDialogKeluar) {
        DialogKeluar(
            onBatal = { tampilDialogKeluar = false },
            onYa = {
                tampilDialogKeluar = false
                onKeluar()
            }
        )
    }
}

@Composable
private fun JudulBagian(teks: String) {
    Text(
        teks,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    )
}

@Composable
private fun BarisInfo(ikon: ImageVector, teks: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Teal.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(ikon, null, tint = Teal, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Text(teks, fontSize = 14.sp)
    }
}

@Composable
private fun DialogKeluar(onBatal: () -> Unit, onYa: () -> Unit) {
    Dialog(onDismissRequest = onBatal) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(Aset.ilustrasiKeluarAkun),
                    contentDescription = null,
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                )
                Spacer(Modifier.height(12.dp))
                Text("Keluar dari Akun?", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Kamu yakin ingin keluar dari akun SEMPAT?",
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = onBatal,
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, Teal)
                    ) { Text("Batal", color = Teal) }
                    Button(
                        onClick = onYa,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Teal)
                    ) { Text("Ya, Keluar") }
                }
            }
        }
    }
}
