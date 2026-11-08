package com.sempatapps.presentation.auth.register

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sempatapps.core.designsystem.Canvas
import com.sempatapps.core.designsystem.HeadingLarge
import com.sempatapps.core.designsystem.component.BackButton
import com.sempatapps.R
import com.sempatapps.core.designsystem.BodyBold
import com.sempatapps.core.designsystem.HeadingLargeGreen
import com.sempatapps.core.designsystem.component.Button
import com.sempatapps.core.designsystem.component.RoleCard
import com.sempatapps.presentation.onboarding.RegisterButton

@Composable
fun RegisterScreen(
    onBackClick: () -> Unit,
    onDonaturClick: () -> Unit,
    onPenerimaClick: () -> Unit,
){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Canvas)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(40.dp))
        Box(modifier = Modifier.fillMaxWidth()){
            BackButton(onBackClick = onBackClick, modifier = Modifier.align(Alignment.TopStart))
        }
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,

        ) {
            Text(
                text = "Daftar",
                style = HeadingLarge,
                textAlign = TextAlign.Center,
            )
            Image(
                painter = painterResource(R.drawable.register_illustration),
                contentDescription = "Illustrasi Pendaftaran",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 29.dp)
                    .height(200.dp),
            )
            Text(
                text = "Pilih Peranmu",
                style = HeadingLargeGreen,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Tentukan cara kamu menggunakan\nSEMPAT",
                style = BodyBold,
                textAlign = TextAlign.Center,
            )
            Row(
                modifier = Modifier
                    .padding(vertical = 29.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp) ,
                verticalAlignment = Alignment.CenterVertically,
                ) {
                RoleCard(
                    title = "Donatur",
                    imageRes = R.drawable.sempat_logo,
                    onClickRole = {selectedRole = "donatur"},
                    modifier = Modifier.weight(1f),
                )
                RoleCard(
                    title = "Penerima",
                    imageRes = R.drawable.sempat_logo,
                    onClickRole = {selectedRole = "penerima"},
                    modifier = Modifier.weight(1f),
                )
            }
            Button(text = "Lanjutkan", onClick = Text("hehehe"))


        }

    }
}

@Preview(showBackground = true, widthDp = 402, heightDp = 874)
@Composable
private fun RegisterScreenPreview() {
    RegisterScreen(onBackClick = {},onDonaturClick = {}, onPenerimaClick = {})
}