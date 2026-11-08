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
import kotlinx.coroutines.launch
import com.sempatapps.R
import com.sempatapps.core.designsystem.*
import com.sempatapps.data.repository.DummyHomeRepository
import com.sempatapps.data.repository.HomeRepository
import com.sempatapps.domain.model.*

// Beranda untuk PENERIMA (pembeli).

@Composable
internal fun RecipientHome(
    data: HomeData,
    onNotices: () -> Unit,
    onLocations: () -> Unit,
    onFood: () -> Unit,
    onDonation: (Donation) -> Unit,
    onHelp: () -> Unit
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(painterResource(R.drawable.sempat_logo), contentDescription = "SemPat", modifier = Modifier.width(88.dp).height(24.dp), contentScale = ContentScale.Fit)
            Spacer(Modifier.weight(1f))
            NoticeButton(data.notices.count { !it.isRead }, onNotices)
        }
        Box(Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(238.dp).clip(RoundedCornerShape(24.dp)).background(Brush.linearGradient(listOf(Color(0xFF6EFFEE), Teal)))) {
            Image(painterResource(R.drawable.recipient_hero), contentDescription = null, modifier = Modifier.align(Alignment.BottomStart).size(244.dp, 224.dp), contentScale = ContentScale.Fit)
            Column(Modifier.align(Alignment.CenterEnd).width(166.dp).padding(end = 20.dp), horizontalAlignment = Alignment.End) {
                Text("Halo", color = Color.White, fontSize = 18.sp)
                Text(data.displayName, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                Text("Temukan pangan gratis di sekitarmu", color = Color.White, textAlign = TextAlign.End, fontSize = 15.sp, lineHeight = 21.sp)
            }
        }
        LocationCard(
            data.selectedLocation?.title.orEmpty(),
            data.selectedLocation?.address.orEmpty(),
            onLocations,
            Modifier.padding(horizontal = 38.dp).offset(y = (-17).dp)
        )
        Column(Modifier.padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(8.dp))
            SectionHeading("Pangan Terdekat", onFood)
            Spacer(Modifier.height(12.dp))
        }
        DonationRow(data.donations, showStatus = false, onDonation = onDonation)
        Spacer(Modifier.height(22.dp))
        Column(Modifier.padding(horizontal = 20.dp)) {
            HelpCard(data.needsHelp, onHelp)
            Spacer(Modifier.height(26.dp))
            SectionHeading("Rekomendasi Pangan", onFood)
            Spacer(Modifier.height(12.dp))
            data.donations.forEach { donation ->
                RecommendationCard(donation) { onDonation(donation) }
                Spacer(Modifier.height(10.dp))
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun LocationCard(title: String, address: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(13.dp), color = Color.White, shadowElevation = 4.dp, border = androidx.compose.foundation.BorderStroke(1.dp, Teal)) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(painterResource(R.drawable.icon_location), contentDescription = null, modifier = Modifier.size(25.dp, 36.dp), contentScale = ContentScale.Fit)
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = Ink, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(address, color = Muted, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text("›", color = DarkTeal, fontSize = 28.sp)
        }
    }
}

@Composable
private fun HelpCard(active: Boolean, onClick: () -> Unit) {
    Card(shape = RoundedCornerShape(17.dp), colors = CardDefaults.cardColors(containerColor = Canvas), elevation = CardDefaults.cardElevation(3.dp)) {
        Row(Modifier.fillMaxWidth().height(164.dp).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Butuh Bantuan Pangan?", color = DarkTeal, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text("Dapatkan kabar donasi terdekat tanpa unggah dokumen", color = Ink, fontSize = 12.sp, lineHeight = 17.sp)
                Spacer(Modifier.height(8.dp))
                Button(onClick = onClick, colors = ButtonDefaults.buttonColors(containerColor = Teal), shape = RoundedCornerShape(24.dp), contentPadding = PaddingValues(horizontal = 14.dp)) {
                    Text(if (active) "Bantuan Aktif" else "Minta Bantuan", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
            Image(painterResource(R.drawable.donation_illustration), contentDescription = null, modifier = Modifier.size(111.dp), contentScale = ContentScale.Fit)
        }
    }
}

@Preview(showBackground = true, widthDp = 402, heightDp = 874)
@Composable
private fun RecipientHomePreview() {
    val repository = remember { DummyHomeRepository() }
    HomeApp(HomeRole.RECIPIENT, repository)
}
