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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.sempatapps.core.designsystem.Canvas
import com.sempatapps.core.designsystem.Figtree
import com.sempatapps.core.designsystem.Heading
import com.sempatapps.core.designsystem.Ink
import com.sempatapps.core.designsystem.Muted
import com.sempatapps.core.designsystem.PaleTeal
import com.sempatapps.core.designsystem.Primary
import com.sempatapps.core.designsystem.PrimaryLightHover
import com.sempatapps.core.designsystem.Sora
import com.sempatapps.core.designsystem.Teal
import com.sempatapps.core.designsystem.component.BackButton

private val BlueStatus = Color(0xFF2F88FF)
private val RedDanger = Color(0xFFE64825)

/**
 * Halaman 2: "Donasi Pangan - Donatur" (Detail Pangan) beserta 3 Pop-up:
 * - Status 1: Menunggu Konfirmasi (Belum ada Penerima + tombol Lihat Penerima + Batalkan Donasi)
 * - Status 2: Proses - Disiapkan (Informasi Penerima + Stepper Disiapkan + tombol Perbarui Status + Batalkan Donasi)
 * - Status 3: Proses - Siap Diambil (Informasi Penerima + Stepper Siap Diambil - Menunggu Diambil)
 * - Status 4: Selesai (Nama & Alamat Disembunyikan + Stepper Selesai - Bantuan Selesai)
 * - Pop-up 1: Status Diperbarui!
 * - Pop-up 2: Batalkan Donasi?
 * - Pop-up 3: Donasi Berhasil Dibatalkan
 */
@Composable
fun DetailDonasiDonaturScreen(
    item: DonasiDonaturItem,
    onBackClick: () -> Unit = {},
    onLihatPenerimaClick: () -> Unit = {},
    onPesanClick: (DonasiDonaturItem) -> Unit = {},
    onTelpClick: (DonasiDonaturItem) -> Unit = {},
    onStatusUpdated: (TahapBantuan) -> Unit = {},
    onDonasiCancelled: () -> Unit = {},
) {
    var currentTahap by rememberSaveable { mutableStateOf(item.tahap) }
    var showPopupStatusUpdated by rememberSaveable { mutableStateOf(false) }
    var showPopupKonfirmasiBatal by rememberSaveable { mutableStateOf(false) }
    var showPopupBerhasilBatal by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Canvas)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 14.dp),
    ) {
        // Top Bar: Back Button + Judul "Detail Pangan"
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            BackButton(
                onBackClick = onBackClick,
                modifier = Modifier.align(Alignment.CenterStart),
            )
            Text(
                text = "Detail Pangan",
                fontFamily = Sora,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                color = Heading,
            )
        }

        Spacer(Modifier.height(16.dp))

        // Banner Foto Makanan + Badge Status + Nama Makanan
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(216.dp)
                .clip(RoundedCornerShape(18.dp)),
        ) {
            Image(
                painter = painterResource(item.imageRes),
                contentDescription = item.nama,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            // Gradasi gelap di bawah agar teks putih terbaca jelas
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color(0xAA000000)),
                            startY = 200f,
                        ),
                    ),
            )

            // Badge di pojok kanan atas
            val badgeLabel = when (currentTahap) {
                TahapBantuan.MENUNGGU_KONFIRMASI -> "Menunggu Konfirmasi"
                TahapBantuan.DISIAPKAN, TahapBantuan.SIAP_DIAMBIL, TahapBantuan.SELESAI -> "Proses"
            }
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp),
                shape = RoundedCornerShape(20.dp),
                color = BlueStatus,
            ) {
                Text(
                    text = badgeLabel,
                    fontFamily = Figtree,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp),
                )
            }

            // Kategori & Nama Makanan di pojok kiri bawah
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp),
            ) {
                Text(
                    text = item.kategoriLabel,
                    fontFamily = Figtree,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.9f),
                )
                Text(
                    text = item.nama,
                    fontFamily = Sora,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    color = Color.White,
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // Seksi 1: Informasi Pangan
        Text(
            text = "Informasi Pangan",
            fontFamily = Figtree,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Heading,
        )
        Spacer(Modifier.height(10.dp))
        InformasiPanganCard(item = item)

        Spacer(Modifier.height(20.dp))

        // Seksi 2: Penerima Donasi / Informasi Penerima Donasi
        if (currentTahap == TahapBantuan.MENUNGGU_KONFIRMASI) {
            Text(
                text = "Penerima Donasi",
                fontFamily = Figtree,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Heading,
            )
            Spacer(Modifier.height(10.dp))
            BelumAdaPenerimaCard(onLihatPenerimaClick = onLihatPenerimaClick)
            Spacer(Modifier.height(20.dp))
            BatalkanDonasiButton(onClick = { showPopupKonfirmasiBatal = true })
        } else {
            val isSelesai = currentTahap == TahapBantuan.SELESAI
            Text(
                text = "Informasi Penerima Donasi",
                fontFamily = Figtree,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Heading,
            )
            Spacer(Modifier.height(10.dp))
            InformasiPenerimaCard(
                namaPenerima = if (isSelesai) "Nama\nDisembunyikan" else (item.namaPenerima ?: "Panti Asuhan\nFasilkom"),
                alamatPenerima = if (isSelesai) "Alamat Disembunyikan" else item.alamatPenerima,
                caraSerahTerima = item.caraSerahTerima,
                showTelpButton = !isSelesai,
                onPesanClick = { onPesanClick(item.copy(tahap = currentTahap)) },
                onTelpClick = { onTelpClick(item.copy(tahap = currentTahap)) },
            )

            Spacer(Modifier.height(20.dp))

            // Seksi 3: Status Bantuan Pangan (Stepper)
            Text(
                text = "Status Bantuan Pangan",
                fontFamily = Figtree,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Heading,
            )
            Spacer(Modifier.height(10.dp))
            StatusBantuanCard(
                tahap = currentTahap,
                onPerbaruiStatusClick = {
                    currentTahap = TahapBantuan.SIAP_DIAMBIL
                    onStatusUpdated(TahapBantuan.SIAP_DIAMBIL)
                    showPopupStatusUpdated = true
                },
            )

            // Tombol Batalkan Donasi hanya muncul saat status masih "Disiapkan"
            if (currentTahap == TahapBantuan.DISIAPKAN) {
                Spacer(Modifier.height(18.dp))
                BatalkanDonasiButton(onClick = { showPopupKonfirmasiBatal = true })
            }
        }

        Spacer(Modifier.height(80.dp))
    }

    // ---- 3 POP-UP DIALOG SESUAI FIGMA ----

    if (showPopupStatusUpdated) {
        PopupStatusDiperbarui(
            onLanjutkan = { showPopupStatusUpdated = false },
        )
    }

    if (showPopupKonfirmasiBatal) {
        PopupBatalkanDonasi(
            onKembali = { showPopupKonfirmasiBatal = false },
            onYaBatalkan = {
                showPopupKonfirmasiBatal = false
                showPopupBerhasilBatal = true
            },
        )
    }

    if (showPopupBerhasilBatal) {
        PopupDonasiBerhasilDibatalkan(
            onKembaliKePanganSaya = {
                showPopupBerhasilBatal = false
                onDonasiCancelled()
            },
        )
    }
}

@Composable
private fun InformasiPanganCard(item: DonasiDonaturItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            InfoRow(label = "Berat", value = item.berat.replace("g", "gram"))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Kondisi",
                    fontFamily = Figtree,
                    fontSize = 13.sp,
                    color = Muted,
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PrimaryLightHover,
                ) {
                    Text(
                        text = item.kondisi,
                        fontFamily = Figtree,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 3.dp),
                    )
                }
            }
            InfoRow(label = "Masa Tahan", value = item.masaTahan)
            InfoRow(label = "Didonasikan", value = item.tanggalDonasi)
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            fontFamily = Figtree,
            fontSize = 13.sp,
            color = Muted,
        )
        Text(
            text = value,
            fontFamily = Figtree,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = Ink,
        )
    }
}

@Composable
private fun BelumAdaPenerimaCard(onLihatPenerimaClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Belum ada Penerima",
                fontFamily = Figtree,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Heading,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Anda belum memilih siapa penerima\ndonasi pangan ini",
                fontFamily = Figtree,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                color = Muted,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(14.dp))
            OutlinedButton(
                onClick = onLihatPenerimaClick,
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.5.dp, Primary),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = PrimaryLightHover,
                    contentColor = Primary,
                ),
                contentPadding = PaddingValues(horizontal = 22.dp, vertical = 8.dp),
            ) {
                Text(
                    text = "Lihat Penerima",
                    fontFamily = Figtree,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                )
            }
        }
    }
}

@Composable
private fun InformasiPenerimaCard(
    namaPenerima: String,
    alamatPenerima: String,
    caraSerahTerima: String,
    showTelpButton: Boolean,
    onPesanClick: () -> Unit,
    onTelpClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Baris 1: Nama Penerima + Tombol Pesan & Telp
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(PaleTeal)
                        .border(1.dp, Teal, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(24.dp),
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Nama Penerima",
                        fontFamily = Figtree,
                        fontSize = 11.sp,
                        color = Muted,
                    )
                    Text(
                        text = namaPenerima,
                        fontFamily = Figtree,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        lineHeight = 18.sp,
                        color = Ink,
                    )
                }
                ActionIconButton(
                    icon = Icons.Default.ChatBubble,
                    label = "Pesan",
                    onClick = onPesanClick,
                )
                if (showTelpButton) {
                    Spacer(Modifier.width(12.dp))
                    ActionIconButton(
                        icon = Icons.Default.Call,
                        label = "Telp",
                        onClick = onTelpClick,
                    )
                }
            }

            // Baris 2: Alamat Penerima
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(PaleTeal),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Alamat Penerima",
                        fontFamily = Figtree,
                        fontSize = 11.sp,
                        color = Muted,
                    )
                    Text(
                        text = alamatPenerima,
                        fontFamily = Figtree,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = Ink,
                    )
                }
            }

            // Baris 3: Cara Serah Terima
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(PaleTeal),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Handshake,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Cara Serah Terima",
                        fontFamily = Figtree,
                        fontSize = 11.sp,
                        color = Muted,
                    )
                    Text(
                        text = caraSerahTerima,
                        fontFamily = Figtree,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Ink,
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionIconButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(Primary),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(15.dp),
            )
        }
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            fontFamily = Figtree,
            fontSize = 10.sp,
            color = Muted,
        )
    }
}

@Composable
private fun StatusBantuanCard(
    tahap: TahapBantuan,
    onPerbaruiStatusClick: () -> Unit,
) {
    val stepIndex = when (tahap) {
        TahapBantuan.MENUNGGU_KONFIRMASI, TahapBantuan.DISIAPKAN -> 1
        TahapBantuan.SIAP_DIAMBIL -> 2
        TahapBantuan.SELESAI -> 3
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Baris Stepper 3 tahap: Disiapkan -> Siap Diambil -> Selesai
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                StepCircle(
                    icon = Icons.Default.Inventory2,
                    label = "Disiapkan",
                    active = stepIndex >= 1,
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(3.dp)
                        .padding(horizontal = 4.dp)
                        .background(if (stepIndex >= 2) Primary else Color(0xFFDDE5E2)),
                )
                StepCircle(
                    icon = Icons.Default.Storefront,
                    label = "Siap Diambil",
                    active = stepIndex >= 2,
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(3.dp)
                        .padding(horizontal = 4.dp)
                        .background(if (stepIndex >= 3) Primary else Color(0xFFDDE5E2)),
                )
                StepCircle(
                    icon = Icons.Default.CheckCircle,
                    label = "Selesai",
                    active = stepIndex >= 3,
                )
            }

            Spacer(Modifier.height(18.dp))

            when (tahap) {
                TahapBantuan.DISIAPKAN, TahapBantuan.MENUNGGU_KONFIRMASI -> {
                    Text(
                        text = "Penerima akan datang. Pastikan donasimu sudah siap.",
                        fontFamily = Figtree,
                        fontSize = 12.sp,
                        color = Muted,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(14.dp))
                    Button(
                        onClick = onPerbaruiStatusClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(24.dp),
                        contentPadding = PaddingValues(horizontal = 28.dp, vertical = 10.dp),
                    ) {
                        Text(
                            text = "Perbarui Status",
                            fontFamily = Figtree,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White,
                        )
                    }
                }
                TahapBantuan.SIAP_DIAMBIL -> {
                    Text(
                        text = "Menunggu Diambil",
                        fontFamily = Figtree,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Heading,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Status akan berubah selesai\nsetelah penerima mengkonfirmasi",
                        fontFamily = Figtree,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = Muted,
                        textAlign = TextAlign.Center,
                    )
                }
                TahapBantuan.SELESAI -> {
                    Text(
                        text = "Bantuan Selesai",
                        fontFamily = Figtree,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Heading,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Pangan telah diterima oleh\npenerima donasi",
                        fontFamily = Figtree,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = Muted,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun StepCircle(
    icon: ImageVector,
    label: String,
    active: Boolean,
) {
    val borderColor = if (active) Primary else Color(0xFFB5B5B5)
    val bgColor = if (active) PrimaryLightHover else Color.White

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(bgColor)
                .border(2.dp, borderColor, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = borderColor,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = label,
            fontFamily = Figtree,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
            fontSize = 11.sp,
            color = if (active) Primary else Muted,
        )
    }
}

@Composable
private fun BatalkanDonasiButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(containerColor = RedDanger),
    ) {
        Text(
            text = "Batalkan Donasi",
            fontFamily = Figtree,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = Color.White,
        )
    }
}

// =========================================================================
// 3 POP-UP DIALOG SESUAI FIGMA
// =========================================================================

/**
 * Pop-up 1: "Pop up Perbarui Status" ("Status Diperbarui!" -> "Lanjutkan")
 */
@Composable
fun PopupStatusDiperbarui(
    onLanjutkan: () -> Unit,
) {
    Dialog(onDismissRequest = onLanjutkan) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Teal)
                        .border(2.5.dp, Color(0xFF0A4D44), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Berhasil",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp),
                    )
                }
                Spacer(Modifier.height(18.dp))
                Text(
                    text = "Status Diperbarui!",
                    fontFamily = Figtree,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Heading,
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onLanjutkan,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(24.dp),
                    contentPadding = PaddingValues(horizontal = 36.dp, vertical = 10.dp),
                ) {
                    Text(
                        text = "Lanjutkan",
                        fontFamily = Figtree,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White,
                    )
                }
            }
        }
    }
}

/**
 * Pop-up 2: "Pop up Batal Pesanan" ("Batalkan Donasi" -> "Kembali" | "Ya, Batalkan")
 */
@Composable
fun PopupBatalkanDonasi(
    onKembali: () -> Unit,
    onYaBatalkan: () -> Unit,
) {
    Dialog(onDismissRequest = onKembali) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Kotak ilustrasi sesuai Figma
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(PrimaryLightHover),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.HelpOutline,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(64.dp),
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Batalkan Donasi",
                    fontFamily = Figtree,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Heading,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Kamu yakin ingin membatalkan donasi pangan ini?\nPangan tidak akan lagi disalurkan kepada penerima",
                    fontFamily = Figtree,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = Muted,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(18.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(
                        onClick = onKembali,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.5.dp, Primary),
                    ) {
                        Text(
                            text = "Kembali",
                            fontFamily = Figtree,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Primary,
                        )
                    }
                    Button(
                        onClick = onYaBatalkan,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    ) {
                        Text(
                            text = "Ya, Batalkan",
                            fontFamily = Figtree,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Pop-up 3: "Pop up Keluar pemilihan Penerima" ("Donasi Berhasil Dibatalkan" -> "Kembali ke Pangan Saya")
 */
@Composable
fun PopupDonasiBerhasilDibatalkan(
    onKembaliKePanganSaya: () -> Unit,
) {
    Dialog(onDismissRequest = onKembaliKePanganSaya) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(PrimaryLightHover),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(64.dp),
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Donasi Berhasil Dibatalkan",
                    fontFamily = Figtree,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Heading,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Donasi telah dibatalkan dan pangan tidak lagi\ntersedia untuk penerima",
                    fontFamily = Figtree,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = Muted,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(18.dp))
                Button(
                    onClick = onKembaliKePanganSaya,
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    contentPadding = PaddingValues(horizontal = 28.dp, vertical = 10.dp),
                ) {
                    Text(
                        text = "Kembali ke Pangan Saya",
                        fontFamily = Figtree,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.White,
                    )
                }
            }
        }
    }
}

// =========================================================================
// PREVIEWS UNTUK SEMUA STATUS & POP-UP
// =========================================================================

@Preview(name = "1. Detail - Menunggu Konfirmasi", showBackground = true, widthDp = 402, heightDp = 920)
@Composable
private fun DetailMenungguKonfirmasiPreview() {
    DetailDonasiDonaturScreen(item = sampleDonasiDonaturList[0])
}

@Preview(name = "2. Detail - Proses (Disiapkan)", showBackground = true, widthDp = 402, heightDp = 1050)
@Composable
private fun DetailDisiapkanPreview() {
    DetailDonasiDonaturScreen(item = sampleDonasiDonaturList[1])
}

@Preview(name = "3. Detail - Proses (Siap Diambil)", showBackground = true, widthDp = 402, heightDp = 980)
@Composable
private fun DetailSiapDiambilPreview() {
    DetailDonasiDonaturScreen(
        item = sampleDonasiDonaturList[1].copy(tahap = TahapBantuan.SIAP_DIAMBIL),
    )
}

@Preview(name = "4. Detail - Selesai", showBackground = true, widthDp = 402, heightDp = 980)
@Composable
private fun DetailSelesaiPreview() {
    DetailDonasiDonaturScreen(item = sampleDonasiDonaturList[2])
}
