package com.sempatapps.presentation.auth.register

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sempatapps.core.designsystem.Canvas
import com.sempatapps.navigation.RegisterRoute
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.sempatapps.R

@Composable
fun RegisterScreen(
    onBackClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Canvas)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .width(74.4.dp)
                .height(48.dp)
                .background(color = Color(0xFFE8F9F7), shape = RoundedCornerShape(size = 28.8.dp))
                .padding(start = 12.dp, top = 13.2.dp, end = 12.dp, bottom = 13.2.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.arrow_back)
                contentDescription = ""
            )
        }
    }
}


@Preview(showBackground = true, widthDp = 402, heightDp = 874)
@Composable
private fun RegisterScreenPreview() {
    RegisterScreen(onBackClick = {})
}