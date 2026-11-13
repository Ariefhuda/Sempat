package com.sempatapps.presentation.auth.register

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sempatapps.R
import com.sempatapps.core.designsystem.BodyBold
import com.sempatapps.core.designsystem.Canvas
import com.sempatapps.core.designsystem.Heading1
import com.sempatapps.core.designsystem.Primary
import com.sempatapps.core.designsystem.component.BackButton
import com.sempatapps.core.designsystem.component.PrimaryButton
import com.sempatapps.core.designsystem.component.RoleCard
import com.sempatapps.domain.model.HomeRole

/**
 * Layar "Daftar": pengguna memilih peran sebelum lanjut mendaftar.
 * Figma: section "1. Akun & KTP / Register", frame "Daftar".
 */
@Composable
fun RegisterScreen(
    onBackClick: () -> Unit,
    onContinue: (HomeRole) -> Unit,
) {
    // rememberSaveable: pilihan tetap ada walau layar diputar / activity dibuat ulang.
    var selectedRole by rememberSaveable { mutableStateOf<HomeRole?>(null) }

    RegisterContent(
        selectedRole = selectedRole,
        onRoleSelect = { selectedRole = it },
        onBackClick = onBackClick,
        onContinueClick = { selectedRole?.let(onContinue) },
    )
}

@Composable
private fun RegisterContent(
    selectedRole: HomeRole?,
    onRoleSelect: (HomeRole) -> Unit,
    onBackClick: () -> Unit,
    onContinueClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Canvas)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(6.dp))

        // Tombol kembali dan judul saling tumpang di Figma, jadi pakai Box.
        Box(modifier = Modifier.fillMaxWidth()) {
            BackButton(
                onBackClick = onBackClick,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 8.dp),
            )
            Text(
                text = "Daftar",
                style = Heading1,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 40.dp),
            )
        }

        Spacer(Modifier.height(6.dp))

        Image(
            painter = painterResource(R.drawable.register_illustration),
            contentDescription = null, // dekoratif
        )

        Spacer(Modifier.height(29.dp))

        Column(
            modifier = Modifier.padding(horizontal = 53.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Pilih Peranmu",
                style = Heading1,
                color = Primary,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Tentukan cara kamu menggunakan SEMPAT",
                style = BodyBold,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(Modifier.height(34.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 17.dp)
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(19.dp),
        ) {
            RoleCard(
                title = "Donatur",
                description = "Salurkan pangan berlebihmu",
                isSelected = selectedRole == HomeRole.DONOR,
                onClick = { onRoleSelect(HomeRole.DONOR) },
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f),
            ) {
                Image(
                    painter = painterResource(R.drawable.role_donatur),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 7.dp),
                )
            }
            RoleCard(
                title = "Penerima",
                description = "Temukan pangan surplus di sekitarmu",
                isSelected = selectedRole == HomeRole.RECIPIENT,
                onClick = { onRoleSelect(HomeRole.RECIPIENT) },
                modifier = Modifier.weight(1f),
            ) {
                Image(
                    painter = painterResource(R.drawable.role_penerima),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 18.dp),
                )
            }
        }

        Spacer(Modifier.height(42.dp))

        PrimaryButton(
            text = "Lanjutkan",
            onClick = onContinueClick,
            enabled = selectedRole != null,
            modifier = Modifier.padding(horizontal = 17.dp),
        )

        Spacer(Modifier.height(40.dp))
    }
}

@Preview(name = "Belum memilih", showBackground = true, widthDp = 402, heightDp = 820)
@Composable
private fun RegisterNoSelectionPreview() {
    RegisterContent(selectedRole = null, onRoleSelect = {}, onBackClick = {}, onContinueClick = {})
}

@Preview(name = "Donatur dipilih", showBackground = true, widthDp = 402, heightDp = 820)
@Composable
private fun RegisterDonorPreview() {
    RegisterContent(selectedRole = HomeRole.DONOR, onRoleSelect = {}, onBackClick = {}, onContinueClick = {})
}

@Preview(name = "Penerima dipilih", showBackground = true, widthDp = 402, heightDp = 820)
@Composable
private fun RegisterRecipientPreview() {
    RegisterContent(selectedRole = HomeRole.RECIPIENT, onRoleSelect = {}, onBackClick = {}, onContinueClick = {})
}
