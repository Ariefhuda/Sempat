package com.sempatapps.presentation.donation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sempatapps.R
import com.sempatapps.core.designsystem.Canvas
import com.sempatapps.core.designsystem.Figtree
import com.sempatapps.core.designsystem.Heading
import com.sempatapps.core.designsystem.Ink
import com.sempatapps.core.designsystem.Muted
import com.sempatapps.core.designsystem.Orange
import com.sempatapps.core.designsystem.PaleTeal
import com.sempatapps.core.designsystem.Primary
import com.sempatapps.core.designsystem.Sora
import com.sempatapps.core.designsystem.Teal
import com.sempatapps.core.designsystem.component.BackButton
import kotlinx.coroutines.delay

/**
 * Model satu bubble chat antara Donatur dan Penerima.
 */
data class PesanChatDonatur(
    val id: String,
    val teks: String,
    val dariDonatur: Boolean,
)

val samplePesanDonaturList = listOf(
    PesanChatDonatur(
        id = "msg-1",
        teks = "Halo Donasi saya tunggu ya",
        dariDonatur = false,
    ),
    PesanChatDonatur(
        id = "msg-2",
        teks = "baik kak semoga bermanfaat!",
        dariDonatur = true,
    ),
)

/**
 * Halaman 3: "Pesan Donatur" (Chat dengan Penerima Donasi)
 * - Mendukung mode Chat Aktif (nama penerima terlihat + tombol Telp + bisa kirim pesan)
 * - Mendukung mode Chat Selesai ("Nama Disembunyikan", tanpa tombol Telp, divider "Sesi Pesan Berakhir")
 */
@Composable
fun PesanDonaturScreen(
    item: DonasiDonaturItem = sampleDonasiDonaturList[1],
    sesiBerakhir: Boolean = item.tahap == TahapBantuan.SELESAI,
    onBackClick: () -> Unit = {},
    onTelpClick: () -> Unit = {},
    onRincianClick: () -> Unit = {},
) {
    val daftarPesan = remember { mutableStateListOf(*samplePesanDonaturList.toTypedArray()) }
    var draftPesan by rememberSaveable { mutableStateOf("") }

    val kirimPesan = {
        val teks = draftPesan.trim()
        if (teks.isNotEmpty() && !sesiBerakhir) {
            daftarPesan.add(
                PesanChatDonatur(
                    id = "msg-${daftarPesan.size + 1}",
                    teks = teks,
                    dariDonatur = true,
                ),
            )
            draftPesan = ""
        }
    }

    val namaTampil = if (sesiBerakhir) "Nama\nDisembunyikan" else (item.namaPenerima ?: "Panti Asuhan\nFasilkom")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE5E9E8))
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
    ) {
        // 1. Header Putih: BackButton + Avatar + Nama Penerima + Tombol Telp
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Canvas,
            shadowElevation = 2.dp,
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BackButton(onBackClick = onBackClick)
                    Spacer(Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(PaleTeal)
                            .border(1.5.dp, Teal, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = namaTampil,
                        fontFamily = Figtree,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        lineHeight = 19.sp,
                        color = Heading,
                        modifier = Modifier.weight(1f),
                    )
                    if (!sesiBerakhir) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable(onClick = onTelpClick)
                                .padding(horizontal = 6.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Telepon Penerima",
                                tint = Primary,
                                modifier = Modifier.size(22.dp),
                            )
                            Text(
                                text = "Telp",
                                fontFamily = Figtree,
                                fontSize = 11.sp,
                                color = Ink,
                            )
                        }
                    }
                }

                // Garis tipis pemisah
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0xFFE0E6E4)),
                )

                // 2. Kartu Ringkasan Pangan yang Didonasikan (Pinned Item)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        painter = painterResource(item.imageRes),
                        contentDescription = item.nama,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop,
                    )
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.nama,
                            fontFamily = Figtree,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Heading,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = item.berat.replace("g", " gram"),
                            fontFamily = Figtree,
                            fontSize = 13.sp,
                            color = Muted,
                        )
                    }
                    OutlinedButton(
                        onClick = onRincianClick,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Ink),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp),
                    ) {
                        Text(
                            text = "Rincian",
                            fontFamily = Figtree,
                            fontSize = 12.sp,
                            color = Ink,
                        )
                    }
                }
            }
        }

        // 3. Area Bubble Chat
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            daftarPesan.forEach { pesan ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = if (pesan.dariDonatur) Alignment.CenterEnd else Alignment.CenterStart,
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (pesan.dariDonatur) Primary else Color.White,
                        shadowElevation = 1.dp,
                        modifier = Modifier.widthIn(max = 270.dp),
                    ) {
                        Text(
                            text = pesan.teks,
                            fontFamily = Figtree,
                            fontSize = 13.sp,
                            color = if (pesan.dariDonatur) Color.White else Ink,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        )
                    }
                }
            }

            if (sesiBerakhir) {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp)
                            .background(Color(0xFFBDC5C3)),
                    )
                    Text(
                        text = "  Sesi Pesan Berakhir  ",
                        fontFamily = Figtree,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Muted,
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp)
                            .background(Color(0xFFBDC5C3)),
                    )
                }
            }
        }

        // 4. Bar Input Pesan Bawah
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 6.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Tombol + bulat
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .border(1.dp, Ink, CircleShape)
                        .clickable(enabled = !sesiBerakhir) {},
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Tambah Lampiran",
                        tint = Ink,
                        modifier = Modifier.size(20.dp),
                    )
                }

                // Kotak input teks "Masukkan Pesan"
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, Color(0xFFD0D7D5), RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (draftPesan.isEmpty()) {
                        Text(
                            text = "Masukkan Pesan",
                            fontFamily = Figtree,
                            fontSize = 13.sp,
                            color = Muted,
                        )
                    }
                    BasicTextField(
                        value = draftPesan,
                        onValueChange = { if (!sesiBerakhir) draftPesan = it },
                        enabled = !sesiBerakhir,
                        singleLine = true,
                        textStyle = TextStyle(
                            fontFamily = Figtree,
                            fontSize = 13.sp,
                            color = Ink,
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { kirimPesan() }),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                // Tombol Kirim hijau bulat
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Primary)
                        .clickable(enabled = !sesiBerakhir) { kirimPesan() },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Kirim Pesan",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

/**
 * Halaman 4: "Pesan Donatur - Telepon Penerima" (Layar Panggilan Suara)
 * Sesuai 2 frame panggilan telepon di Figma Task #3.
 */
@Composable
fun TeleponPenerimaScreen(
    namaPenerima: String = "Panti Asuhan\nFasilkom",
    onEndCallClick: () -> Unit = {},
) {
    var detikPanggilan by rememberSaveable { mutableIntStateOf(2) }
    var speakerAktif by rememberSaveable { mutableStateOf(false) }
    var muteAktif by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            detikPanggilan++
        }
    }

    val menit = detikPanggilan / 60
    val detik = detikPanggilan % 60
    val waktuFormat = "%02d:%02d".format(menit, detik)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Canvas)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(56.dp))

        Text(
            text = "Penerima",
            fontFamily = Figtree,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Orange,
        )

        Spacer(Modifier.height(18.dp))

        // Avatar Penerima Besar
        Box(
            modifier = Modifier
                .size(128.dp)
                .clip(CircleShape)
                .background(PaleTeal)
                .border(2.5.dp, Color(0xFF0A4D44), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = namaPenerima,
                tint = Primary,
                modifier = Modifier.size(78.dp),
            )
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = namaPenerima,
            fontFamily = Sora,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 20.sp,
            lineHeight = 26.sp,
            color = Heading,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = waktuFormat,
            fontFamily = Figtree,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Muted,
        )

        Spacer(Modifier.weight(1f))

        // 3 Tombol Kontrol Panggilan: Speaker | End Call Merah | Mute
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 36.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Tombol Speaker
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(if (speakerAktif) PaleTeal else Color(0xFFE0E0E0))
                    .clickable { speakerAktif = !speakerAktif },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = "Speaker",
                    tint = if (speakerAktif) Primary else Ink,
                    modifier = Modifier.size(26.dp),
                )
            }

            // Tombol Tutup Telepon Merah
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE64825))
                    .clickable(onClick = onEndCallClick),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.CallEnd,
                    contentDescription = "Akhiri Panggilan",
                    tint = Color.White,
                    modifier = Modifier.size(34.dp),
                )
            }

            // Tombol Mute
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(if (muteAktif) PaleTeal else Color(0xFFE0E0E0))
                    .clickable { muteAktif = !muteAktif },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.MicOff,
                    contentDescription = "Bisukan Mikrofon",
                    tint = if (muteAktif) Primary else Ink,
                    modifier = Modifier.size(26.dp),
                )
            }
        }
    }
}

@Preview(name = "Pesan Donatur - Chat Aktif", showBackground = true, widthDp = 402, heightDp = 874)
@Composable
private fun PesanDonaturAktifPreview() {
    PesanDonaturScreen(
        item = sampleDonasiDonaturList[1],
        sesiBerakhir = false,
    )
}

@Preview(name = "Pesan Donatur - Sesi Berakhir", showBackground = true, widthDp = 402, heightDp = 874)
@Composable
private fun PesanDonaturSelesaiPreview() {
    PesanDonaturScreen(
        item = sampleDonasiDonaturList[2],
        sesiBerakhir = true,
    )
}

@Preview(name = "Telepon Penerima", showBackground = true, widthDp = 402, heightDp = 874)
@Composable
private fun TeleponPenerimaPreview() {
    TeleponPenerimaScreen()
}
