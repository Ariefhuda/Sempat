package com.sempatapps

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import com.sempatapps.home.Donation
import com.sempatapps.home.DummyHomeRepository
import com.sempatapps.home.HomeData
import com.sempatapps.home.HomeNotice
import com.sempatapps.home.HomeRepository
import com.sempatapps.home.HomeRole
import kotlinx.coroutines.launch

private val Teal = Color(0xFF19C6B2)
private val DarkTeal = Color(0xFF139586)
private val PaleTeal = Color(0xFFE8F9F7)
private val Ink = Color(0xFF212121)
private val Muted = Color(0xFF7D7B7B)
private val Canvas = Color(0xFFF9FFFE)
private val Orange = Color(0xFFFF861C)

private enum class HomePage { HOME, NOTIFICATIONS, LOCATIONS, FOOD, CHAT }

private data class ChatMessage(val text: String, val fromUser: Boolean)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val role = if (intent.getStringExtra("home_role") == "recipient") HomeRole.RECIPIENT else HomeRole.DONOR
        setContent {
            val repository = remember { DummyHomeRepository() }
            HomeApp(role = role, repository = repository)
        }
    }
}

@Composable
private fun HomeApp(role: HomeRole, repository: HomeRepository) {
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
private fun DonorHome(
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
private fun RecipientHome(
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
private fun NoticeButton(count: Int, onClick: () -> Unit) {
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

@Composable
private fun DonationRow(donations: List<Donation>, showStatus: Boolean, onDonation: (Donation) -> Unit) {
    Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        donations.forEach { donation ->
            DonationCard(donation, showStatus) { onDonation(donation) }
        }
    }
}

@Composable
private fun DonationCard(donation: Donation, showStatus: Boolean, onClick: () -> Unit) {
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
private fun RecommendationCard(donation: Donation, onClick: () -> Unit) {
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
private fun PageTitle(title: String, onBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(44.dp).background(PaleTeal, CircleShape).clickable(onClick = onBack), contentAlignment = Alignment.Center) {
            Text("‹", color = DarkTeal, fontSize = 33.sp)
        }
        Spacer(Modifier.width(14.dp))
        Text(title, color = Ink, fontSize = 23.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(title, color = Ink, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
}

@Composable
private fun SectionHeading(title: String, onAll: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, color = Ink, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
        Text("Lihat Semua", color = DarkTeal, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable(onClick = onAll))
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

private fun photoRes(name: String): Int = when (name) {
    "bread_photo_alt" -> R.drawable.bread_photo_alt
    else -> R.drawable.bread_photo
}

@Preview(showBackground = true, widthDp = 402, heightDp = 874)
@Composable
private fun DonorHomePreview() {
    val repository = remember { DummyHomeRepository() }
    HomeApp(HomeRole.DONOR, repository)
}

@Preview(showBackground = true, widthDp = 402, heightDp = 874)
@Composable
private fun RecipientHomePreview() {
    val repository = remember { DummyHomeRepository() }
    HomeApp(HomeRole.RECIPIENT, repository)
}
