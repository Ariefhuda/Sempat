package com.sempatapps.presentation.donation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BakeryDining
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sempatapps.R
import com.sempatapps.core.designsystem.Canvas
import com.sempatapps.core.designsystem.DarkTeal
import com.sempatapps.core.designsystem.Figtree
import com.sempatapps.core.designsystem.Heading
import com.sempatapps.core.designsystem.Ink
import com.sempatapps.core.designsystem.Muted
import com.sempatapps.core.designsystem.PaleTeal
import com.sempatapps.core.designsystem.Primary
import com.sempatapps.core.designsystem.PrimaryLightHover
import com.sempatapps.core.designsystem.Sora
import com.sempatapps.core.designsystem.Teal

/**
 * Tab status pada halaman "Pangan Saya" (Donatur).
 * Sesuai Figma: "3. Kelola donasi (donatur) / Pangan Saya Donasi"
 */
enum class PanganTab(val label: String) {
    AKTIF("Aktif"),
    PROSES("Proses"),
    SELESAI("Selesai"),
}

/**
 * Kategori pangan pada halaman "Pangan Saya".
 */
enum class KategoriPangan(val label: String, val icon: ImageVector, val tint: Color) {
    KEBUTUHAN_POKOK("Kebutuhan\nPokok", Icons.Default.Inventory2, Color(0xFF0E7C66)),
    SAYUR_BUAH("Sayur & Buah", Icons.Default.Eco, Color(0xFF2E9D52)),
    MAKANAN_SIAP_SAJI("Makanan\nSiap Saji", Icons.Default.Restaurant, Color(0xFF1995AD)),
}

/**
 * Tahap status bantuan pangan saat donasi sedang berjalan / selesai.
 */
enum class TahapBantuan {
    MENUNGGU_KONFIRMASI,
    DISIAPKAN,
    SIAP_DIAMBIL,
    SELESAI,
}

/**
 * Model data item donasi untuk fitur Kelola Donasi (Donatur) - Nomor 3.
 */
data class DonasiDonaturItem(
    val id: String,
    val nama: String,
    val kategoriLabel: String,
    val kategoriFilter: KategoriPangan?,
    val berat: String,
    val kondisi: String = "Layak",
    val masaTahan: String = "2 Hari",
    val tanggalDonasi: String = "13 Agustus 2026",
    val tab: PanganTab,
    val tahap: TahapBantuan,
    val namaPenerima: String? = null,
    val alamatPenerima: String = "Jl Setia Budi\nMedan, Sumatera Utara",
    val caraSerahTerima: String = "Diambil oleh Penerima",
    val imageRes: Int = R.drawable.bread_photo,
)

/**
 * Data contoh (dummy) sesuai 3 frame "Pangan Saya - Donasi" di Figma (Aktif, Proses, Selesai).
 */
val sampleDonasiDonaturList = listOf(
    DonasiDonaturItem(
        id = "donasi-aktif-1",
        nama = "Roti Enak",
        kategoriLabel = "Roti & Kue",
        kategoriFilter = KategoriPangan.MAKANAN_SIAP_SAJI,
        berat = "250g",
        tab = PanganTab.AKTIF,
        tahap = TahapBantuan.MENUNGGU_KONFIRMASI,
        namaPenerima = null,
        imageRes = R.drawable.bread_photo,
    ),
    DonasiDonaturItem(
        id = "donasi-proses-1",
        nama = "Roti Enak",
        kategoriLabel = "Roti & Kue",
        kategoriFilter = KategoriPangan.MAKANAN_SIAP_SAJI,
        berat = "250g",
        tab = PanganTab.PROSES,
        tahap = TahapBantuan.DISIAPKAN,
        namaPenerima = "Panti Asuhan\nFasilkom",
        imageRes = R.drawable.bread_photo,
    ),
    DonasiDonaturItem(
        id = "donasi-selesai-1",
        nama = "Roti Enak",
        kategoriLabel = "Roti & Kue",
        kategoriFilter = KategoriPangan.MAKANAN_SIAP_SAJI,
        berat = "250g",
        tab = PanganTab.SELESAI,
        tahap = TahapBantuan.SELESAI,
        namaPenerima = "Panti Asuhan\nFasilkom",
        imageRes = R.drawable.bread_photo,
    ),
)

/**
 * Halaman 1: "Pangan Saya - Donasi"
 * Menampilkan tab status (Aktif, Proses, Selesai), filter Kategori Pangan, dan kartu pangan yang didonasikan.
 */
@Composable
fun PanganSayaScreen(
    items: List<DonasiDonaturItem> = sampleDonasiDonaturList,
    initialTab: PanganTab = PanganTab.AKTIF,
    onItemClick: (DonasiDonaturItem) -> Unit = {},
) {
    var selectedTab by rememberSaveable { mutableStateOf(initialTab) }
    var selectedKategori by rememberSaveable { mutableStateOf<KategoriPangan?>(null) }

    val filteredItems = items.filter { item ->
        item.tab == selectedTab && (selectedKategori == null || item.kategoriFilter == selectedKategori)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Canvas)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp),
    ) {
        // Header: Pangan Saya
        Text(
            text = "Pangan Saya",
            fontFamily = Sora,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 24.sp,
            color = Heading,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Kelola dan lihat riwayat pangan\nyang kamu donasikan",
            fontFamily = Figtree,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = Muted,
        )

        Spacer(Modifier.height(14.dp))

        // Label Urutkan
        Text(
            text = "Urutkan",
            fontFamily = Figtree,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = Muted,
        )

        Spacer(Modifier.height(10.dp))

        // Baris Tab: Aktif | Proses | Selesai
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PanganTab.entries.forEach { tab ->
                val isSelected = selectedTab == tab
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (isSelected) PrimaryLightHover else Color.Transparent)
                        .clickable { selectedTab = tab },
                ) {
                    Text(
                        text = tab.label,
                        fontFamily = Figtree,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 14.sp,
                        color = if (isSelected) Primary else Ink,
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        // Garis pembatas tipis di bawah tab
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFFDDE5E2)),
        )

        Spacer(Modifier.height(16.dp))

        // Judul Kategori Pangan
        Text(
            text = "Kategori Pangan",
            fontFamily = Figtree,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Primary,
        )

        Spacer(Modifier.height(12.dp))

        // 3 Kartu Kategori Pangan
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            KategoriPangan.entries.forEach { kategori ->
                val isSelected = selectedKategori == kategori
                KategoriPanganCard(
                    kategori = kategori,
                    isSelected = isSelected,
                    onClick = {
                        selectedKategori = if (isSelected) null else kategori
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Spacer(Modifier.height(26.dp))

        // Daftar Kartu Pangan sesuai Tab yang dipilih
        if (filteredItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 48.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Belum ada pangan pada tab ${selectedTab.label}",
                    fontFamily = Figtree,
                    fontSize = 14.sp,
                    color = Muted,
                )
            }
        } else {
            // Ditampilkan 2 kolom (grid) seperti di Figma
            val rows = filteredItems.chunked(2)
            rows.forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    rowItems.forEach { item ->
                        PanganSayaFoodCard(
                            item = item,
                            onClick = { onItemClick(item) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (rowItems.size == 1) {
                        Spacer(Modifier.weight(1f))
                    }
                }
                Spacer(Modifier.height(20.dp))
            }
        }

        Spacer(Modifier.height(80.dp))
    }
}

@Composable
private fun KategoriPanganCard(
    kategori: KategoriPangan,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .height(126.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) PrimaryLightHover else Color.White,
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) Primary else Teal.copy(alpha = 0.6f),
        ),
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(PaleTeal),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = kategori.icon,
                    contentDescription = kategori.label,
                    tint = kategori.tint,
                    modifier = Modifier.size(28.dp),
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = kategori.label,
                fontFamily = Figtree,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                lineHeight = 15.sp,
                color = Ink,
                textAlign = TextAlign.Center,
                maxLines = 2,
            )
        }
    }
}

@Composable
private fun PanganSayaFoodCard(
    item: DonasiDonaturItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (badgeText, badgeColor) = when (item.tab) {
        PanganTab.AKTIF -> "Aktif" to Primary
        PanganTab.PROSES -> "Proses" to Color(0xFF2F88FF)
        PanganTab.SELESAI -> "Didonasikan" to Color(0xFFD96B27)
    }

    Box(
        modifier = modifier.padding(top = 12.dp),
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Teal.copy(alpha = 0.65f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
            ) {
                Image(
                    painter = painterResource(item.imageRes),
                    contentDescription = item.nama,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(114.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = item.kategoriLabel,
                    fontFamily = Figtree,
                    fontSize = 10.sp,
                    color = Muted,
                )
                Text(
                    text = item.nama,
                    fontFamily = Figtree,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = item.berat,
                    fontFamily = Figtree,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = DarkTeal,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Penerima Donasi",
                    fontFamily = Figtree,
                    fontSize = 10.sp,
                    color = Muted,
                )
                Spacer(Modifier.height(2.dp))
                if (item.namaPenerima == null) {
                    Text(
                        text = "Menunggu Konfirmasi",
                        fontFamily = Figtree,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color(0xFF2F88FF),
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Ink,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = item.namaPenerima,
                            fontFamily = Figtree,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            lineHeight = 14.sp,
                            color = Ink,
                        )
                    }
                }
            }
        }

        // Badge Status mengambang di bagian atas tengah kartu seperti di Figma
        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-11).dp),
            shape = RoundedCornerShape(20.dp),
            color = badgeColor,
            shadowElevation = 2.dp,
        ) {
            Text(
                text = badgeText,
                fontFamily = Figtree,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp),
            )
        }
    }
}

@Preview(name = "Pangan Saya - Tab Aktif", showBackground = true, widthDp = 402, heightDp = 874)
@Composable
private fun PanganSayaAktifPreview() {
    PanganSayaScreen(initialTab = PanganTab.AKTIF)
}

@Preview(name = "Pangan Saya - Tab Proses", showBackground = true, widthDp = 402, heightDp = 874)
@Composable
private fun PanganSayaProsesPreview() {
    PanganSayaScreen(initialTab = PanganTab.PROSES)
}

@Preview(name = "Pangan Saya - Tab Selesai", showBackground = true, widthDp = 402, heightDp = 874)
@Composable
private fun PanganSayaSelesaiPreview() {
    PanganSayaScreen(initialTab = PanganTab.SELESAI)
}
