package com.sempatapps.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// import com.sempatapps.R // Sesuaikan dengan package R Anda

// WARNA SEMPAT
private val Green = Color(0xFF078B70)
private val DarkGreen = Color(0xFF006B58)
private val LightGreen = Color(0xFFE4F6F0)
private val Orange = Color(0xFFFF7824)
private val Background = Color(0xFFF5F8F7)
private val Ink = Color(0xFF263B36)
private val Gray = Color(0xFF858D8A)

// HALAMAN HOME PENERIMA
@Composable
fun HomePenerima() {
    var selectedMenu by remember { mutableIntStateOf(0) }

    Scaffold(
        containerColor = Background,
        bottomBar = {
            BottomNavigationPenerima(
                selectedMenu = selectedMenu,
                onSelect = { selectedMenu = it }
            )
        },
        floatingActionButton = {
            // Tombol mengambang di kanan bawah seperti pada gambar
            FloatingActionButton(
                onClick = { },
                containerColor = Green,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "AI Assistant",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {

                // HEADER
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Logo SemPat (Bisa diganti Image jika ada drawable logo)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Green),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Eco,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "SemPat",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Green
                        )
                    }

                    Spacer(Modifier.weight(1f))

                    // Tombol Notifikasi
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable { },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Notifikasi",
                            tint = Green,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // BANNER SAPAAN
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(170.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF42B69A),
                                    Color(0xFF087B67)
                                )
                            )
                        )
                ) {
                    // Ilustrasi Banner (Ganti dengan painterResource(R.drawable.img_banner_hero) jika ada)
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 12.dp, bottom = 4.dp)
                            .size(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Placeholder ikon jika asset belum ada
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(80.dp)
                        )
                    }

                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 17.dp)
                            .width(165.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            "Halo,",
                            color = Color.White,
                            fontSize = 18.sp
                        )

                        Text(
                            "Pengguna",
                            color = Color.White,
                            fontSize = 21.sp,
                            fontWeight = FontWeight.ExtraBold
                        )

                        Spacer(Modifier.height(2.dp))

                        Text(
                            "Mau cari pangan apa hari ini?",
                            color = Color.White,
                            fontSize = 11.sp,
                            lineHeight = 14.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.End
                        )
                    }
                }

                // LOKASI
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .offset(y = (-14).dp)
                        .clickable { },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(3.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Orange,
                            modifier = Modifier.size(22.dp)
                        )

                        Spacer(Modifier.width(9.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                "Lokasi Saya",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Ink
                            )
                            Spacer(Modifier.height(1.dp))
                            Text(
                                "Jalan Setia Budi, Medan Sumatera Utara",
                                fontSize = 9.sp,
                                color = Gray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Green,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // PANGAN TERDEKAT
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .offset(y = (-4).dp)
                ) {
                    Text(
                        "Pangan Terdekat",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink
                    )

                    Spacer(Modifier.height(10.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        NearbyFoodCard(
                            modifier = Modifier.weight(1f),
                            distance = "1.5 Km",
                            category = "Roti & Kue",
                            name = "Roti Enak",
                            weight = "250g",
                            seller = "Zahran Bakery",
                            time = "Sisa 1 jam 20 menit"
                        )

                        NearbyFoodCard(
                            modifier = Modifier.weight(1f),
                            distance = "1.2 Km",
                            category = "Roti & Kue",
                            name = "Reagan Roti",
                            weight = "250g",
                            seller = "Reagan Roti",
                            time = "Sisa 3 jam"
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    NearbyFoodCardHorizontal(
                        modifier = Modifier.fillMaxWidth(),
                        distance = "1.2 Km",
                        category = "Sayuran & Buah",
                        name = "Fitri Sayur",
                        weight = "1kg",
                        seller = "Penyalur Pangan",
                        time = "Sisa 2 jam"
                    )
                }

                Spacer(Modifier.height(10.dp))

                // BANTUAN PANGAN
                HelpFoodCard(
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(Modifier.height(20.dp))

                // REKOMENDASI
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Rekomendasi Pangan",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Ink
                        )

                        Spacer(Modifier.weight(1f))

                        Text(
                            "Lihat Semua",
                            fontSize = 11.sp,
                            color = DarkGreen,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { }
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    RecommendationCard(
                        category = "Roti & Kue",
                        name = "Roti Enak",
                        weight = "250g",
                        time = "Sisa 45 menit"
                    )

                    RecommendationCard(
                        category = "Makanan Siap Saji",
                        name = "Ayam Penyet",
                        weight = "200g",
                        time = "Sisa 4 jam"
                    )

                    RecommendationCard(
                        category = "Bahan Pokok",
                        name = "Ikan Nila",
                        weight = "2kg",
                        time = "Sisa 1 jam 20 menit"
                    )
                }

                Spacer(Modifier.height(30.dp))
            }
        }
    }
}

// CARD PANGAN TERDEKAT (GRID VERTICAL)
@Composable
private fun NearbyFoodCard(
    modifier: Modifier = Modifier,
    distance: String,
    category: String,
    name: String,
    weight: String,
    seller: String,
    time: String
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(13.dp),
        colors = CardDefaults.cardColors(Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(105.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(LightGreen),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Fastfood,
                    contentDescription = null,
                    tint = Green.copy(alpha = 0.5f),
                    modifier = Modifier.size(40.dp)
                )

                // Badge Jarak
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .clip(RoundedCornerShape(bottomStart = 10.dp))
                        .background(Orange)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        "📍 $distance",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            Text(category, fontSize = 9.sp, color = Gray)

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    name,
                    modifier = Modifier.weight(1f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(weight, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Green)
            }

            Spacer(Modifier.height(2.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Store,
                    contentDescription = null,
                    tint = Gray,
                    modifier = Modifier.size(10.dp)
                )
                Spacer(Modifier.width(3.dp))
                Text(
                    seller,
                    fontSize = 9.sp,
                    color = Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(2.dp))

            Text(
                time,
                fontSize = 9.sp,
                color = Orange,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// CARD PANGAN TERDEKAT (HORIZONTAL)
@Composable
private fun NearbyFoodCardHorizontal(
    modifier: Modifier = Modifier,
    distance: String,
    category: String,
    name: String,
    weight: String,
    seller: String,
    time: String
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(13.dp),
        colors = CardDefaults.cardColors(Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(LightGreen),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Eco,
                    contentDescription = null,
                    tint = Green.copy(alpha = 0.6f),
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(Modifier.width(10.dp))

            Column(Modifier.weight(1f)) {
                Text(category, fontSize = 10.sp, color = Gray)
                Text(
                    name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink
                )
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Store,
                        contentDescription = null,
                        tint = Gray,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(seller, fontSize = 10.sp, color = Gray)
                }
                Spacer(Modifier.height(2.dp))
                Text(time, fontSize = 10.sp, color = Orange, fontWeight = FontWeight.Bold)
            }

            Text(
                weight,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Green
            )
        }
    }
}

// CARD BANTUAN
@Composable
private fun HelpFoodCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { },
        shape = RoundedCornerShape(17.dp),
        colors = CardDefaults.cardColors(Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    "Butuh Bantuan Pangan?",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkGreen
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    "Ajukan bantuan dan dapatkan pangan yang tersedia",
                    fontSize = 10.sp,
                    color = Ink,
                    lineHeight = 14.sp
                )

                Spacer(Modifier.height(10.dp))

                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Green
                    ),
                    contentPadding = PaddingValues(
                        horizontal = 14.dp,
                        vertical = 4.dp
                    ),
                    shape = RoundedCornerShape(30.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        "Minta Bantuan",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Ilustrasi Bantuan (Ganti dengan Image jika ada asset)
            Box(
                modifier = Modifier.size(60.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CardGiftcard,
                    contentDescription = null,
                    tint = Green,
                    modifier = Modifier.size(42.dp)
                )
            }
        }
    }
}

// CARD REKOMENDASI
@Composable
private fun RecommendationCard(
    category: String,
    name: String,
    weight: String,
    time: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clickable { },
        shape = RoundedCornerShape(13.dp),
        colors = CardDefaults.cardColors(Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(LightGreen),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Restaurant,
                    contentDescription = null,
                    tint = Green.copy(alpha = 0.6f),
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(Modifier.width(10.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(category, fontSize = 9.sp, color = Gray)
                Spacer(Modifier.height(1.dp))
                Text(
                    name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink
                )
                Spacer(Modifier.height(2.dp))
                Text(time, fontSize = 9.sp, color = Orange, fontWeight = FontWeight.Bold)
            }

            Text(
                weight,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Green
            )
        }
    }
}

// BOTTOM NAVIGATION
@Composable
private fun BottomNavigationPenerima(
    selectedMenu: Int,
    onSelect: (Int) -> Unit
) {
    Surface(
        color = Color.White,
        shadowElevation = 10.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(60.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val menus = listOf(
                Triple("Beranda", Icons.Default.Home, Icons.Default.Home),
                Triple("Bantuan", Icons.Default.Assignment, Icons.Default.Assignment),
                Triple("Profil", Icons.Default.Person, Icons.Default.Person)
            )

            menus.forEachIndexed { index, menu ->
                val isSelected = selectedMenu == index
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onSelect(index) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(
                                if (isSelected) LightGreen else Color.Transparent
                            )
                            .padding(horizontal = 16.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = menu.second,
                            contentDescription = menu.first,
                            tint = if (isSelected) DarkGreen else Gray,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(Modifier.height(2.dp))

                    Text(
                        menu.first,
                        fontSize = 9.sp,
                        color = if (isSelected) DarkGreen else Gray,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

// PREVIEW ANDROID STUDIO
@Preview(
    showBackground = true,
    showSystemUi = true,
    widthDp = 402,
    heightDp = 874
)
@Composable
fun HomePenerimaPreview() {
    MaterialTheme {
        HomePenerima()
    }
}