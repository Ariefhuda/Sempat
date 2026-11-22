package com.sempatapps.presentation.auth.register

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
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
import com.sempatapps.core.designsystem.ErrorText
import com.sempatapps.core.designsystem.Heading2
import com.sempatapps.core.designsystem.Heading3
import com.sempatapps.core.designsystem.HelperText
import com.sempatapps.core.designsystem.Ink
import com.sempatapps.core.designsystem.PaleTeal
import com.sempatapps.core.designsystem.Placeholder
import com.sempatapps.core.designsystem.Primary
import com.sempatapps.core.designsystem.PrimaryDark
import com.sempatapps.core.designsystem.PrimaryLightHover
import com.sempatapps.core.designsystem.SecondaryDark
import com.sempatapps.core.designsystem.SecondaryLightActive
import com.sempatapps.core.designsystem.ShadowCard
import com.sempatapps.core.designsystem.ShadowFaint
import com.sempatapps.core.designsystem.component.BackButton
import com.sempatapps.core.designsystem.component.BottomActionPanel
import com.sempatapps.core.designsystem.component.PrimaryButton
import com.sempatapps.core.designsystem.component.SempatDialog
import com.sempatapps.core.designsystem.component.SempatTextField
import com.sempatapps.domain.model.KtpData
import com.sempatapps.domain.model.KtpField

/** Figma: "Daftar - Hasil Baca KTP (Donatur/Penerima)" + "Pop up Konfirmasi" + "Pop up Batal Register". */
@Composable
fun HasilKtpScreen(
    viewModel: RegisterViewModel,
    onBackClick: () -> Unit,
    onLanjut: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var tampilkanError by rememberSaveable { mutableStateOf(false) }
    var dialogKonfirmasi by rememberSaveable { mutableStateOf(false) }
    var dialogBatal by rememberSaveable { mutableStateOf(false) }

    // Kembali dari sini = hasil baca KTP dibuang, jadi tanya dulu.
    BackHandler { dialogBatal = true }

    HasilKtpContent(
        noHp = state.data.noHp,
        ktp = state.data.ktp,
        isiManual = state.isiManual,
        kolomSalah = if (tampilkanError) viewModel.kolomSalah() else emptySet(),
        noHpSalah = tampilkanError && !viewModel.noHpValid(),
        onNoHpChange = viewModel::ubahNoHp,
        onKtpChange = viewModel::ubahKtp,
        onBackClick = { dialogBatal = true },
        onDataBenar = {
            tampilkanError = true
            if (viewModel.noHpValid() && viewModel.kolomSalah().isEmpty()) dialogKonfirmasi = true
        },
    )

    if (dialogKonfirmasi) {
        SempatDialog(
            illustrationRes = R.drawable.ilustrasi_konfirmasi_data,
            title = "Data Sudah Benar?",
            message = "Pastikan data yang kamu masukkan sudah sesuai sebelum melanjutkan",
            secondaryText = "Periksa Lagi",
            primaryText = "Ya, Sudah Benar",
            onPrimary = {
                dialogKonfirmasi = false
                onLanjut()
            },
            onDismiss = { dialogKonfirmasi = false },
        )
    }
    if (dialogBatal) {
        SempatDialog(
            illustrationRes = R.drawable.ilustrasi_batal_daftar,
            title = "Batalkan Pendaftaran?",
            message = "Data yang sudah kamu masukkan akan hilang jika kamu keluar sekarang.",
            secondaryText = "Kembali",
            primaryText = "Ya, Batalkan",
            onPrimary = {
                dialogBatal = false
                onBackClick()
            },
            onDismiss = { dialogBatal = false },
        )
    }
}

@Composable
private fun HasilKtpContent(
    noHp: String,
    ktp: KtpData,
    isiManual: Boolean,
    kolomSalah: Set<KtpField>,
    noHpSalah: Boolean,
    onNoHpChange: (String) -> Unit,
    onKtpChange: (KtpField, String) -> Unit,
    onBackClick: () -> Unit,
    onDataBenar: () -> Unit,
) {
    Column(Modifier.fillMaxSize().background(Canvas)) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 20.dp),
        ) {
            Spacer(Modifier.height(6.dp))
            BackButton(onBackClick = onBackClick)
            Spacer(Modifier.height(8.dp))
            Text("Hasil Baca KTP", style = Heading2, color = PrimaryDark)
            Text("Periksa datanya. Perbaiki kalau ada yang salah baca.", style = Body)

            Spacer(Modifier.height(16.dp))
            Text("Nomor HP", style = BodyBold, color = Color.Black)
            Spacer(Modifier.height(4.dp))
            SempatTextField(
                value = noHp,
                onValueChange = onNoHpChange,
                placeholder = "812 3456 7890",
                leading = "+62",
                isError = noHpSalah,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            )
            Text(
                text = if (noHpSalah) "Nomor HP belum benar, contoh: 812 3456 7890" else "Dipakai untuk masuk ke aplikasi",
                style = Body.copy(fontSize = 12.sp, lineHeight = 18.sp),
                color = if (noHpSalah) ErrorText else HelperText,
            )

            Spacer(Modifier.height(12.dp))
            Text("Data KTP", style = Heading3, color = Color.Black)
            Spacer(Modifier.height(12.dp))
            KartuDataKtp(ktp = ktp, isiManual = isiManual, kolomSalah = kolomSalah, onKtpChange = onKtpChange)
            Spacer(Modifier.height(17.dp))
            KartuCatatan()
            Spacer(Modifier.height(24.dp))
        }

        BottomActionPanel {
            PrimaryButton(text = "Data Sudah Benar", onClick = onDataBenar)
            Text(
                "Data ini masih bisa kamu ubah nanti di Profil.",
                style = Body.copy(fontSize = 12.sp, lineHeight = 18.sp),
                color = Color.Black,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun KartuDataKtp(
    ktp: KtpData,
    isiManual: Boolean,
    kolomSalah: Set<KtpField>,
    onKtpChange: (KtpField, String) -> Unit,
) {
    val shape = RoundedCornerShape(24.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(18.dp, shape, spotColor = ShadowCard, ambientColor = ShadowCard)
            .clip(shape)
            .background(Canvas)
            .padding(start = 11.dp, end = 13.dp, top = 18.dp, bottom = 18.dp),
    ) {
        GambarKtp(kota = ktp.kabupatenKota, modifier = Modifier.padding(start = 1.dp))
        Spacer(Modifier.height(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            BarisKtp("NIK", ktp.nik, "16 angka", KtpField.NIK in kolomSalah, angka = true, samarkan = true) {
                onKtpChange(KtpField.NIK, it)
            }
            BarisKtp("Nama", ktp.nama, "Nama sesuai KTP", KtpField.NAMA in kolomSalah) {
                onKtpChange(KtpField.NAMA, it)
            }
            BarisKtp("Tanggal Lahir", ktp.tanggalLahir, "dd-mm-yyyy", KtpField.TANGGAL_LAHIR in kolomSalah) {
                onKtpChange(KtpField.TANGGAL_LAHIR, it)
            }
            BarisStatus(
                when {
                    isiManual -> StatusBaca("Diisi manual", PrimaryLightHover, PrimaryDark)
                    ktp.perluDicek.isEmpty() -> StatusBaca("Terbaca", PrimaryLightHover, PrimaryDark)
                    else -> StatusBaca("Perlu dicek", SecondaryLightActive, SecondaryDark)
                },
            )
            BarisKtp("Kota", ktp.kabupatenKota, "Kota/Kabupaten", KtpField.KABUPATEN_KOTA in kolomSalah) {
                onKtpChange(KtpField.KABUPATEN_KOTA, it)
            }
            BarisKtp("Alamat", ktp.alamat, "Jalan, RT/RW (opsional)", KtpField.ALAMAT in kolomSalah) {
                onKtpChange(KtpField.ALAMAT, it)
            }
        }
        if (!isiManual && ktp.perluDicek.isEmpty()) {
            Spacer(Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .size(width = 212.dp, height = 31.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(SecondaryDark),
            ) {
                Image(painterResource(R.drawable.ic_like), contentDescription = null, modifier = Modifier.size(24.dp))
                Text("DATA TERBACA", style = BodyBold.copy(fontSize = 14.sp), color = PaleTeal, textAlign = TextAlign.Center, modifier = Modifier.width(144.dp))
            }
        }
    }
}

/**
 * Satu baris "label .... nilai". Nilainya bisa langsung diketik untuk memperbaiki hasil baca.
 * NIK ditampilkan tersamar (1271 •••• •••• 0001) kecuali sedang diedit.
 */
@Composable
private fun BarisKtp(
    label: String,
    nilai: String,
    placeholder: String,
    salah: Boolean,
    angka: Boolean = false,
    samarkan: Boolean = false,
    onChange: (String) -> Unit,
) {
    var fokus by remember { mutableStateOf(false) }
    val gaya = BodyBold.copy(color = if (salah) SecondaryDark else Color.Black, textAlign = TextAlign.End)
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(label, style = Body, color = if (salah) ErrorText else Color.Black)
        Spacer(Modifier.width(12.dp))
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
            BasicTextField(
                value = nilai,
                onValueChange = onChange,
                singleLine = true,
                textStyle = gaya,
                visualTransformation = if (samarkan && !fokus && nilai.length == 16) NikTersamar else VisualTransformation.None,
                keyboardOptions = if (angka) KeyboardOptions(keyboardType = KeyboardType.Number) else KeyboardOptions.Default,
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { fokus = it.isFocused },
                decorationBox = { field ->
                    Box(contentAlignment = Alignment.CenterEnd) {
                        if (nilai.isEmpty()) Text(placeholder, style = gaya.copy(color = Placeholder, fontWeight = FontWeight.Normal))
                        field()
                    }
                },
            )
        }
    }
}

private data class StatusBaca(val teks: String, val latar: Color, val warna: Color)

@Composable
private fun BarisStatus(status: StatusBaca) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text("Status", style = Body, color = Color.Black)
        Spacer(Modifier.weight(1f))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .width(102.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(status.latar),
        ) {
            Text(status.teks, style = BodyBold.copy(fontSize = 14.sp), color = status.warna)
        }
    }
}

/** Gambar KTP ilustrasi (Figma: "KTP (contoh)"). Baris abu-abu cuma hiasan, bukan data asli. */
@Composable
private fun GambarKtp(kota: String, modifier: Modifier = Modifier) {
    val garis = Color(0xFF99BDD4)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(202.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFD6EDF7))
            .border(1.dp, Primary, RoundedCornerShape(12.dp)),
    ) {
        Column(Modifier.padding(start = 20.dp, top = 16.dp)) {
            Text("KARTU TANDA PENDUDUK", style = BodyBold.copy(fontSize = 12.sp, lineHeight = 15.sp), color = Color(0xFF336185))
            Text(kota.ifBlank { "-" }, style = Body.copy(fontSize = 12.sp, lineHeight = 15.sp, fontWeight = FontWeight.Medium), color = Color(0xFF436C8B))
            Spacer(Modifier.height(16.dp))
            listOf(150, 120, 165, 100, 140, 90).forEach { lebar ->
                Box(Modifier.padding(bottom = 13.dp).size(width = lebar.dp, height = 7.dp).clip(RoundedCornerShape(3.5.dp)).background(garis))
            }
        }
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .padding(top = 52.dp, end = 20.dp)
                .size(width = 84.dp, height = 108.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFA8CCE0)),
        )
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .padding(top = 168.dp, end = 36.dp)
                .size(width = 52.dp, height = 6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(garis),
        )
    }
}

@Composable
private fun KartuCatatan() {
    val shape = RoundedCornerShape(24.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(28.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .shadow(4.dp, shape, spotColor = ShadowFaint, ambientColor = ShadowFaint)
            .clip(shape)
            .background(Canvas)
            .border(1.dp, Primary, shape)
            .padding(horizontal = 17.dp),
    ) {
        Image(painterResource(R.drawable.ilustrasi_catatan_ktp), contentDescription = null, modifier = Modifier.size(width = 61.dp, height = 67.dp))
        Column {
            Text("Catatan", style = BodyBold, color = PrimaryDark)
            Text("Foto KTP tidak disimpan. Hanya data di atas yang dipakai untuk akunmu.", style = Body, color = Ink)
        }
    }
}

/** "1271010101980001" → "1271 •••• •••• 0001". Hanya dipakai saat kolom NIK tidak sedang diedit. */
private object NikTersamar : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val nik = text.text
        val tampil = "${nik.take(4)} •••• •••• ${nik.takeLast(4)}"
        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int) = if (offset <= 4) offset else (offset + 3).coerceAtMost(tampil.length)
            override fun transformedToOriginal(offset: Int) = if (offset <= 4) offset else (offset - 3).coerceIn(4, nik.length)
        }
        return TransformedText(AnnotatedString(tampil), mapping)
    }
}

@Preview(widthDp = 402, heightDp = 1100)
@Composable
private fun HasilKtpPreview() {
    HasilKtpContent(
        noHp = "",
        ktp = KtpData(nik = "1271010101980001", nama = "SITI RAHMA", tanggalLahir = "12-05-1998", kabupatenKota = "KOTA MEDAN"),
        isiManual = false,
        kolomSalah = emptySet(),
        noHpSalah = false,
        onNoHpChange = {},
        onKtpChange = { _, _ -> },
        onBackClick = {},
        onDataBenar = {},
    )
}
