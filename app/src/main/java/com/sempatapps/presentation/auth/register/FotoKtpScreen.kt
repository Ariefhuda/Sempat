package com.sempatapps.presentation.auth.register

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sempatapps.R
import com.sempatapps.core.designsystem.Body
import com.sempatapps.core.designsystem.Canvas
import com.sempatapps.core.designsystem.Heading2
import com.sempatapps.core.designsystem.Ink
import com.sempatapps.core.designsystem.Primary
import com.sempatapps.core.designsystem.PrimaryDark
import com.sempatapps.core.designsystem.PrimaryLightActive
import com.sempatapps.core.designsystem.ShadowFaint
import com.sempatapps.core.designsystem.Title
import com.sempatapps.core.designsystem.component.BackButton
import com.sempatapps.core.designsystem.component.ErrorBox
import com.sempatapps.core.designsystem.component.SecondaryButton

/**
 * Figma: "Daftar - Foto KTP (Donatur/Penerima)" dan "Daftar - KTP Tidak Terbaca".
 * Foto diambil pakai [KameraKtp] (ada bingkai bantuan), lalu dibaca di HP (tidak dikirim ke server).
 */
@Composable
fun FotoKtpScreen(
    viewModel: RegisterViewModel,
    onBackClick: () -> Unit,
    onKtpTerbaca: () -> Unit,
    onIsiManual: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    // rememberSaveable: kalau activity dibuat ulang (mis. HP diputar), kamera tetap terbuka.
    var kameraTerbuka by rememberSaveable { mutableStateOf(false) }
    val izinKamera = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { boleh ->
        if (boleh) {
            kameraTerbuka = true
        } else {
            Toast.makeText(context, "Izin kamera dibutuhkan untuk memotret KTP", Toast.LENGTH_LONG).show()
        }
    }

    if (kameraTerbuka) {
        val tutup = {
            kameraTerbuka = false
            viewModel.fotoDibatalkan()
        }
        BackHandler(onBack = tutup)
        KameraKtp(
            deteksi = viewModel::ktpDalamBingkai,
            buatUri = viewModel::uriFotoBaru,
            onFoto = { uri ->
                kameraTerbuka = false
                viewModel.bacaKtp(uri, onKtpTerbaca)
            },
            onTutup = tutup,
        )
        return
    }

    FotoKtpContent(
        statusScan = state.statusScan,
        onBackClick = onBackClick,
        onAmbilFoto = {
            val sudahBoleh = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
            if (sudahBoleh) kameraTerbuka = true else izinKamera.launch(Manifest.permission.CAMERA)
        },
        onIsiManual = {
            viewModel.isiDataSendiri()
            onIsiManual()
        },
    )
}

@Composable
private fun FotoKtpContent(
    statusScan: StatusScan,
    onBackClick: () -> Unit,
    onAmbilFoto: () -> Unit,
    onIsiManual: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Canvas)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(17.dp)) {
            BackButton(onBackClick = onBackClick)
            Text("Foto KTP", style = Heading2, color = Ink)
        }

        Spacer(Modifier.height(25.dp))
        Text("Foto KTP", style = Title, color = PrimaryDark)
        Text("Data dibaca di HP ini. Foto KTP tidak dikirim ke server.", style = Body, modifier = Modifier.width(254.dp))

        Spacer(Modifier.height(10.dp))
        KartuAmbilFoto(statusScan = statusScan, onClick = onAmbilFoto)

        if (statusScan == StatusScan.GAGAL) {
            Spacer(Modifier.height(20.dp))
            ErrorBox(
                title = "KTP belum terbaca.",
                message = "Pastikan semua tulisan kelihatan, tidak silau, dan KTP tidak terpotong.",
            )
            Spacer(Modifier.weight(1f))
            SecondaryButton(text = "Isi Data Sendiri", onClick = onIsiManual)
            Spacer(Modifier.height(20.dp))
        } else {
            Spacer(Modifier.height(40.dp))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PrimaryLightActive),
            ) {
                Text("Data akun terisi otomatis dari KTP", style = Body, color = PrimaryDark, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun KartuAmbilFoto(statusScan: StatusScan, onClick: () -> Unit) {
    val shape = RoundedCornerShape(24.dp)
    val membaca = statusScan == StatusScan.MEMBACA
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        modifier = Modifier
            .fillMaxWidth()
            .height(235.dp)
            .shadow(4.dp, shape, spotColor = ShadowFaint, ambientColor = ShadowFaint)
            .clip(shape)
            .background(Canvas)
            .border(1.dp, Primary, shape)
            .clickable(enabled = !membaca, role = Role.Button, onClick = onClick),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(width = 57.dp, height = 56.dp)
                .clip(CircleShape)
                .background(Primary),
        ) {
            if (membaca) {
                CircularProgressIndicator(color = Canvas, strokeWidth = 3.dp, modifier = Modifier.size(28.dp))
            } else {
                Image(painterResource(R.drawable.ic_camera), contentDescription = null)
            }
        }
        Text(
            text = when (statusScan) {
                StatusScan.BELUM -> "Ambil Foto KTP"
                StatusScan.MEMBACA -> "Membaca KTP..."
                StatusScan.GAGAL -> "Foto Ulang"
            },
            style = Title,
            color = Ink,
        )
    }
}

@Preview(widthDp = 402, heightDp = 874)
@Composable
private fun FotoKtpPreview() {
    FotoKtpContent(statusScan = StatusScan.BELUM, onBackClick = {}, onAmbilFoto = {}, onIsiManual = {})
}

@Preview(name = "KTP Tidak Terbaca", widthDp = 402, heightDp = 874)
@Composable
private fun KtpTidakTerbacaPreview() {
    FotoKtpContent(statusScan = StatusScan.GAGAL, onBackClick = {}, onAmbilFoto = {}, onIsiManual = {})
}
