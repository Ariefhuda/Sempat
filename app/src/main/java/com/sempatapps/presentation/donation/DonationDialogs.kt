package com.sempatapps.presentation.donation

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.sempatapps.R
import com.sempatapps.core.designsystem.Ink
import com.sempatapps.core.designsystem.PrimaryDark
import com.sempatapps.core.designsystem.PrimaryLight
import com.sempatapps.core.designsystem.PrimaryLightHover
import com.sempatapps.core.designsystem.PrimaryNormal
import com.sempatapps.core.designsystem.SurfaceWhite

@Composable
internal fun CancelDonationDialog(
    onBack: () -> Unit,
    onConfirmCancel: () -> Unit
) {
    DonationModalCard(
        illustrationRes = R.drawable.popup_cancel_donation,
        title = "Batalkan Donasi?",
        message = "Pangan ini belum didonasikan. Yakin ingin membatalkan proses donasi?",
        onDismiss = onBack
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            SecondaryPillButton(
                text = "Kembali",
                onClick = onBack,
                modifier = Modifier.weight(1f)
            )
            PrimaryPillButton(
                text = "Ya, Batalkan",
                onClick = onConfirmCancel,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
internal fun DonationSavedDialog(
    onGoHome: () -> Unit,
    onSelectRecipient: () -> Unit
) {
    DonationModalCard(
        illustrationRes = R.drawable.popup_donation_success,
        title = "Pangan Berhasil Disalurkan!",
        message = "Panganmu kini tersedia dan siap didonasikan",
        onDismiss = onGoHome
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            SecondaryPillButton(
                text = "Beranda",
                onClick = onGoHome,
                modifier = Modifier.weight(1f)
            )
            PrimaryPillButton(
                text = "Pilih Penerima",
                onClick = onSelectRecipient,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
internal fun ExitRecipientDialog(
    onExit: () -> Unit,
    onStay: () -> Unit
) {
    DonationModalCard(
        illustrationRes = R.drawable.popup_exit_recipient,
        title = "Keluar dari Pilihan Penerima?",
        message = "Kamu bisa memilih penerima yang ingin kamu donasikan nanti. Data panganmu tetap tersimpan",
        onDismiss = onStay
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            SecondaryPillButton(
                text = "Keluar",
                onClick = onExit,
                modifier = Modifier.weight(1f)
            )
            PrimaryPillButton(
                text = "Pilih Penerima",
                onClick = onStay,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
internal fun DistributedSuccessDialog(
    onViewDetail: () -> Unit
) {
    DonationModalCard(
        illustrationRes = R.drawable.popup_distributed_success,
        title = "Donasi Berhasil Disalurkan!",
        message = "Terima kasih sudah memberi kesempatan pada pangan untuk bermanfaat kembali",
        onDismiss = onViewDetail
    ) {
        PrimaryPillButton(
            text = "Lihat Detail",
            onClick = onViewDetail,
            modifier = Modifier.width(160.dp)
        )
    }
}

@Composable
private fun DonationModalCard(
    @DrawableRes illustrationRes: Int,
    title: String,
    message: String,
    onDismiss: () -> Unit,
    actions: @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(12.dp),
            color = SurfaceWhite,
            shadowElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(157.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(PrimaryLightHover),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(illustrationRes),
                        contentDescription = title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(157.dp),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = title,
                    color = Ink,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    lineHeight = 28.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = message,
                    color = Ink,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 21.sp,
                    modifier = Modifier.padding(horizontal = 6.dp)
                )
                Spacer(Modifier.height(18.dp))
                actions()
            }
        }
    }
}

@Composable
internal fun PrimaryPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    heightDp: Int = 48
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(heightDp.dp),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = PrimaryNormal,
            contentColor = Color.White
        ),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

@Composable
internal fun SecondaryPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    heightDp: Int = 48
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(heightDp.dp),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, PrimaryDark),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = PrimaryLight,
            contentColor = PrimaryDark
        ),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}
