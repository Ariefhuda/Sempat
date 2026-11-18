package com.sempatapps.core.designsystem.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sempatapps.R
import com.sempatapps.core.designsystem.Body
import com.sempatapps.core.designsystem.BodyBold
import com.sempatapps.core.designsystem.Canvas
import com.sempatapps.core.designsystem.ErrorBorder
import com.sempatapps.core.designsystem.ErrorSurface
import com.sempatapps.core.designsystem.ErrorText
import com.sempatapps.core.designsystem.FieldBorder
import com.sempatapps.core.designsystem.InfoText
import com.sempatapps.core.designsystem.Ink
import com.sempatapps.core.designsystem.PaleTeal
import com.sempatapps.core.designsystem.Placeholder
import com.sempatapps.core.designsystem.ShadowFaint
import com.sempatapps.core.designsystem.SurfaceLight

private val FieldShape = RoundedCornerShape(12.dp)

/**
 * Kolom isian putih bergaris hijau muda (Figma: kolom "Nomor HP", "Patokan Lokasi", "Password").
 * Kalau [isPassword] true, muncul ikon mata untuk lihat/sembunyikan isi.
 */
@Composable
fun SempatTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    minHeight: Dp = 56.dp,
    singleLine: Boolean = true,
    isPassword: Boolean = false,
    isError: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    leading: String? = null,
) {
    var passwordTerlihat by remember { mutableStateOf(false) }
    val transformation = if (isPassword && !passwordTerlihat) PasswordVisualTransformation() else VisualTransformation.None

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = singleLine,
        textStyle = Body.copy(color = Ink),
        visualTransformation = transformation,
        keyboardOptions = if (isPassword) keyboardOptions.copy(keyboardType = KeyboardType.Password) else keyboardOptions,
        modifier = modifier.fillMaxWidth(),
        decorationBox = { field ->
            Row(
                verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .heightIn(min = minHeight)
                    .clip(FieldShape)
                    .background(Color.White)
                    .border(1.dp, if (isError) ErrorBorder else FieldBorder, FieldShape)
                    .padding(horizontal = if (isPassword) 16.dp else 10.dp, vertical = 10.dp),
            ) {
                if (leading != null) Text(leading, style = Body, color = Placeholder)
                Box(Modifier.weight(1f)) {
                    if (value.isEmpty()) Text(placeholder, style = Body, color = Placeholder)
                    field()
                }
                if (isPassword) {
                    Image(
                        painter = painterResource(R.drawable.ic_eye),
                        contentDescription = if (passwordTerlihat) "Sembunyikan password" else "Lihat password",
                        modifier = Modifier
                            .size(24.dp)
                            .clickable(role = Role.Button) { passwordTerlihat = !passwordTerlihat },
                    )
                }
            }
        },
    )
}

/** Kotak info hijau muda dengan ikon di kiri (Figma: "Info Sesi", "Info Isi Otomatis"). */
@Composable
fun InfoBox(
    iconRes: Int,
    text: String,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = Body,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(FieldShape)
            .background(PaleTeal)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Image(painterResource(iconRes), contentDescription = null, modifier = Modifier.size(22.dp))
        Text(text, style = textStyle.copy(lineHeight = textStyle.fontSize * 1.4f), color = InfoText)
    }
}

/** Kotak pesan merah (Figma: "Pesan Gagal Baca"). Dipakai juga untuk error dari server. */
@Composable
fun ErrorBox(
    title: String,
    message: String?,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(FieldShape)
            .background(ErrorSurface)
            .border(1.dp, ErrorBorder, FieldShape)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Image(painterResource(R.drawable.ic_error_circle), contentDescription = null, modifier = Modifier.size(24.dp))
        Column {
            Text(title, style = BodyBold.copy(lineHeight = BodyBold.fontSize * 1.4f), color = ErrorText)
            if (message != null) {
                Text(message, style = Body.copy(lineHeight = Body.fontSize * 1.4f), color = ErrorText)
            }
        }
    }
}

/** Panel putih menempel di bawah layar, berisi tombol utama (Figma: tinggi 130, sudut atas 12). */
@Composable
fun BottomActionPanel(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(7.dp),
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 6.dp, shape = shape, spotColor = ShadowFaint, ambientColor = ShadowFaint)
            .background(SurfaceLight, shape)
            .navigationBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 19.dp, bottom = 20.dp),
        content = content,
    )
}

@Preview(showBackground = true, widthDp = 402)
@Composable
private fun FormComponentsPreview() {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.background(Canvas).padding(20.dp),
    ) {
        SempatTextField(value = "", onValueChange = {}, placeholder = "8xx xxxx xxxx", leading = "+62")
        SempatTextField(value = "", onValueChange = {}, placeholder = "Minimal 8 huruf atau angka", isPassword = true, minHeight = 60.dp)
        InfoBox(R.drawable.ic_shield_check, "Setelah daftar, kamu langsung masuk dan tidak perlu masuk lagi di HP ini.")
        ErrorBox("KTP belum terbaca.", "Pastikan semua tulisan kelihatan, tidak silau, dan KTP tidak terpotong.")
    }
}
