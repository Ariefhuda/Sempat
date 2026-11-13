package com.sempatapps.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sempatapps.core.designsystem.BodyBold
import com.sempatapps.core.designsystem.DisabledContent
import com.sempatapps.core.designsystem.DisabledSurface
import com.sempatapps.core.designsystem.PaleTeal
import com.sempatapps.core.designsystem.Primary
import com.sempatapps.core.designsystem.PrimaryDark
import com.sempatapps.core.designsystem.ShadowSoft

private val ButtonShape = RoundedCornerShape(24.dp)

/** Tombol utama: hijau solid. Contoh: "Masuk", "Lanjutkan". */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    SempatButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        containerColor = Primary,
        contentColor = Color.White,
    )
}

/** Tombol sekunder: latar pucat dengan garis tepi. Contoh: "Daftar/Buat Akun". */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    SempatButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        containerColor = PaleTeal,
        contentColor = PrimaryDark,
        border = BorderStroke(1.dp, PrimaryDark),
    )
}

@Composable
private fun SempatButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    containerColor: Color,
    contentColor: Color,
    border: BorderStroke? = null,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .shadow(
                elevation = 4.dp,
                shape = ButtonShape,
                spotColor = ShadowSoft,
                ambientColor = ShadowSoft,
            ),
        shape = ButtonShape,
        border = border,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = DisabledSurface,
            disabledContentColor = DisabledContent,
        ),
        contentPadding = PaddingValues(10.dp),
    ) {
        Text(text = text, style = BodyBold.copy(color = Color.Unspecified))
    }
}

@Preview(showBackground = true)
@Composable
private fun ButtonsPreview() {
    Column(Modifier.padding(20.dp)) {
        PrimaryButton(text = "Masuk", onClick = {})
        Spacer(Modifier.height(11.dp))
        SecondaryButton(text = "Daftar/Buat Akun", onClick = {})
        Spacer(Modifier.height(11.dp))
        PrimaryButton(text = "Lanjutkan", onClick = {}, enabled = false)
    }
}
