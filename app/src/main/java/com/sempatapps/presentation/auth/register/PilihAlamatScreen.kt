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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sempatapps.R
import com.sempatapps.core.designsystem.Body
import com.sempatapps.core.designsystem.ErrorText
import com.sempatapps.core.designsystem.FieldBorder
import com.sempatapps.core.designsystem.Ink
import com.sempatapps.core.designsystem.Placeholder
import com.sempatapps.core.designsystem.ShadowFaint
import com.sempatapps.core.designsystem.component.BackButton
import com.sempatapps.core.designsystem.component.BottomActionPanel
import com.sempatapps.core.designsystem.component.LokasiDefault
import com.sempatapps.core.designsystem.component.PetaOsm
import com.sempatapps.core.designsystem.component.PinOffsetY
import com.sempatapps.core.designsystem.component.PinPilihLokasi
import com.sempatapps.core.designsystem.component.PrimaryButton
import com.sempatapps.domain.model.TitikLokasi
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint

/**
 * Figma: "Pilih Alamat". Peta digeser sampai pin pas di lokasi, lalu "Konfirmasi".
 * Titik yang dipilih = tengah peta.
 */
@Composable
fun PilihAlamatScreen(
    viewModel: RegisterViewModel,
    onBackClick: () -> Unit,
    onKonfirmasi: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    // pusatPeta = ke mana peta disuruh pindah; titikTengah = posisi terakhir setelah digeser pengguna.
    var pusatPeta by remember { mutableStateOf(LokasiDefault) }
    var titikTengah by remember { mutableStateOf(LokasiDefault) }
    var pencarian by rememberSaveable { mutableStateOf("") }
    var pesan by remember { mutableStateOf<String?>(null) }
    var menyimpan by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.titikAwalPeta()?.let {
            pusatPeta = GeoPoint(it.latitude, it.longitude)
            titikTengah = pusatPeta
        }
    }

    fun cari() {
        if (pencarian.isBlank()) return
        focusManager.clearFocus()
        scope.launch {
            val hasil = viewModel.cariTempat(pencarian)
            if (hasil == null) {
                pesan = "Lokasi tidak ditemukan. Coba kata lain atau geser peta."
            } else {
                pesan = null
                pusatPeta = GeoPoint(hasil.latitude, hasil.longitude)
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        PetaOsm(
            pusat = pusatPeta,
            onPusatBerubah = { titikTengah = it },
            modifier = Modifier.fillMaxSize(),
        )
        PinPilihLokasi(Modifier.align(Alignment.Center).offset(y = PinOffsetY))

        Column(
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = 20.dp),
        ) {
            Spacer(Modifier.height(6.dp))
            BackButton(onBackClick = onBackClick)
            Spacer(Modifier.height(12.dp))
            KolomCari(
                nilai = pencarian,
                onChange = { pencarian = it },
                onCari = ::cari,
            )
            if (pesan != null) {
                Text(
                    pesan!!,
                    style = Body,
                    color = ErrorText,
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
        }

        BottomActionPanel(Modifier.align(Alignment.BottomCenter)) {
            PrimaryButton(
                text = if (menyimpan) "Menyimpan..." else "Konfirmasi",
                enabled = !menyimpan,
                onClick = {
                    menyimpan = true
                    scope.launch {
                        val titik = titikTengah
                        val alamat = viewModel.alamatDari(titik.latitude, titik.longitude)
                        viewModel.pilihLokasi(TitikLokasi(titik.latitude, titik.longitude, alamat))
                        onKonfirmasi()
                    }
                },
            )
            Text("Kamu juga bisa mengubah alamatmu nanti di aplikasi", style = Body, color = Color.Black, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun KolomCari(nilai: String, onChange: (String) -> Unit, onCari: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    BasicTextField(
        value = nilai,
        onValueChange = onChange,
        singleLine = true,
        textStyle = Body.copy(color = Ink),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onCari() }),
        modifier = Modifier.fillMaxWidth(),
        decorationBox = { field ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .height(57.dp)
                    .shadow(2.dp, shape, spotColor = ShadowFaint, ambientColor = ShadowFaint)
                    .clip(shape)
                    .background(Color.White)
                    .border(1.dp, FieldBorder, shape)
                    .padding(10.dp),
            ) {
                Box(Modifier.weight(1f)) {
                    if (nilai.isEmpty()) Text("Cari Lokasi", style = Body, color = Placeholder)
                    field()
                }
                Image(
                    painter = painterResource(R.drawable.ic_search),
                    contentDescription = "Cari",
                    modifier = Modifier.size(24.dp).clickable(role = Role.Button, onClick = onCari),
                )
            }
        },
    )
}
