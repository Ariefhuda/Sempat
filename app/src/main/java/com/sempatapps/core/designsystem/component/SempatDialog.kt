package com.sempatapps.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.sempatapps.R
import com.sempatapps.core.designsystem.Body
import com.sempatapps.core.designsystem.BodyBold
import com.sempatapps.core.designsystem.Heading3
import com.sempatapps.core.designsystem.PaleTeal
import com.sempatapps.core.designsystem.Primary
import com.sempatapps.core.designsystem.PrimaryDark
import com.sempatapps.core.designsystem.PrimaryLightHover
import com.sempatapps.core.designsystem.SurfaceLight

/**
 * Pop-up standar SEMPAT (Figma: semua frame "Pop up ...", 362 x 337).
 * Ilustrasi di atas, judul, penjelasan, lalu satu atau dua tombol.
 * Kalau [secondaryText] null, cuma tombol utama yang tampil (contoh: "Pendaftaran Berhasil!").
 */
@Composable
fun SempatDialog(
    illustrationRes: Int,
    title: String,
    message: String,
    primaryText: String,
    onPrimary: () -> Unit,
    onDismiss: () -> Unit,
    secondaryText: String? = null,
    onSecondary: () -> Unit = onDismiss,
    dismissible: Boolean = true,
) {
    Dialog(
        onDismissRequest = { if (dismissible) onDismiss() },
        properties = DialogProperties(dismissOnBackPress = dismissible, dismissOnClickOutside = dismissible),
    ) {
        SempatDialogContent(illustrationRes, title, message, primaryText, onPrimary, secondaryText, onSecondary)
    }
}

@Composable
private fun SempatDialogContent(
    illustrationRes: Int,
    title: String,
    message: String,
    primaryText: String,
    onPrimary: () -> Unit,
    secondaryText: String?,
    onSecondary: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(362.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceLight)
            .padding(start = 21.dp, end = 21.dp, top = 21.dp, bottom = 24.dp),
    ) {
        Image(
            painter = painterResource(illustrationRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(157.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(PrimaryLightHover),
        )
        Spacer(Modifier.height(8.dp))
        Text(title, style = Heading3, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Text(message, style = Body, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp))
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            if (secondaryText != null) {
                SmallButton(secondaryText, onSecondary, container = PaleTeal, content = PrimaryDark, border = BorderStroke(1.dp, PrimaryDark))
            }
            SmallButton(primaryText, onPrimary, container = Primary, content = Color.White)
        }
    }
}

/** Figma "Small Button Primary/Secondary": lebar 144, sudut 24. */
@Composable
private fun SmallButton(
    text: String,
    onClick: () -> Unit,
    container: Color,
    content: Color,
    border: BorderStroke? = null,
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        border = border,
        colors = ButtonDefaults.buttonColors(containerColor = container, contentColor = content),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 12.dp),
        modifier = Modifier.width(144.dp),
    ) {
        Text(text, style = BodyBold.copy(color = Color.Unspecified), maxLines = 1)
    }
}

@Preview
@Composable
private fun SempatDialogPreview() {
    SempatDialogContent(
        illustrationRes = R.drawable.ilustrasi_batal_daftar,
        title = "Batalkan Pendaftaran?",
        message = "Data yang sudah kamu masukkan akan hilang jika kamu keluar sekarang.",
        primaryText = "Ya, Batalkan",
        onPrimary = {},
        secondaryText = "Kembali",
        onSecondary = {},
    )
}
