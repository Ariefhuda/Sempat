 package com.sempatapps.presentation.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.zIndex
import com.sempatapps.R
import com.sempatapps.core.designsystem.*
import com.sempatapps.data.repository.DummyHomeRepository
import com.sempatapps.domain.model.*

// Beranda untuk DONATUR.

@Composable
internal fun DonorHome(
    data: HomeData,
    onNotices: () -> Unit,
    onFood: () -> Unit,
    onDonation: (Donation) -> Unit,
    onCreateDonation: () -> Unit
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Box(Modifier.fillMaxWidth().height(312.dp)) {
            Box(
                Modifier
                    .matchParentSize()
                    .background(Brush.verticalGradient(listOf(Color(0xFF3DA188), PrimaryNormal)))
            )
            Image(
                painter = painterResource(R.drawable.seller_hero),
                contentDescription = null,
                alignment = Alignment.TopCenter,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .statusBarsPadding()
                    .padding(end = 16.dp, top = 44.dp)
                    .size(185.dp, 224.dp),
                contentScale = ContentScale.Crop
            )
            Column(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 20.dp, vertical = 16.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        modifier = Modifier.width(24.dp).height(20.dp),
                        painter = painterResource(id = R.drawable.logo_putih),
                        contentDescription = "SemPat",
                        contentScale = ContentScale.FillBounds
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("SemPat", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    NoticeButton(data.notices.count { !it.isRead }, onNotices)
                }
                Spacer(Modifier.height(34.dp))
                Text("Halo ,", color = Color.White, fontSize = 20.sp, modifier = Modifier.padding(start = 18.dp))
                Text(
                    data.displayName,
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(start = 18.dp)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Yuk, kelola pangan\nsurplusmu hari ini.",
                    color = Color.White,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(start = 18.dp)
                )
            }
        }
        ImpactCard(
            data,
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .offset(y = (-42).dp)
                .zIndex(1f)
        )
        Column(
            Modifier
                .offset(y = (-22).dp)
                .padding(horizontal = 20.dp)
        ) {
            ScanCard(onCreateDonation)
            Spacer(Modifier.height(22.dp))
            SectionTitle("Mulai Berbagi")
            Spacer(Modifier.height(12.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(color = SecondaryDarkOrange)
                    .clickable(onClick = onCreateDonation)
            ) {
                Column(
                    Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 16.dp, top = 16.dp)
                ) {
                    Text(
                        text = "Donasi Pangan",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 27.sp
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Foto pangan berlebihmu,\nbiar sampai ke yang butuh.",
                        color = Color(0xEBFFFFFF),
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        modifier = Modifier.width(160.dp)
                    )
                }
                Image(
                    painter = painterResource(id = R.drawable.donation_illustration),
                    contentDescription = "Donasi Pangan",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 14.dp)
                        .width(156.dp)
                        .height(124.dp)
                )
            }
            Spacer(Modifier.height(22.dp))
            SectionHeading("Aktivitas Terakhir", onFood)
            Spacer(Modifier.height(10.dp))
        }
        DonorActivityRow(data.donations, onDonation = onDonation)
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun ImpactCard(data: HomeData, modifier: Modifier = Modifier) {
    Card(
        modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryLight),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(Modifier.padding(horizontal = 18.dp, vertical = 14.dp)) {
            Text("Dampakmu Hari Ini", color = MutedDark, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ImpactValue(R.drawable.money_bag, data.donatedWeight, "Didonasikan", Modifier.weight(1f))
                ImpactValue(R.drawable.donation, data.sharedCount, "Sudah Diambil", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ImpactValue(symbol: Int, value: String, label: String, modifier: Modifier) {
    val parts = value.trim().split(" ", limit = 2)
    Surface(modifier, shape = RoundedCornerShape(6.dp), color = Canvas, shadowElevation = 2.dp) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(symbol),
                contentDescription = null,
                modifier = Modifier.size(26.dp),
            )
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)) {
                            append(parts.firstOrNull().orEmpty())
                        }
                        if (parts.size > 1) {
                            append(" ")
                            withStyle(SpanStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold)) {
                                append(parts[1])
                            }
                        }
                    },
                    color = PrimaryDark,
                    maxLines = 1
                )
                Text(label, color = Ink, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun ScanCard(onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 164.dp)
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    "Cek pangan dengan AI",
                    color = PrimaryDark,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 22.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Foto panganmu dan dapatkan rekomendasi dalam hitungan detik",
                    color = Ink,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onClick,
                    modifier = Modifier.height(44.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryNormal,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        "Coba Sekarang",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            Image(
                painterResource(R.drawable.scan_illustration),
                contentDescription = null,
                modifier = Modifier.size(112.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}


@Preview(showBackground = true, widthDp = 402, heightDp = 874)
@Composable
private fun DonorHomePreview() {
    val repository = remember { DummyHomeRepository() }
    HomeApp(HomeRole.DONOR, repository)
}
