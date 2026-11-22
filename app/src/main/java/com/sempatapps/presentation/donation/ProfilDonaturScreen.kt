package com.sempatapps.presentation.donation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas as ComposeCanvas
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.EditLocationAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
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
import com.sempatapps.core.designsystem.Orange
import com.sempatapps.core.designsystem.PaleTeal
import com.sempatapps.core.designsystem.Primary
import com.sempatapps.core.designsystem.PrimaryLightHover
import com.sempatapps.core.designsystem.Sora
import com.sempatapps.core.designsystem.Teal
import com.sempatapps.core.designsystem.component.BackButton

/**
 * Halaman 5: "Profil Donatur" beserta 2 Pop-up ("Pop up Ganti alamat" & "Pop up Log Out")
 * Sesuai Figma: "3. Kelola donasi (donatur) / Profil Donatur"
 */
@Composable
fun ProfilDonaturScreen(
    namaPengguna: String = "Nama Pengguna",
    peranLabel: String = "Pedagang Pangan",
    kotaLabel: String = "Medan, Sumatera Utara",
    nomorHp: String = "Nomor Handphone",
    alamatUsaha: String = "Jalan Jamin Ginting Medan, Sumatera Utara",
    onBukaPilihAlamat: () -> Unit = {},
    onKeluarAkun: () -> Unit = {},
) {
    var showPopupGantiAlamat by rememberSaveable { mutableStateOf(false) }
    var showPopupLogOut by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Canvas)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Judul Halaman
        Text(
            text = "Profil",
            fontFamily = Sora,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 22.sp,
            color = Heading,
        )

        Spacer(Modifier.height(16.dp))

        // Avatar Donatur
        Box(
            modifier = Modifier
                .size(104.dp)
                .clip(CircleShape)
                .background(PaleTeal)
                .border(2.5.dp, Teal, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = namaPengguna,
                tint = Primary,
                modifier = Modifier.size(62.dp),
            )
        }

        Spacer(Modifier.height(12.dp))

        // Nama Pengguna & Peran
        Text(
            text = namaPengguna,
            fontFamily = Sora,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp,
            color = Primary,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = peranLabel,
            fontFamily = Figtree,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = Orange,
        )
        Spacer(Modifier.height(4.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(14.dp),
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = kotaLabel,
                fontFamily = Figtree,
                fontSize = 11.sp,
                color = Ink,
            )
        }

        Spacer(Modifier.height(24.dp))

        // Seksi 1: Informasi Pengguna
        Text(
            text = "Informasi Pengguna",
            fontFamily = Figtree,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Heading,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(10.dp))

        ProfilInfoTile(
            icon = Icons.Default.Person,
            label = namaPengguna,
        )
        Spacer(Modifier.height(10.dp))
        ProfilInfoTile(
            icon = Icons.Default.Call,
            label = nomorHp,
        )
        Spacer(Modifier.height(10.dp))
        ProfilInfoTile(
            icon = Icons.Default.Badge,
            label = "KTP Terverifikasi",
        )

        Spacer(Modifier.height(22.dp))

        // Seksi 2: Alamat Usaha
        Text(
            text = "Alamat Usaha",
            fontFamily = Figtree,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Heading,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(10.dp))

        AlamatUsahaCard(
            alamat = alamatUsaha,
            onUbahAlamatClick = { showPopupGantiAlamat = true },
        )

        Spacer(Modifier.height(22.dp))

        // Tombol Merah Outline: Keluar dari Akun
        OutlinedButton(
            onClick = { showPopupLogOut = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(26.dp),
            border = BorderStroke(1.2.dp, Color(0xFFD64527)),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.White,
                contentColor = Color(0xFFD64527),
            ),
        ) {
            Text(
                text = "Keluar dari Akun",
                fontFamily = Figtree,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color(0xFFD64527),
            )
        }

        Spacer(Modifier.height(80.dp))
    }

    // ---- 2 POP-UP DIALOG SESUAI FIGMA ----

    if (showPopupGantiAlamat) {
        PopupGantiAlamat(
            onBatal = { showPopupGantiAlamat = false },
            onYaGanti = {
                showPopupGantiAlamat = false
                onBukaPilihAlamat()
            },
        )
    }

    if (showPopupLogOut) {
        PopupKeluarAkun(
            onBatal = { showPopupLogOut = false },
            onYaKeluar = {
                showPopupLogOut = false
                onKeluarAkun()
            },
        )
    }
}

@Composable
private fun ProfilInfoTile(
    icon: ImageVector,
    label: String,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(PaleTeal),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(18.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = label,
                fontFamily = Figtree,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Ink,
            )
        }
    }
}

@Composable
private fun AlamatUsahaCard(
    alamat: String,
    onUbahAlamatClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.2.dp, Primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column {
            // Ilustrasi Peta Mini + Pin "Alamat Saya"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(104.dp)
                    .background(Color(0xFFEAF4F2)),
                contentAlignment = Alignment.Center,
            ) {
                // Garis jalan simulasi peta
                ComposeCanvas(modifier = Modifier.fillMaxSize()) {
                    drawLine(
                        color = Color(0xFFFFFFFF),
                        start = Offset(0f, size.height * 0.4f),
                        end = Offset(size.width, size.height * 0.65f),
                        strokeWidth = 18f,
                    )
                    drawLine(
                        color = Color(0xFFA7D8F0),
                        start = Offset(size.width * 0.25f, 0f),
                        end = Offset(size.width * 0.35f, size.height),
                        strokeWidth = 14f,
                    )
                    drawLine(
                        color = Color(0xFFFAD284),
                        start = Offset(0f, size.height * 0.75f),
                        end = Offset(size.width, size.height * 0.2f),
                        strokeWidth = 14f,
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFD96B27),
                    ) {
                        Text(
                            text = "Alamat Saya",
                            fontFamily = Figtree,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Pin Lokasi",
                        tint = Primary,
                        modifier = Modifier.size(30.dp),
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(PaleTeal),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = alamat,
                        fontFamily = Figtree,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Ink,
                    )
                }

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = onUbahAlamatClick,
                    shape = RoundedCornerShape(22.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    contentPadding = PaddingValues(horizontal = 26.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = "Ubah Alamat",
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

/**
 * Pop-up "Pop up Ganti alamat" ("Ganti Alamat?" -> "Batal" | "Ya, Ganti")
 */
@Composable
fun PopupGantiAlamat(
    onBatal: () -> Unit,
    onYaGanti: () -> Unit,
) {
    Dialog(onDismissRequest = onBatal) {
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
                        imageVector = Icons.Default.EditLocationAlt,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(64.dp),
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Ganti Alamat?",
                    fontFamily = Figtree,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Heading,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Perbarui alamat usaha yang digunakan untuk\npengambilan pangan.",
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
                        onClick = onBatal,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.5.dp, Primary),
                    ) {
                        Text(
                            text = "Batal",
                            fontFamily = Figtree,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Primary,
                        )
                    }
                    Button(
                        onClick = onYaGanti,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    ) {
                        Text(
                            text = "Ya, Ganti",
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
 * Pop-up "Pop up Log Out" ("Keluar dari Akun?" -> "Batal" | "Ya, Keluar")
 */
@Composable
fun PopupKeluarAkun(
    onBatal: () -> Unit,
    onYaKeluar: () -> Unit,
) {
    Dialog(onDismissRequest = onBatal) {
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
                        imageVector = Icons.Default.Logout,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(60.dp),
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Keluar dari Akun?",
                    fontFamily = Figtree,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Heading,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Kamu yakin ingin keluar dari akun SEMPAT?",
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
                        onClick = onBatal,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.5.dp, Primary),
                    ) {
                        Text(
                            text = "Batal",
                            fontFamily = Figtree,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Primary,
                        )
                    }
                    Button(
                        onClick = onYaKeluar,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    ) {
                        Text(
                            text = "Ya, Keluar",
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
 * Halaman 6: "Pilih Alamat" (Layar Peta Ubah Alamat Usaha Donatur)
 * Sesuai frame "Pilih Alamat" di Figma "3. Kelola donasi (donatur) / Profil Donatur".
 */
@Composable
fun PilihAlamatDonaturScreen(
    initialQuery: String = "",
    onBackClick: () -> Unit = {},
    onKonfirmasiAlamat: (String) -> Unit = {},
) {
    var searchQuery by rememberSaveable { mutableStateOf(initialQuery) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE9F2F0)),
    ) {
        // Latar Peta Interaktif / Visualisasi Peta Medan sesuai Figma
        ComposeCanvas(modifier = Modifier.fillMaxSize()) {
            // Sungai / jalur biru
            drawLine(
                color = Color(0xFF9AD4EA),
                start = Offset(size.width * 0.72f, 0f),
                end = Offset(size.width * 0.58f, size.height),
                strokeWidth = 34f,
            )
            // Jalan utama kuning/oranye
            drawLine(
                color = Color(0xFFF9D57E),
                start = Offset(0f, size.height * 0.35f),
                end = Offset(size.width, size.height * 0.55f),
                strokeWidth = 28f,
            )
            // Jalan putih sekunder
            drawLine(
                color = Color.White,
                start = Offset(size.width * 0.25f, 0f),
                end = Offset(size.width * 0.45f, size.height),
                strokeWidth = 22f,
            )
            drawLine(
                color = Color.White,
                start = Offset(0f, size.height * 0.7f),
                end = Offset(size.width, size.height * 0.25f),
                strokeWidth = 20f,
            )
        }

        // Pin Tengah: "Pilih Lokasi"
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFD96B27),
                shadowElevation = 3.dp,
            ) {
                Text(
                    text = "Pilih Lokasi",
                    fontFamily = Figtree,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
            Spacer(Modifier.height(2.dp))
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Pin Pilih Lokasi",
                tint = Primary,
                modifier = Modifier.size(44.dp),
            )
        }

        // Bagian Atas: Tombol Back + Search Bar "Cari Lokasi"
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            BackButton(onBackClick = onBackClick)
            Spacer(Modifier.height(12.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 4.dp,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Cari Lokasi",
                                fontFamily = Figtree,
                                fontSize = 13.sp,
                                color = Muted,
                            )
                        }
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            singleLine = true,
                            textStyle = TextStyle(
                                fontFamily = Figtree,
                                fontSize = 13.sp,
                                color = Ink,
                            ),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Cari",
                        tint = Primary,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
        }

        // Bagian Bawah: Panel Putih + Tombol Hijau "Konfirmasi Alamat"
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            color = Color.White,
            shadowElevation = 8.dp,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 18.dp),
            ) {
                Button(
                    onClick = {
                        val hasilAlamat = searchQuery.ifBlank { "Jalan Jamin Ginting Medan, Sumatera Utara" }
                        onKonfirmasiAlamat(hasilAlamat)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                ) {
                    Text(
                        text = "Konfirmasi Alamat",
                        fontFamily = Figtree,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White,
                    )
                }
            }
        }
    }
}

@Preview(name = "Profil Donatur", showBackground = true, widthDp = 402, heightDp = 874)
@Composable
private fun ProfilDonaturPreview() {
    ProfilDonaturScreen()
}

@Preview(name = "Pilih Alamat Donatur", showBackground = true, widthDp = 402, heightDp = 874)
@Composable
private fun PilihAlamatDonaturPreview() {
    PilihAlamatDonaturScreen()
}
