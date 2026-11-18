package com.sempatapps.presentation.auth.register

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sempatapps.R
import com.sempatapps.core.designsystem.Body
import com.sempatapps.core.designsystem.BodyBold
import com.sempatapps.core.designsystem.Canvas
import com.sempatapps.core.designsystem.Heading2
import com.sempatapps.core.designsystem.HelperText
import com.sempatapps.core.designsystem.IndicatorActive
import com.sempatapps.core.designsystem.Ink
import com.sempatapps.core.designsystem.Muted
import com.sempatapps.core.designsystem.Primary
import com.sempatapps.core.designsystem.SecondaryDark
import com.sempatapps.core.designsystem.SecondaryLightActive
import com.sempatapps.core.designsystem.SurfaceLight
import com.sempatapps.core.designsystem.component.BackButton
import com.sempatapps.core.designsystem.component.BottomActionPanel
import com.sempatapps.core.designsystem.component.ErrorBox
import com.sempatapps.core.designsystem.component.LokasiDefault
import com.sempatapps.core.designsystem.component.PetaOsm
import com.sempatapps.core.designsystem.component.PinOffsetY
import com.sempatapps.core.designsystem.component.PinPilihLokasi
import com.sempatapps.core.designsystem.component.PrimaryButton
import com.sempatapps.core.designsystem.component.SempatTextField
import com.sempatapps.domain.model.HomeRole
import com.sempatapps.domain.model.TitikLokasi
import org.osmdroid.util.GeoPoint

/** Figma: "Daftar-Donatur - Lokasi" dan "Daftar-Penerima - Lokasi". */
@Composable
fun LokasiScreen(
    viewModel: RegisterViewModel,
    onBackClick: () -> Unit,
    onPilihDiPeta: () -> Unit,
    onKodeTerkirim: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.hapusError() }
    LokasiContent(
        peran = state.data.peran,
        lokasi = state.data.lokasi,
        patokan = state.data.patokan,
        setuju = state.setujuKetentuan,
        sedangProses = state.sedangProses,
        pesanError = state.pesanError,
        onBackClick = onBackClick,
        onPilihDiPeta = onPilihDiPeta,
        onPatokanChange = viewModel::ubahPatokan,
        onSetujuChange = viewModel::ubahSetuju,
        onBuatAkun = { viewModel.buatAkun(onKodeTerkirim) },
    )
}

@Composable
private fun LokasiContent(
    peran: HomeRole,
    lokasi: TitikLokasi?,
    patokan: String,
    setuju: Boolean,
    sedangProses: Boolean,
    pesanError: String?,
    onBackClick: () -> Unit,
    onPilihDiPeta: () -> Unit,
    onPatokanChange: (String) -> Unit,
    onSetujuChange: (Boolean) -> Unit,
    onBuatAkun: () -> Unit,
) {
    val donatur = peran == HomeRole.DONOR
    Column(Modifier.fillMaxSize().background(Canvas)) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 20.dp),
        ) {
            HeaderLangkah(judul = if (donatur) "Daftar Donatur" else "Daftar Penerima", onBackClick = onBackClick)

            Text(
                text = if (donatur) "Di Mana Lokasimu?" else "Di Mana Kamu Berada?",
                style = Heading2,
                color = Ink,
                modifier = Modifier.width(251.dp),
            )
            Text(
                text = if (donatur) "Penerima mengambil pangan di lokasi ini." else "Kami gunakan lokasimu untuk menemukan pangan terdekat.",
                style = BodyBold,
                color = Muted,
            )

            Spacer(Modifier.height(32.dp))
            Text(if (donatur) "Lokasi Pengambilan" else "Alamat Penerima", style = BodyBold, color = Color.Black)
            Spacer(Modifier.height(7.dp))
            Text("Cari Alamat di Map", style = Body, color = Color.Black)
            Spacer(Modifier.height(6.dp))
            PratinjauPeta(lokasi = lokasi, onClick = onPilihDiPeta)
            if (lokasi != null && lokasi.alamat.isNotBlank()) {
                Text(lokasi.alamat, style = Body.copy(fontSize = 12.sp, lineHeight = 18.sp), color = HelperText, modifier = Modifier.padding(top = 4.dp))
            }

            Spacer(Modifier.height(7.dp))
            Text("Patokan Lokasi", style = Body, color = Color.Black)
            SempatTextField(
                value = patokan,
                onValueChange = onPatokanChange,
                placeholder = "Patokan lokasi, contoh: depan masjid",
                singleLine = false,
                minHeight = 101.dp,
            )

            if (pesanError != null) {
                Spacer(Modifier.height(16.dp))
                ErrorBox(title = "Gagal membuat akun.", message = pesanError)
            }
            Spacer(Modifier.height(24.dp))
        }

        BottomActionPanel {
            PrimaryButton(
                text = if (sedangProses) "Mengirim kode..." else "Buat Akun",
                onClick = onBuatAkun,
                enabled = lokasi != null && setuju && !sedangProses,
            )
            Persetujuan(setuju = setuju, onChange = onSetujuChange)
        }
    }
}

/** Kepala layar dengan dua garis langkah + badge "Langkah terakhir" (Figma, tinggi 130). */
@Composable
private fun HeaderLangkah(judul: String, onBackClick: () -> Unit) {
    Spacer(Modifier.height(20.dp))
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(17.dp)) {
        BackButton(onBackClick = onBackClick)
        Text(judul, style = Heading2, color = Ink)
    }
    Spacer(Modifier.height(6.dp))
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
        repeat(2) {
            Box(Modifier.width(175.dp).height(4.dp).clip(RoundedCornerShape(12.dp)).background(Primary))
        }
    }
    Spacer(Modifier.height(24.dp))
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(width = 26.dp, height = 22.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(SecondaryLightActive)
                .clickable(role = Role.Button, onClick = onBackClick),
        ) {
            Image(painterResource(R.drawable.ic_chevron_back_small), contentDescription = "Langkah sebelumnya", modifier = Modifier.size(18.dp))
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .height(22.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(SecondaryDark)
                .padding(horizontal = 10.dp),
        ) {
            Text("Langkah terakhir", style = BodyBold.copy(fontSize = 14.sp, lineHeight = 16.sp), color = SurfaceLight)
        }
    }
    Spacer(Modifier.height(4.dp))
}

@Composable
private fun PratinjauPeta(lokasi: TitikLokasi?, onClick: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(115.dp)
            .clip(shape)
            .background(Color.White)
            .border(2.dp, Primary, shape),
    ) {
        PetaOsm(
            pusat = lokasi?.let { GeoPoint(it.latitude, it.longitude) } ?: LokasiDefault,
            bisaDigeser = false,
            modifier = Modifier.fillMaxSize(),
        )
        // Lapisan transparan supaya ketukan diteruskan ke onClick, bukan ditelan peta.
        Box(Modifier.fillMaxSize().clickable(role = Role.Button, onClick = onClick))
        PinPilihLokasi(Modifier.offset(y = PinOffsetY))
    }
}

@Composable
private fun Persetujuan(setuju: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(15.dp, Alignment.CenterHorizontally),
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(value = setuju, role = Role.Checkbox, onValueChange = onChange),
    ) {
        if (setuju) {
            Image(painterResource(R.drawable.ic_checkbox_checked), contentDescription = null, modifier = Modifier.size(24.dp))
        } else {
            Box(Modifier.size(24.dp).padding(3.dp).border(2.dp, IndicatorActive, RoundedCornerShape(3.dp)))
        }
        Text(
            text = buildAnnotatedString {
                append("Dengan ini saya menyetujui ")
                withStyle(SpanStyle(color = Primary)) { append("segala peraturan dan ketentuan SEMPAT") }
            },
            style = Body,
            color = Color.Black,
            modifier = Modifier.width(286.dp),
        )
    }
}

@Preview(widthDp = 402, heightDp = 874)
@Composable
private fun LokasiPreview() {
    LokasiContent(
        peran = HomeRole.DONOR,
        lokasi = null,
        patokan = "",
        setuju = true,
        sedangProses = false,
        pesanError = null,
        onBackClick = {},
        onPilihDiPeta = {},
        onPatokanChange = {},
        onSetujuChange = {},
        onBuatAkun = {},
    )
}
