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
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch
import com.sempatapps.R
import com.sempatapps.core.designsystem.*
import com.sempatapps.data.repository.DummyHomeRepository
import com.sempatapps.data.repository.HomeRepository
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
        Box(Modifier.fillMaxWidth().height(286.dp)) {
            Box(Modifier.fillMaxWidth().height(286.dp).matchParentSize().background(Brush.verticalGradient(listOf(Color(0xFF3DA188), Color(0xFF0E7C65)))))
            Image(
                painter = painterResource(R.drawable.seller_hero),
                contentDescription = null,
                alignment = Alignment.TopCenter,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .statusBarsPadding()
                    .padding(end = 20.dp, top = 40.dp)
                    .size(200.dp, 230.dp),
                contentScale = ContentScale.Crop
            )
            Column(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 25.dp, vertical = 18.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Image(modifier = Modifier.width(20.dp).height(16.dp),
                        painter = painterResource(id = R.drawable.logo_putih),
                        contentDescription = "image description",
                        contentScale = ContentScale.FillBounds
                    )
                    Text("SemPat", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    NoticeButton(data.notices.count { !it.isRead }, onNotices)
                }
                Spacer(Modifier.height(38.dp))
                Text("Halo,", color = Color.White, fontSize = 21.sp)
                Text(data.displayName, color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.ExtraBold)
                Text("Yuk, salurkan pangan\nsurplusmu hari ini.", color = Color.White, fontSize = 15.sp, lineHeight = 22.sp)
            }
        }
        ImpactCard(
            data,
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .offset(y = (-52).dp)
                .zIndex(1f)
        )
        Column(Modifier.padding(horizontal = 20.dp)) {
            ScanCard(onCreateDonation)
            Spacer(Modifier.height(25.dp))
            SectionTitle("Mulai Berbagi")
            Spacer(Modifier.height(10.dp))
            Box(
                Modifier
                    .width(360.dp)
                    .height(170.dp)
                    .background(color = Color(0xFFB84E08), shape = RoundedCornerShape(size = 24.dp))
            ) {
                Column(
                    Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 16.dp, top = 16.dp)
                ) {
                    Text(
                        text = "Donasi Pangan",
                        modifier = Modifier.width(122.dp).height(27.dp),
                        style = TextStyle(
                            fontSize = 18.sp,
                            lineHeight = 27.sp,
                            fontFamily = FontFamily(Font(R.font.figtree)),
                            fontWeight = FontWeight(700),
                            color = Color(0xFFFFFFFF),
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Foto pangan berlebihmu,\nbiar sampai ke yang butuh.",
                        modifier = Modifier.width(160.dp),
                        style = TextStyle(
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            fontFamily = FontFamily(Font(R.font.figtree)),
                            fontWeight = FontWeight(400),
                            color = Color(0xEBFFFFFF),
                        )
                    )
                }
                Image(
                    painter = painterResource(id = R.drawable.donation_illustration),
                    contentDescription = "image description",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp)
                        .width(160.dp)
                        .height(127.8148.dp)
                )
            }
            Spacer(Modifier.height(25.dp))
            SectionHeading("Aktivitas Terakhir", onFood)
            Spacer(Modifier.height(12.dp))
        }
        DonationRow(data.donations, showStatus = true, onDonation = onDonation)
        Spacer(Modifier.height(22.dp))
    }
}

@Composable
private fun ImpactCard(data: HomeData, modifier: Modifier = Modifier) {
    Card(modifier.fillMaxWidth(), shape = RoundedCornerShape(15.dp), colors = CardDefaults.cardColors(containerColor = PaleTeal), elevation = CardDefaults.cardElevation(4.dp)) {
        Column(Modifier.padding(17.dp)) {
            Text("Dampakmu Hari Ini", color = Muted, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(7.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                ImpactValue(R.drawable.money_bag, data.donatedWeight, "Terdonasikan", Modifier.weight(1f))
                ImpactValue(R.drawable.donation,   data.sharedCount,  "Pangan tersalur", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ImpactValue(symbol: Int, value: String, label: String, modifier: Modifier) {
    Surface(modifier, shape = RoundedCornerShape(8.dp), color = Color.White, shadowElevation = 2.dp) {
        Row(Modifier.padding(horizontal = 8.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(symbol),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
            Spacer(Modifier.width(6.dp))
            Column {
                Text(value, color = DarkTeal, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
                Text(label, color = Muted, fontSize = 10.sp, maxLines = 1)
            }
        }
    }
}

@Composable
private fun ScanCard(onClick: () -> Unit) {
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Canvas), elevation = CardDefaults.cardElevation(3.dp)) {
        Row(Modifier.fillMaxWidth().height(164.dp).padding(11.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Cek pangan dengan AI", color = DarkTeal, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(3.dp))
                Text("Foto panganmu sebelum didonasikan", color = Ink, fontSize = 12.sp, lineHeight = 17.sp)
                Spacer(Modifier.height(9.dp))
                Button(onClick = onClick, colors = ButtonDefaults.buttonColors(containerColor = Teal), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 5.dp), shape = RoundedCornerShape(24.dp)) {
                    Text("Coba Sekarang", fontWeight = FontWeight.Bold)
                }
            }
            Image(painterResource(R.drawable.scan_illustration), contentDescription = null, modifier = Modifier.size(120.dp), contentScale = ContentScale.Fit)
        }
    }
}


@Preview(showBackground = true, widthDp = 402, heightDp = 874)
@Composable
private fun DonorHomePreview() {
    val repository = remember { DummyHomeRepository() }
    HomeApp(HomeRole.DONOR, repository)
}
