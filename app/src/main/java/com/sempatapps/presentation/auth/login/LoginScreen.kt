package com.sempatapps.presentation.auth.login

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sempatapps.R
import com.sempatapps.core.designsystem.Body
import com.sempatapps.core.designsystem.BodyBold
import com.sempatapps.core.designsystem.Canvas
import com.sempatapps.core.designsystem.ErrorText
import com.sempatapps.core.designsystem.Heading1
import com.sempatapps.core.designsystem.Heading2
import com.sempatapps.core.designsystem.Ink
import com.sempatapps.core.designsystem.Muted
import com.sempatapps.core.designsystem.Primary
import com.sempatapps.core.designsystem.component.BackButton
import com.sempatapps.core.designsystem.component.ErrorBox
import com.sempatapps.core.designsystem.component.InfoBox
import com.sempatapps.core.designsystem.component.PrimaryButton
import com.sempatapps.core.designsystem.component.SecondaryButton
import com.sempatapps.core.designsystem.component.SempatDialog
import com.sempatapps.core.designsystem.component.SempatTextField
import com.sempatapps.domain.model.HomeRole

/** Figma: "Login" + "Pop up Berhasil Login" (section 1. Akun & KTP / Register (Daftar & Masuk)). */
@Composable
fun LoginScreen(
    onBackClick: () -> Unit,
    onDaftarClick: () -> Unit,
    onMulai: (HomeRole) -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LoginContent(
        state = state,
        onNoHpChange = viewModel::ubahNoHp,
        onPasswordChange = viewModel::ubahPassword,
        onMasuk = viewModel::masuk,
        // TODO: masuk pakai kode OTP (Figma "Masuk - Kode OTP") butuh endpoint OTP-masuk di server.
        onMasukOtp = { Toast.makeText(context, "Masuk pakai kode OTP belum tersedia", Toast.LENGTH_SHORT).show() },
        onDaftarClick = onDaftarClick,
        onBackClick = onBackClick,
    )

    state.berhasilSebagai?.let { peran ->
        SempatDialog(
            illustrationRes = R.drawable.ilustrasi_masuk_berhasil,
            title = "Berhasil Masuk!",
            message = "Selamat datang! Mulai sekarang kamu tidak perlu masuk lagi di HP ini.",
            primaryText = "Mulai",
            onPrimary = { onMulai(peran) },
            onDismiss = { onMulai(peran) },
            dismissible = false,
        )
    }
}

@Composable
private fun LoginContent(
    state: LoginState,
    onNoHpChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onMasuk: () -> Unit,
    onMasukOtp: () -> Unit,
    onDaftarClick: () -> Unit,
    onBackClick: () -> Unit,
) {
    val kecil = Body.copy(fontSize = 12.sp, lineHeight = 18.sp)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Canvas)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(19.dp)) {
            BackButton(onBackClick = onBackClick)
            Text("Masuk", style = Heading1, color = Ink)
        }
        Spacer(Modifier.height(16.dp))
        Text("Selamat Datang Kembali", style = Heading2, color = Primary)
        Spacer(Modifier.height(2.dp))
        Text("Masuk pakai nomor HP dan password. Lupa password? Pakai kode OTP saja.", style = BodyBold, color = Muted)

        Spacer(Modifier.height(32.dp))
        Text("Nomor Handphone", style = BodyBold, color = Color.Black)
        SempatTextField(
            value = state.noHp,
            onValueChange = onNoHpChange,
            placeholder = "812 3456 7890",
            leading = "+62",
            isError = state.noHpSalah,
            minHeight = 60.dp,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        )
        if (state.noHpSalah) Text("Nomor HP belum benar, contoh: 812 3456 7890", style = kecil, color = ErrorText)

        Spacer(Modifier.height(16.dp))
        Text("Password", style = BodyBold, color = Color.Black)
        SempatTextField(
            value = state.password,
            onValueChange = onPasswordChange,
            placeholder = "Ketik password",
            isPassword = true,
            isError = state.passwordKosong,
            minHeight = 60.dp,
        )
        if (state.passwordKosong) Text("Password wajib diisi", style = kecil, color = ErrorText)

        Spacer(Modifier.height(16.dp))
        InfoBox(R.drawable.ic_shield_check, "Cukup masuk sekali di HP ini. Setelah itu aplikasi langsung terbuka tanpa masuk lagi.")
        if (state.pesanError != null) {
            Spacer(Modifier.height(16.dp))
            ErrorBox(title = "Gagal masuk.", message = state.pesanError)
        }

        Spacer(Modifier.weight(1f))
        PrimaryButton(
            text = if (state.sedangProses) "Memeriksa..." else "Masuk",
            onClick = onMasuk,
            enabled = !state.sedangProses,
        )
        Spacer(Modifier.height(12.dp))
        SecondaryButton(text = "Masuk pakai kode OTP", onClick = onMasukOtp, enabled = !state.sedangProses)
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Belum Punya Akun?", style = Body, color = Ink)
            Text(
                "Daftar",
                style = BodyBold,
                color = Primary,
                modifier = Modifier
                    .heightIn(min = 44.dp)
                    .clickable(role = Role.Button, onClick = onDaftarClick)
                    .padding(vertical = 10.dp),
            )
        }
        Spacer(Modifier.height(20.dp))
    }
}

@Preview(widthDp = 402, heightDp = 874)
@Composable
private fun LoginPreview() {
    LoginContent(
        state = LoginState(),
        onNoHpChange = {},
        onPasswordChange = {},
        onMasuk = {},
        onMasukOtp = {},
        onDaftarClick = {},
        onBackClick = {},
    )
}
