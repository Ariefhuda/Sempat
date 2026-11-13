package com.example.sempat

import com.example.sempat.ui.theme.Teal

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val LatarChat = Color(0xFFE2EAE9)

// Satu pasang pertanyaan-jawaban + kata kunci untuk pencocokan saat user mengetik
data class Faq(val tanya: String, val jawab: String, val kataKunci: List<String>)

// Satu gelembung pesan. denganPilihan = true -> bubble sapaan yang memuat daftar pertanyaan
data class PesanChat(
    val teks: String,
    val dariUser: Boolean,
    val denganPilihan: Boolean = false
)

// Isi jawaban ini DRAFT, cocokkan dengan alur aplikasi final bersama tim
val daftarFaq = listOf(
    Faq(
        "Apa itu SEMPAT?",
        "SEMPAT adalah aplikasi yang menyalurkan makanan berlebih yang masih layak kepada orang yang membutuhkan. Semua pangan dibagikan gratis, tidak ada jual beli di aplikasi ini.",
        listOf("sempat", "apa itu", "aplikasi")
    ),
    Faq(
        "Bagaimana cara mendapatkan bantuan pangan?",
        "Buka Beranda atau tab Pangan, pilih pangan yang lokasinya terjangkau, lalu tekan Ajukan Pengambilan. Kalau butuh bantuan khusus, tekan Minta Bantuan dan unggah bukti kebutuhanmu.",
        listOf("bantuan", "minta", "dapat", "ajukan", "mengajukan")
    ),
    Faq(
        "Bagaimana cara mendonasikan pangan?",
        "Daftar sebagai Donatur, lalu foto makanan yang mau dibagikan. Aplikasi akan mengenali jenisnya dan mengisi formulir, tinggal lengkapi jumlah porsi, batas waktu pengambilan, dan lokasi.",
        listOf("donasi", "mendonasikan", "donatur", "berbagi", "bagikan")
    ),
    Faq(
        "Bagaimana cara mengambil pangan?",
        "Setelah pengajuanmu disetujui, datang sendiri ke lokasi donatur sebelum batas waktu pengambilan habis. Tidak ada pengantaran. Setelah pangan diterima, tekan Konfirmasi di tab Bantuan.",
        listOf("ambil", "mengambil", "antar", "kirim", "lokasi", "waktu", "terima")
    ),
    Faq(
        "Bagaimana SEMPAT memverifikasi pengguna dan pangan?",
        "Pengguna mendaftar dengan memotret KTP agar donatur dan penerima sama-sama orang sungguhan. Untuk pangan, donatur mencantumkan batas waktu pengambilan, dan pangan yang waktunya habis tidak lagi tampil.",
        listOf("verifikasi", "memverifikasi", "aman", "layak", "ktp", "valid")
    )
)

fun pesanAwalChat(): SnapshotStateList<PesanChat> = mutableStateListOf(
    PesanChat(
        "Halo! Ada yang bisa kubantu?\nPilih pertanyaan atau tanyakan apa saja tentang SEMPAT.",
        dariUser = false,
        denganPilihan = true
    )
)

// Cocokkan kata kunci pada teks yang diketik user
fun cariJawaban(input: String): String {
    val teks = input.lowercase()
    val terbaik = daftarFaq.maxByOrNull { faq -> faq.kataKunci.count { it in teks } }
    return if (terbaik != null && terbaik.kataKunci.any { it in teks }) {
        terbaik.jawab
    } else {
        "Maaf, aku belum paham pertanyaanmu. Coba pilih salah satu pertanyaan di atas, ya."
    }
}

// Tombol pil melayang "Tanya Yuk"
@Composable
fun TombolTanyaYuk(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val bentuk = RoundedCornerShape(50)
    Surface(
        shape = bentuk,
        color = Teal,
        shadowElevation = 6.dp,
        modifier = modifier
            .clip(bentuk)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.ikon_bot),
                contentDescription = null,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text("Tanya Yuk", color = Color.White, fontWeight = FontWeight.SemiBold)
        }
    }
}

// Layar chatbot penuh, sesuai Figma "Asisten SemPat"
@Composable
fun ChatbotScreen(
    onKembali: () -> Unit,
    pesan: SnapshotStateList<PesanChat> = remember { pesanAwalChat() }
) {
    val context = LocalContext.current
    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    fun kirimPilihan(faq: Faq) {
        pesan.add(PesanChat(faq.tanya, dariUser = true))
        pesan.add(PesanChat(faq.jawab, dariUser = false))
    }

    fun kirimKetikan() {
        val teks = input.trim()
        if (teks.isEmpty()) return
        pesan.add(PesanChat(teks, dariUser = true))
        pesan.add(PesanChat(cariJawaban(teks), dariUser = false))
        input = ""
    }

    // Setiap ada pesan baru, scroll ke paling bawah
    LaunchedEffect(pesan.size) {
        listState.animateScrollToItem(pesan.lastIndex)
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(LatarChat)
            .imePadding()
    ) {
        // Header
        Surface(color = Color(0xFFF4FBFA), shadowElevation = 2.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
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
                AvatarBot(40.dp)
                Spacer(Modifier.width(10.dp))
                Text("Asisten SemPat", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
        }

        // Daftar pesan
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(pesan) { p ->
                if (p.dariUser) {
                    BubbleUser(p.teks)
                } else {
                    BubbleBot(p, onPilih = { kirimPilihan(it) })
                }
            }
        }

        // Kolom input
        Surface(color = Color.White, shadowElevation = 8.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tombol + (lampiran), belum ada fungsinya
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color.Black, CircleShape)
                        .clickable {
                            Toast
                                .makeText(context, "Lampiran belum tersedia", Toast.LENGTH_SHORT)
                                .show()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah", modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(8.dp))

                TextField(
                    value = input,
                    onValueChange = { input = it },
                    placeholder = { Text("Masukkan Pesan", fontSize = 13.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(50),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFE3E3E3),
                        unfocusedContainerColor = Color(0xFFE3E3E3),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { kirimKetikan() }),
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Teal)
                        .clickable { kirimKetikan() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "Kirim",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AvatarBot(ukuran: Dp) {
    Image(
        painter = painterResource(R.drawable.avatar_bot),
        contentDescription = "Asisten SemPat",
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(ukuran)
            .clip(CircleShape)
    )
}

@Composable
private fun BubbleBot(pesan: PesanChat, onPilih: (Faq) -> Unit) {
    Row(verticalAlignment = Alignment.Top) {
        AvatarBot(28.dp)
        Spacer(Modifier.width(8.dp))
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            modifier = Modifier.widthIn(max = 290.dp)
        ) {
            Column {
                Text(pesan.teks, fontSize = 13.sp, modifier = Modifier.padding(12.dp))

                if (pesan.denganPilihan) {
                    daftarFaq.forEach { faq ->
                        HorizontalDivider(color = Color(0xFFDDDDDD))
                        Text(
                            faq.tanya,
                            color = Teal,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPilih(faq) }
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BubbleUser(teks: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 260.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Teal)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(teks, color = Color.White, fontSize = 13.sp)
        }
        Spacer(Modifier.width(8.dp))
        Image(
            painter = painterResource(R.drawable.avatar_penerima),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
        )
    }
}
