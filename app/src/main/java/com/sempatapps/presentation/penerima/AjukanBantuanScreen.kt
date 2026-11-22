package com.example.sempat

import com.example.sempat.ui.theme.Teal

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
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
import coil.compose.AsyncImage

private val AbuIsian = Color(0xFFE3E3E3)

// Disamakan dengan kolom represented_entity di database
private val opsiMewakili = listOf("Individu", "Yayasan", "Pedagang")

@Composable
fun AjukanBantuanScreen(
    onKembali: () -> Unit = {},
    onKeBeranda: () -> Unit = {},
    onLihatStatus: () -> Unit = {}
) {
    var nama by remember { mutableStateOf("") }
    var mewakili by remember { mutableStateOf(opsiMewakili.first()) }
    var alasan by remember { mutableStateOf("") }
    var fotoBukti by remember { mutableStateOf<Uri?>(null) }

    var menuTerbuka by remember { mutableStateOf(false) }
    var dialogBatal by remember { mutableStateOf(false) }
    var dialogBerhasil by remember { mutableStateOf(false) }

    // Tombol hanya aktif kalau semua terisi
    val formValid = nama.isNotBlank() && alasan.isNotBlank() && fotoBukti != null

    // Pemilih foto dari galeri (tidak perlu izin khusus)
    val pilihFoto = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) fotoBukti = uri }

    // Tombol Back HP memunculkan pop-up batal
    BackHandler(enabled = !dialogBerhasil) { dialogBatal = true }

    Column(Modifier.fillMaxSize()) {

        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { dialogBatal = true }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Teal)
            }
            Text("Ajukan Bantuan Pangan", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Text(
                "Ceritakan kebutuhanmu agar SEMPAT dapat membantu menyalurkan pangan yang sesuai.",
                fontSize = 12.sp
            )

            // Nama
            Label("Nama Kamu")
            OutlinedTextField(
                value = nama,
                onValueChange = { nama = it },
                placeholder = { Text("Masukkan nama", fontSize = 13.sp) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = warnaIsian(),
                modifier = Modifier.fillMaxWidth()
            )

            // Kamu mewakili (dropdown)
            Label("Kamu Mewakili")
            Box {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Teal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { menuTerbuka = true }
                ) {
                    Row(
                        Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(mewakili, modifier = Modifier.weight(1f), fontSize = 14.sp)
                        Icon(Icons.Default.KeyboardArrowDown, null, tint = Teal)
                    }
                }
                DropdownMenu(expanded = menuTerbuka, onDismissRequest = { menuTerbuka = false }) {
                    opsiMewakili.forEach { opsi ->
                        DropdownMenuItem(
                            text = { Text(opsi) },
                            onClick = { mewakili = opsi; menuTerbuka = false }
                        )
                    }
                }
            }

            // Alasan
            Label("Alasan Membutuhkan Bantuan")
            OutlinedTextField(
                value = alasan,
                onValueChange = { alasan = it },
                placeholder = { Text("Ceritakan secara singkat kebutuhan pangan kamu...", fontSize = 13.sp) },
                shape = RoundedCornerShape(12.dp),
                colors = warnaIsian(),
                minLines = 4,
                modifier = Modifier.fillMaxWidth()
            )

            // Bukti kebutuhan
            Label("Bukti Kebutuhan")
            Text("Unggah bukti yang menunjukkan kebutuhan bantuan pangan", fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, Teal, RoundedCornerShape(20.dp))
                    .clickable {
                        pilihFoto.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                if (fotoBukti == null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Teal),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Add, null, tint = Color.White)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("Unggah Bukti", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                } else {
                    AsyncImage(
                        model = fotoBukti,
                        contentDescription = "Foto bukti",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            if (fotoBukti != null) {
                Text("Ketuk foto untuk menggantinya", fontSize = 11.sp, color = Color.Gray)
            }

            // Contoh bukti
            Label("Contoh Bukti")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ContohBukti("Kondisi tempat tinggal", Modifier.weight(1f))
                ContohBukti("Foto lingkungan", Modifier.weight(1f))
                ContohBukti("Surat keterangan", Modifier.weight(1f))
            }
            Spacer(Modifier.height(16.dp))
        }

        // Tombol kirim
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Button(
                onClick = { dialogBerhasil = true },
                enabled = formValid,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Teal),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Ajukan Bantuan Pangan", fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "Data dan bukti akan digunakan untuk proses verifikasi bantuan.",
                fontSize = 10.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }

    if (dialogBatal) {
        DialogDuaTombol(
            gambar = Aset.ilustrasiBatalkan,
            judul = "Batalkan Pengajuan?",
            isi = "Kamu yakin ingin membatalkan pengajuan bantuan pangan? Data yang sudah diisi tidak akan disimpan.",
            teksKiri = "Kembali",
            onKiri = { dialogBatal = false },
            teksKanan = "Ya, Batalkan",
            onKanan = { dialogBatal = false; onKembali() },
            onDismiss = { dialogBatal = false }
        )
    }

    if (dialogBerhasil) {
        DialogDuaTombol(
            gambar = Aset.ilustrasiPengajuanBerhasil,
            judul = "Pengajuan Berhasil Dikirim!",
            isi = "Pengajuan bantuanmu sedang ditinjau. Kami akan memberi tahu jika tersedia pangan yang sesuai.",
            teksKiri = "Beranda",
            onKiri = { dialogBerhasil = false; onKeBeranda() },
            teksKanan = "Lihat Status",
            onKanan = { dialogBerhasil = false; onLihatStatus() }
        )
    }
}

@Composable
private fun Label(teks: String) {
    Spacer(Modifier.height(16.dp))
    Text(teks, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    Spacer(Modifier.height(6.dp))
}

@Composable
private fun warnaIsian() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Teal,
    unfocusedBorderColor = Color.Transparent,
    focusedContainerColor = AbuIsian,
    unfocusedContainerColor = AbuIsian
)

@Composable
private fun ContohBukti(judul: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(100.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Teal.copy(alpha = 0.15f))
            .padding(8.dp),
        contentAlignment = Alignment.BottomStart
    ) {
        Text(judul, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun DialogDuaTombol(
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
