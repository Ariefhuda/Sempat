package com.example.sempat

import com.example.sempat.ui.theme.Teal
import com.example.sempat.ui.theme.TealGelap
import com.example.sempat.ui.theme.Latar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


// tipe: pangan_siap / bantuan_diterima / waktu_hampir_habis / umum
data class Notifikasi(
    val id: String,
    val judul: String,
    val isi: String,
    val waktu: String,
    val kelompok: String = "Hari ini",
    val tipe: String = "umum",
    val sudahDibaca: Boolean = false
)

val notifikasiContoh = listOf(
    Notifikasi(
        id = "1",
        judul = "Pangan Siap Diambil",
        isi = "Roti Enak dari Zahran Bakery sudah siap kamu ambil.",
        waktu = "5 menit lalu",
        kelompok = "Hari ini",
        tipe = "pangan_siap"
    ),
    Notifikasi(
        id = "2",
        judul = "Bantuan Pangan Diterima",
        isi = "Permintaan bantuanmu telah disetujui. Pangan siap diambil.",
        waktu = "1 jam lalu",
        kelompok = "Hari ini",
        tipe = "bantuan_diterima"
    ),
    Notifikasi(
        id = "3",
        judul = "Waktu Pengambilan Hampir Habis",
        isi = "Sisa 30 menit untuk mengambil nasi kotak. Segera datang ke lokasi donatur.",
        waktu = "Kemarin",
        kelompok = "Kemarin",
        tipe = "waktu_hampir_habis"
    )
)

fun gambarNotifikasi(tipe: String): Int = when (tipe) {
    "pangan_siap" -> R.drawable.notif_pangan_siap
    "bantuan_diterima" -> R.drawable.notif_bantuan_diterima
    else -> R.drawable.notif_status_diperbarui
}

@Composable
fun NotifikasiScreen(
    daftarAwal: List<Notifikasi> = notifikasiContoh,
    onKembali: () -> Unit = {},
    onTesNotifikasi: () -> Unit = {}
) {
    // State: supaya kartu bisa berubah jadi "sudah dibaca"
    val daftar = remember { daftarAwal.toMutableStateList() }

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
            IconButton(onClick = onKembali) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Teal)
            }
            Text(
                "Notifikasi",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            // Tombol tes sementara, hapus kalau notifikasi sudah terhubung ke kejadian nyata
            TextButton(onClick = onTesNotifikasi) { Text("Tes", color = Teal) }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            daftar.groupBy { it.kelompok }.forEach { (kelompok, isi) ->
                item(key = "judul-$kelompok") {
                    Text(kelompok, fontSize = 13.sp, color = Color.Gray)
                }
                items(isi, key = { it.id }) { notif ->
                    NotifikasiItem(
                        notif = notif,
                        onClick = {
                            val i = daftar.indexOfFirst { it.id == notif.id }
                            if (i >= 0) daftar[i] = daftar[i].copy(sudahDibaca = true)
                        }
                    )
                }
            }
            // Ruang supaya tidak tertutup tombol Tanya Yuk
            item(key = "spasi") { Spacer(Modifier.height(96.dp)) }
        }
    }
}

@Composable
fun NotifikasiItem(notif: Notifikasi, onClick: () -> Unit = {}) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notif.sudahDibaca) Color.White else Teal.copy(alpha = 0.08f)
        ),
        border = BorderStroke(1.dp, Teal),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            Image(
                painter = painterResource(gambarNotifikasi(notif.tipe)),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(52.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    notif.judul,
                    color = TealGelap,
                    fontWeight = if (notif.sudahDibaca) FontWeight.SemiBold else FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(2.dp))
                Text(notif.isi, fontSize = 12.sp)
                Spacer(Modifier.height(6.dp))
                Text(notif.waktu, fontSize = 11.sp, color = Color.Gray)
            }
        }
    }
}
