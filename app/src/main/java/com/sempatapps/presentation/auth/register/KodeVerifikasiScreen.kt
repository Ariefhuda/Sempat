package com.sempatapps.presentation.auth.register

import android.os.SystemClock
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sempatapps.R
import com.sempatapps.core.designsystem.Body
import com.sempatapps.core.designsystem.BodyBold
import com.sempatapps.core.designsystem.Canvas
import com.sempatapps.core.designsystem.ErrorBorder
import com.sempatapps.core.designsystem.Heading1
import com.sempatapps.core.designsystem.Heading2
import com.sempatapps.core.designsystem.Heading3
import com.sempatapps.core.designsystem.Ink
import com.sempatapps.core.designsystem.Muted
import com.sempatapps.core.designsystem.Primary
import com.sempatapps.core.designsystem.ShadowCard
import com.sempatapps.core.designsystem.Sora
import com.sempatapps.core.designsystem.component.BackButton
import com.sempatapps.core.designsystem.component.ErrorBox
import com.sempatapps.core.designsystem.component.InfoBox
import com.sempatapps.core.designsystem.component.PrimaryButton
import com.sempatapps.core.designsystem.component.SempatDialog
import kotlinx.coroutines.delay

/** Figma: "Daftar Donatur/Penerima - Kode Verifikasi" + "Pop up Kirim Ulang OTP". */
@Composable
fun KodeVerifikasiScreen(
    viewModel: RegisterViewModel,
    onBackClick: () -> Unit,
    onKodeBenar: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var dialogTerkirim by rememberSaveable { mutableStateOf(false) }

    // Hitung mundur "Kirim ulang dalam 00:59"
    var sisaDetik by remember { mutableLongStateOf(0L) }
    LaunchedEffect(state.bisaKirimUlangPada) {
        while (true) {
            sisaDetik = ((state.bisaKirimUlangPada - SystemClock.elapsedRealtime()) / 1000L).coerceAtLeast(0L)
            if (sisaDetik == 0L) break
            delay(1000L)
        }
    }
    LaunchedEffect(Unit) { viewModel.hapusError() }

    KodeVerifikasiContent(
        noHp = state.data.noHp,
        kode = state.kode,
        terisiOtomatis = state.kodeTerisiOtomatis,
        sisaDetik = sisaDetik,
        sedangProses = state.sedangProses,
        pesanError = state.pesanError,
        onKodeChange = viewModel::ubahKode,
        onKirimUlang = { viewModel.kirimUlang { dialogTerkirim = true } },
        onLanjut = { viewModel.cekKode(onKodeBenar) },
        onBackClick = onBackClick,
    )

    if (dialogTerkirim) {
        SempatDialog(
            illustrationRes = R.drawable.ilustrasi_kode_terkirim,
            title = "Kode Terkirim",
            message = "Kode sebelumnya akan diganti dengan kode baru. Pastikan nomor HP kamu masih aktif",
            primaryText = "Oke",
            onPrimary = { dialogTerkirim = false },
            onDismiss = { dialogTerkirim = false },
        )
    }
}

@Composable
private fun KodeVerifikasiContent(
    noHp: String,
    kode: String,
    terisiOtomatis: Boolean,
    sisaDetik: Long,
    sedangProses: Boolean,
    pesanError: String?,
    onKodeChange: (String) -> Unit,
    onKirimUlang: () -> Unit,
    onLanjut: () -> Unit,
    onBackClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Canvas)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp),
    ) {
        JudulDaftar(onBackClick)
        Spacer(Modifier.height(16.dp))
        Text("Cek Nomor HP", style = Heading2, color = Primary)
        Spacer(Modifier.height(2.dp))
        Text("Kode 6 angka sudah dikirim lewat WhatsApp ke ${samarkanNoHp(noHp)}", style = BodyBold, color = Muted)

        Spacer(Modifier.height(32.dp))
        val shape = RoundedCornerShape(12.dp)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .shadow(18.dp, shape, spotColor = ShadowCard, ambientColor = ShadowCard)
                .clip(shape)
                .background(Canvas)
                .padding(horizontal = 28.dp, vertical = 16.dp),
        ) {
            Text("Kode 6 angka", style = Heading3, color = Color.Black)
            Spacer(Modifier.height(24.dp))
            KotakKode(kode = kode, salah = pesanError != null, onChange = onKodeChange)
            Spacer(Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (sisaDetik > 0) "Kirim ulang dalam 00:%02d".format(sisaDetik) else "Belum dapat kode?",
                    style = Body,
                    color = Ink,
                    modifier = Modifier.weight(1f),
                )
                val bisaKirim = sisaDetik == 0L && !sedangProses
                Box(
                    contentAlignment = Alignment.CenterEnd,
                    modifier = Modifier
                        .heightIn(min = 44.dp)
                        .clickable(enabled = bisaKirim, role = Role.Button, onClick = onKirimUlang),
                ) {
                    Text("Kirim Ulang", style = BodyBold, color = if (bisaKirim) Primary else Color(0xFF8A9490), textAlign = TextAlign.End)
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        when {
            pesanError != null -> ErrorBox(title = pesanError, message = null)
            terisiOtomatis -> InfoBox(R.drawable.ic_sparkle, "Kode sudah terisi otomatis ✓", textStyle = BodyBold.copy(fontSize = 14.sp))
        }

        Spacer(Modifier.weight(1f))
        PrimaryButton(
            text = if (sedangProses) "Memeriksa..." else "Lanjutkan",
            onClick = onLanjut,
            enabled = kode.length == 6 && !sedangProses,
        )
        Spacer(Modifier.height(30.dp))
    }
}

/** Header "< Daftar" (Figma: Heading 1, 32). Dipakai juga di layar Buat Password. */
@Composable
internal fun JudulDaftar(onBackClick: () -> Unit) {
    Spacer(Modifier.height(2.dp))
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(19.dp)) {
        BackButton(onBackClick = onBackClick)
        Text("Daftar", style = Heading1, color = Ink)
    }
}

/**
 * Enam kotak kode. Sebenarnya cuma satu BasicTextField tersembunyi;
 * kotak-kotaknya hanya tampilan dari isi field itu.
 */
@Composable
private fun KotakKode(kode: String, salah: Boolean, onChange: (String) -> Unit) {
    BasicTextField(
        value = kode,
        onValueChange = onChange,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        decorationBox = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(6) { i ->
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(width = 44.dp, height = 60.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White)
                            .border(2.dp, if (salah) ErrorBorder else Primary, RoundedCornerShape(10.dp)),
                    ) {
                        Text(
                            text = kode.getOrNull(i)?.toString().orEmpty(),
                            style = TextStyle(fontFamily = Sora, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp),
                            color = Primary,
                        )
                    }
                }
            }
        },
    )
}

/** "6281234567890" → "0812-xxxx-xxxx" (sama dengan Figma). */
private fun samarkanNoHp(noHp: String): String {
    val lokal = "0" + noHp.removePrefix("62").removePrefix("0")
    return "${lokal.take(4)}-xxxx-xxxx"
}

@Preview(widthDp = 402, heightDp = 874)
@Composable
private fun KodeVerifikasiPreview() {
    KodeVerifikasiContent(
        noHp = "81234567890",
        kode = "482915",
        terisiOtomatis = true,
        sisaDetik = 59,
        sedangProses = false,
        pesanError = null,
        onKodeChange = {},
        onKirimUlang = {},
        onLanjut = {},
        onBackClick = {},
    )
}
