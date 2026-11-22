package com.sempatapps.presentation.auth.register

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sempatapps.R
import com.sempatapps.core.designsystem.Body
import com.sempatapps.core.designsystem.BodyBold
import com.sempatapps.core.designsystem.Canvas
import com.sempatapps.core.designsystem.ErrorText
import com.sempatapps.core.designsystem.Heading2
import com.sempatapps.core.designsystem.Muted
import com.sempatapps.core.designsystem.Primary
import com.sempatapps.core.designsystem.component.ErrorBox
import com.sempatapps.core.designsystem.component.InfoBox
import com.sempatapps.core.designsystem.component.PrimaryButton
import com.sempatapps.core.designsystem.component.SempatDialog
import com.sempatapps.core.designsystem.component.SempatTextField

/** Figma: "Daftar Donatur/Penerima - Buat Password" + "Pop up Berhasil Daftar". */
@Composable
fun BuatPasswordScreen(
    viewModel: RegisterViewModel,
    onBackClick: () -> Unit,
    onMulai: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var password by rememberSaveable { mutableStateOf("") }
    var ulangi by rememberSaveable { mutableStateOf("") }
    var cekIsian by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) { viewModel.hapusError() }

    val salahPassword = when {
        !cekIsian -> null
        password.length < 8 -> "Password minimal 8 huruf atau angka"
        ulangi != password -> "Password yang diulang belum sama"
        else -> null
    }

    BuatPasswordContent(
        password = password,
        ulangi = ulangi,
        salahPassword = salahPassword,
        pesanError = state.pesanError,
        sedangProses = state.sedangProses,
        onPasswordChange = { password = it },
        onUlangiChange = { ulangi = it },
        onBackClick = onBackClick,
        onSimpan = {
            cekIsian = true
            if (password.length >= 8 && password == ulangi) viewModel.simpanPassword(password)
        },
    )

    if (state.daftarBerhasil) {
        SempatDialog(
            illustrationRes = R.drawable.ilustrasi_daftar_berhasil,
            title = "Pendaftaran Berhasil!",
            message = "Akun SEMPAT kamu sudah siap digunakan. Yuk, mulai perjalananmu!",
            primaryText = "Mulai",
            onPrimary = onMulai,
            onDismiss = onMulai,
            dismissible = false,
        )
    }
}

@Composable
private fun BuatPasswordContent(
    password: String,
    ulangi: String,
    salahPassword: String?,
    pesanError: String?,
    sedangProses: Boolean,
    onPasswordChange: (String) -> Unit,
    onUlangiChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onSimpan: () -> Unit,
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
        Text("Buat Password", style = Heading2, color = Primary)
        Spacer(Modifier.height(2.dp))
        Text("Dipakai kalau kamu perlu masuk lagi, misalnya saat ganti HP.", style = BodyBold, color = Muted)

        Spacer(Modifier.height(32.dp))
        Text("Password", style = BodyBold, color = Color.Black)
        SempatTextField(
            value = password,
            onValueChange = onPasswordChange,
            placeholder = "Minimal 8 huruf atau angka",
            isPassword = true,
            isError = salahPassword != null && password.length < 8,
            minHeight = 60.dp,
        )
        Spacer(Modifier.height(16.dp))
        Text("Ulangi Password", style = BodyBold, color = Color.Black)
        SempatTextField(
            value = ulangi,
            onValueChange = onUlangiChange,
            placeholder = "Ketik lagi password yang sama",
            isPassword = true,
            isError = salahPassword != null && password.length >= 8,
            minHeight = 60.dp,
        )
        if (salahPassword != null) {
            Text(salahPassword, style = Body.copy(fontSize = 12.sp, lineHeight = 18.sp), color = ErrorText, modifier = Modifier.padding(top = 4.dp))
        }
        Spacer(Modifier.height(16.dp))
        InfoBox(R.drawable.ic_shield_check, "Setelah daftar, kamu langsung masuk dan tidak perlu masuk lagi di HP ini.")
        if (pesanError != null) {
            Spacer(Modifier.height(16.dp))
            ErrorBox(title = "Gagal mendaftar.", message = pesanError)
        }

        Spacer(Modifier.weight(1f))
        PrimaryButton(
            text = if (sedangProses) "Menyimpan..." else "Simpan & Selesai",
            onClick = onSimpan,
            enabled = !sedangProses,
        )
        Spacer(Modifier.height(20.dp))
    }
}

@Preview(widthDp = 402, heightDp = 874)
@Composable
private fun BuatPasswordPreview() {
    BuatPasswordContent(
        password = "",
        ulangi = "",
        salahPassword = null,
        pesanError = null,
        sedangProses = false,
        onPasswordChange = {},
        onUlangiChange = {},
        onBackClick = {},
        onSimpan = {},
    )
}
