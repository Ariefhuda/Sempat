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

// Kerangka beranda: mengatur pindah halaman dan memanggil HomeDonatur / HomePembeli.

private enum class HomePage { HOME, NOTIFICATIONS, LOCATIONS, FOOD, CHAT }

private data class ChatMessage(val text: String, val fromUser: Boolean)

@Composable
fun HomeApp(role: HomeRole, repository: HomeRepository) {
    val data by repository.observe(role).collectAsState()
    var page by rememberSaveable { mutableStateOf(HomePage.HOME) }
    var selectedDonation by remember { mutableStateOf<Donation?>(null) }
    var showHelp by remember { mutableStateOf(false) }
    var showFaq by remember { mutableStateOf(false) }
    val chatMessages = remember { mutableStateListOf(ChatMessage("Halo! Aku Yuk, asisten SEMPAT. Ada yang ingin kamu tanyakan tentang donasi pangan?", false)) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val outsideHome: (String) -> Unit = { name ->
        scope.launch { snackbar.showSnackbar("Alur $name disambungkan oleh modul terkait.") }
    }

    MaterialTheme(typography = Typography()) {
        Box(Modifier.fillMaxSize().background(Canvas)) {
            Box(Modifier.fillMaxSize().padding(bottom = 94.dp)) {
                when (page) {
                    HomePage.HOME -> if (role == HomeRole.DONOR) {
                        DonorHome(
                            data = data,
                            onNotices = { page = HomePage.NOTIFICATIONS },
                            onFood = { page = HomePage.FOOD },
                            onDonation = { selectedDonation = it },
                            onCreateDonation = { outsideHome("donasi pangan") }
                        )
                    } else {
                        RecipientHome(
                            data = data,
                            onNotices = { page = HomePage.NOTIFICATIONS },
                            onLocations = { page = HomePage.LOCATIONS },
                            onFood = { page = HomePage.FOOD },
                            onDonation = { selectedDonation = it },
                            onHelp = { showHelp = true }
                        )
                    }
                    HomePage.NOTIFICATIONS -> NoticesPage(data, onBack = { page = HomePage.HOME }) {
                        repository.markNoticeRead(role, it)
                    }
                    HomePage.LOCATIONS -> LocationsPage(data, onBack = { page = HomePage.HOME }) {
                        repository.selectLocation(it)
                        page = HomePage.HOME
                    }
                    HomePage.FOOD -> FoodPage(data, onBack = { page = HomePage.HOME }) {
                        selectedDonation = it
                    }
                    HomePage.CHAT -> ChatPage(chatMessages, onBack = { page = HomePage.HOME }) { question ->
                        chatMessages.add(ChatMessage(question, true))
                        chatMessages.add(ChatMessage(chatReply(question), false))
                    }
                }
                if (page == HomePage.HOME && role == HomeRole.RECIPIENT) {
                    Button(
                        onClick = { page = HomePage.CHAT },
                        modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Teal),
                        shape = RoundedCornerShape(32.dp)
                    ) {
                        Text("✦  Tanya Yuk", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            HomeBottomBar(
                role = role,
                activePage = page,
                onHome = { page = HomePage.HOME },
                onFood = { page = HomePage.FOOD },
                onOutsideHome = outsideHome,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
            SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).padding(bottom = 94.dp))
        }
    }

    selectedDonation?.let { donation ->
        DonationDialog(donation, onDismiss = { selectedDonation = null })
    }
    if (showHelp) {
        AlertDialog(
            onDismissRequest = { showHelp = false },
            title = { Text("Butuh bantuan pangan?") },
            text = { Text("Aktifkan permintaan bantuan untuk melihat donasi yang tersedia di sekitarmu. Tidak perlu mengunggah dokumen.") },
            confirmButton = {
                TextButton(onClick = {
                    repository.setNeedsHelp(!data.needsHelp)
                    showHelp = false
                }) { Text(if (data.needsHelp) "Nonaktifkan" else "Aktifkan", color = DarkTeal) }
            },
            dismissButton = { TextButton(onClick = { showHelp = false }) { Text("Kembali") } }
        )
    }
}

@Composable
private fun ChatPage(messages: List<ChatMessage>, onBack: () -> Unit, onSend: (String) -> Unit) {
    var draft by rememberSaveable { mutableStateOf("") }
    val send = {
        val text = draft.trim()
        if (text.isNotEmpty()) {
            onSend(text)
            draft = ""
        }
    }
    Column(Modifier.fillMaxSize().imePadding().padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(16.dp))
        PageTitle("Tanya Yuk", onBack)
        Text("Asisten donasi pangan SEMPAT", color = Muted, fontSize = 13.sp, modifier = Modifier.padding(start = 58.dp))
        Spacer(Modifier.height(20.dp))
        Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            messages.forEach { message ->
                Surface(
                    modifier = Modifier.align(if (message.fromUser) Alignment.End else Alignment.Start).widthIn(max = 290.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = if (message.fromUser) Teal else PaleTeal
                ) {
                    Text(message.text, modifier = Modifier.padding(14.dp), color = if (message.fromUser) Color.White else Ink, fontSize = 14.sp, lineHeight = 20.sp)
                }
            }
            Spacer(Modifier.height(4.dp))
        }
        Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextField(
                value = draft,
                onValueChange = { draft = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Tulis pertanyaan...") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { send() }),
                shape = RoundedCornerShape(24.dp)
            )
            Button(onClick = send, enabled = draft.isNotBlank(), colors = ButtonDefaults.buttonColors(containerColor = Teal), contentPadding = PaddingValues(horizontal = 14.dp)) {
                Text("Kirim")
            }
        }
    }
}

private fun chatReply(question: String): String = when {
    question.contains("lokasi", ignoreCase = true) -> "Pilih lokasi lewat kartu lokasi di beranda agar pangan terdekat sesuai pilihanmu."
    question.contains("ambil", ignoreCase = true) || question.contains("pesan", ignoreCase = true) -> "Pilih pangan yang tersedia, lalu ambil sendiri di lokasi yang tertera pada detail donasi."
    question.contains("dokumen", ignoreCase = true) || question.contains("syarat", ignoreCase = true) -> "Kamu bisa mengakses donasi pangan tanpa mengunggah dokumen berat."
    question.contains("donasi", ignoreCase = true) || question.contains("pangan", ignoreCase = true) -> "Semua pangan yang tampil di SEMPAT gratis. Buka detail pangan untuk melihat jumlah dan lokasi pengambilan."
    else -> "Aku bisa bantu menjelaskan donasi pangan, lokasi, syarat, dan cara mengambil pangan di SEMPAT."
}

@Composable
private fun NoticesPage(data: HomeData, onBack: () -> Unit, onRead: (String) -> Unit) {
    var expandedId by rememberSaveable { mutableStateOf<String?>(null) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        PageTitle("Notifikasi", onBack)
        Spacer(Modifier.height(20.dp))
        data.notices.groupBy { it.day }.forEach { (day, notices) ->
            Text(day, color = Muted, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            notices.forEach { notice ->
                NoticeCard(notice, expanded = expandedId == notice.id) {
                    expandedId = if (expandedId == notice.id) null else notice.id
                    onRead(notice.id)
                }
                Spacer(Modifier.height(10.dp))
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun NoticeCard(notice: HomeNotice, expanded: Boolean, onClick: () -> Unit) {
    Surface(Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(12.dp), border = androidx.compose.foundation.BorderStroke(1.dp, if (notice.isRead) PaleTeal else Teal), color = Canvas, shadowElevation = 2.dp) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("♡", color = Teal, fontSize = 31.sp)
            Spacer(Modifier.width(16.dp))
            Column {
                Text(notice.title, color = DarkTeal, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(notice.message, color = Ink, fontSize = 13.sp, lineHeight = 19.sp, maxLines = if (expanded) Int.MAX_VALUE else 1, overflow = TextOverflow.Ellipsis)
                Text(if (expanded) "Sudah dibaca" else "Ketuk untuk membaca", color = Muted, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun LocationsPage(data: HomeData, onBack: () -> Unit, onSelect: (String) -> Unit) {
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        PageTitle("Pilih Lokasi", onBack)
        Spacer(Modifier.height(20.dp))
        data.locations.forEach { location ->
            Surface(Modifier.fillMaxWidth().padding(bottom = 12.dp).clickable { onSelect(location.id) }, shape = RoundedCornerShape(12.dp), color = Canvas, border = androidx.compose.foundation.BorderStroke(1.dp, Teal), shadowElevation = 2.dp) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(if (location.id == data.selectedLocationId) "◉" else "○", color = Orange, fontSize = 24.sp)
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(location.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(location.address, color = Muted, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun FoodPage(data: HomeData, onBack: () -> Unit, onDonation: (Donation) -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        PageTitle(if (data.role == HomeRole.DONOR) "Pangan Saya" else "Donasi Terdekat", onBack)
        Spacer(Modifier.height(20.dp))
        data.donations.forEach { donation ->
            RecommendationCard(donation) { onDonation(donation) }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun DonationDialog(donation: Donation, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(Modifier.padding(18.dp)) {
                Image(painterResource(photoRes(donation.imageName)), contentDescription = donation.name, modifier = Modifier.fillMaxWidth().height(168.dp).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Crop)
                Spacer(Modifier.height(13.dp))
                Text(donation.name, fontWeight = FontWeight.Bold, fontSize = 21.sp)
                Text("${donation.category} · ${donation.quantity}", color = Muted)
                Spacer(Modifier.height(9.dp))
                Text("Gratis · Ambil sendiri", color = DarkTeal, fontWeight = FontWeight.Bold)
                Text("Lokasi: ${donation.pickupAddress}", color = Ink, fontSize = 13.sp)
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) { Text("Tutup", color = DarkTeal) }
            }
        }
    }
}

@Composable
private fun HomeBottomBar(role: HomeRole, activePage: HomePage, onHome: () -> Unit, onFood: () -> Unit, onOutsideHome: (String) -> Unit, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().background(Canvas).navigationBarsPadding().padding(horizontal = 20.dp, vertical = 6.dp)) {
        Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(38.dp), color = Canvas, shadowElevation = 9.dp) {
            Row(Modifier.fillMaxWidth().height(70.dp), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
                NavItem(R.drawable.ic_home, "Beranda", activePage == HomePage.HOME, onHome)
                NavItem(R.drawable.ic_food, "Pangan", activePage == HomePage.FOOD, onFood)
                if (role == HomeRole.RECIPIENT) NavItem(R.drawable.ic_order, "Pesanan", false) { onOutsideHome("pesanan") }
                NavItem(R.drawable.ic_profile, "Profil", false) { onOutsideHome("profil") }
            }
        }
    }
}

@Composable
private fun NavItem(icon: Int, label: String, active: Boolean, onClick: () -> Unit) {
    Column(Modifier.widthIn(min = 58.dp).clickable(onClick = onClick), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(if (active) 44.dp else 32.dp).background(if (active) PaleTeal else Color.Transparent, CircleShape), contentAlignment = Alignment.Center) {
            Image(
                painterResource(icon),
                contentDescription = label,
                modifier = Modifier.size(if (active) 29.dp else 25.dp),
                colorFilter = ColorFilter.tint(if (active) Color(0xFF09453E) else Muted)
            )
        }
        if (!active) Text(label, color = Muted, fontSize = 11.sp)
    }
}

