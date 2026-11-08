package com.sempatapps.presentation.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        Box(Modifier.fillMaxWidth().height(314.dp)) {
            Box(Modifier.fillMaxWidth().height(286.dp).background(Brush.verticalGradient(listOf(Color(0xFF6EFFEE), Teal))))
            Image(
                painter = painterResource(R.drawable.seller_hero),
                contentDescription = null,
                modifier = Modifier.align(Alignment.TopEnd).padding(top = 76.dp, end = 20.dp).size(158.dp, 225.dp),
                contentScale = ContentScale.Fit
            )
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("SemPat", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    NoticeButton(data.notices.count { !it.isRead }, onNotices)
                }
                Spacer(Modifier.height(38.dp))
                Text("Halo,", color = Color.White, fontSize = 21.sp)
                Text(data.displayName, color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.ExtraBold)
                Text("Yuk, salurkan pangan\nsurplusmu hari ini.", color = Color.White, fontSize = 15.sp, lineHeight = 22.sp)
            }
            ImpactCard(data, Modifier.align(Alignment.BottomCenter).padding(horizontal = 20.dp))
        }
        Column(Modifier.padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(24.dp))
            ScanCard(onCreateDonation)
            Spacer(Modifier.height(25.dp))
            SectionTitle("Opsi Utama")
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.weight(1f)) {
                    ActionTile("Donasi Pangan", Orange, R.drawable.donation_illustration, onCreateDonation)
                }
                Box(Modifier.weight(1f)) {
                    ActionTile("Pangan Saya", Teal, R.drawable.bread_photo, onFood)
                }
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
                ImpactValue("♧", data.donatedWeight, "Terdonasikan", Modifier.weight(1f))
                ImpactValue("♡", data.sharedCount, "Pangan tersalur", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ImpactValue(symbol: String, value: String, label: String, modifier: Modifier) {
    Surface(modifier, shape = RoundedCornerShape(8.dp), color = Color.White, shadowElevation = 2.dp) {
        Row(Modifier.padding(horizontal = 8.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(symbol, color = Teal, fontSize = 24.sp)
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

@Composable
private fun ActionTile(title: String, background: Color, image: Int, onClick: () -> Unit) {
    Box(Modifier.fillMaxWidth().height(170.dp).clip(RoundedCornerShape(22.dp)).background(background).clickable(onClick = onClick)) {
        Text(title, modifier = Modifier.padding(14.dp), color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Image(painterResource(image), contentDescription = null, modifier = Modifier.align(Alignment.BottomEnd).size(128.dp, 116.dp), contentScale = ContentScale.Fit)
    }
}

@Preview(showBackground = true, widthDp = 402, heightDp = 874)
@Composable
private fun DonorHomePreview() {
    val repository = remember { DummyHomeRepository() }
    HomeApp(HomeRole.DONOR, repository)
}
