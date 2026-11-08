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

// Komponen yang dipakai di beranda donatur maupun penerima.

@Composable
internal fun NoticeButton(count: Int, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = Modifier.size(48.dp), shape = CircleShape, color = Color.White, shadowElevation = 3.dp) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Image(painterResource(R.drawable.ic_bell), contentDescription = "Buka notifikasi", modifier = Modifier.size(24.dp))
        if (count > 0) {
                Box(Modifier.align(Alignment.TopEnd).offset(x = 3.dp, y = (-3).dp).size(10.dp).background(Orange, CircleShape))
            }
        }
    }
}

@Composable
internal fun DonationRow(donations: List<Donation>, showStatus: Boolean, onDonation: (Donation) -> Unit) {
    Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        donations.forEach { donation ->
            DonationCard(donation, showStatus) { onDonation(donation) }
        }
    }
}

@Composable
internal fun DonationCard(donation: Donation, showStatus: Boolean, onClick: () -> Unit) {
    Card(Modifier.width(174.dp).clickable(onClick = onClick), shape = RoundedCornerShape(13.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(3.dp)) {
        Column {
            Box {
                Image(painterResource(photoRes(donation.imageName)), contentDescription = donation.name, modifier = Modifier.fillMaxWidth().height(132.dp), contentScale = ContentScale.Crop)
                Surface(Modifier.align(Alignment.TopEnd).padding(5.dp), color = Orange, shape = RoundedCornerShape(11.dp)) {
                    Text(if (showStatus) donation.status else "◆ ${donation.distance}", modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp), color = Color.White, fontSize = 11.sp)
                }
            }
            Column(Modifier.padding(9.dp)) {
                Text(donation.category, color = Muted, fontSize = 11.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(donation.name, color = Ink, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1, modifier = Modifier.weight(1f))
                    Text(donation.quantity, color = DarkTeal, fontSize = 11.sp)
                }
                Spacer(Modifier.height(6.dp))
                Text("Ambil sendiri", color = Muted, fontSize = 11.sp)
                Text("Gratis", color = DarkTeal, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
internal fun RecommendationCard(donation: Donation, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(13.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(painterResource(photoRes(donation.imageName)), contentDescription = donation.name, modifier = Modifier.size(106.dp).clip(RoundedCornerShape(10.dp)), contentScale = ContentScale.Crop)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(donation.category, color = Muted, fontSize = 11.sp)
                Text(donation.name, color = Ink, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text(donation.quantity, color = DarkTeal, fontSize = 12.sp)
                Spacer(Modifier.height(5.dp))
                Text("Gratis · Ambil sendiri", color = DarkTeal, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
internal fun PageTitle(title: String, onBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(44.dp).background(PaleTeal, CircleShape).clickable(onClick = onBack), contentAlignment = Alignment.Center) {
            Text("‹", color = DarkTeal, fontSize = 33.sp)
        }
        Spacer(Modifier.width(14.dp))
        Text(title, color = Ink, fontSize = 23.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
internal fun SectionTitle(title: String) {
    Text(title, color = Ink, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
}

@Composable
internal fun SectionHeading(title: String, onAll: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, color = Ink, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
        Text("Lihat Semua", color = DarkTeal, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable(onClick = onAll))
    }
}

internal fun photoRes(name: String): Int = when (name) {
    "bread_photo_alt" -> R.drawable.bread_photo_alt
    else -> R.drawable.bread_photo
}
