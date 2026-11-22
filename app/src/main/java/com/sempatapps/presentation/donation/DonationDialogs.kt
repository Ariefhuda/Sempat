package com.sempatapps.presentation.donation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sempatapps.R
import com.sempatapps.core.designsystem.Figtree
import com.sempatapps.core.designsystem.Primary
import com.sempatapps.core.designsystem.component.SempatDialog

@Composable
internal fun CancelDonationDialog(
    onBack: () -> Unit,
    onConfirmCancel: () -> Unit
) {
    SempatDialog(
        illustrationRes = R.drawable.popup_cancel_donation,
        title = "Batalkan Donasi?",
        message = "Pangan ini belum didonasikan. Yakin ingin membatalkan proses donasi?",
        primaryText = "Ya, Batalkan",
        onPrimary = onConfirmCancel,
        secondaryText = "Kembali",
        onSecondary = onBack,
        onDismiss = onBack,
    )
}

@Composable
internal fun DonationSavedDialog(
    onGoHome: () -> Unit,
    onSelectRecipient: () -> Unit
) {
    SempatDialog(
        illustrationRes = R.drawable.popup_donation_success,
        title = "Pangan Berhasil Disalurkan!",
        message = "Panganmu kini tersedia dan siap didonasikan",
        primaryText = "Pilih Penerima",
        onPrimary = onSelectRecipient,
        secondaryText = "Beranda",
        onSecondary = onGoHome,
        onDismiss = onGoHome,
    )
}

@Composable
internal fun ExitRecipientDialog(
    onExit: () -> Unit,
    onStay: () -> Unit
) {
    SempatDialog(
        illustrationRes = R.drawable.popup_exit_recipient,
        title = "Keluar dari Pilihan Penerima?",
        message = "Kamu bisa memilih penerima yang ingin kamu donasikan nanti. Data panganmu tetap tersimpan",
        primaryText = "Pilih Penerima",
        onPrimary = onStay,
        secondaryText = "Keluar",
        onSecondary = onExit,
        onDismiss = onStay,
    )
}

@Composable
internal fun DistributedSuccessDialog(
    onViewDetail: () -> Unit
) {
    SempatDialog(
        illustrationRes = R.drawable.popup_distributed_success,
        title = "Donasi Berhasil Disalurkan!",
        message = "Terima kasih sudah memberi kesempatan pada pangan untuk bermanfaat kembali",
        primaryText = "Lihat Detail",
        onPrimary = onViewDetail,
        onDismiss = onViewDetail,
    )
}

/** Tombol kompak 44dp untuk kartu penerima 2 kolom. */
@Composable
internal fun SmallPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Primary,
            contentColor = Color.White,
        ),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(
            text = text,
            fontFamily = Figtree,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )
    }
}
