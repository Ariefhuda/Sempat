package com.sempatapps.core.designsystem.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sempatapps.R

@Composable
fun BackButton(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .size(width = 74.dp, height = 48.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFFE8F9F7))
                .clickable(onClick = onBackClick)
        ){
            Image(
                painter = painterResource(R.drawable.arrow_back),
                contentDescription = "Kembali",
                modifier = Modifier.size(26.dp),
            )
        }
    }

@Preview(showBackground = true)
@Composable
private fun BackButtonPreview(){
    BackButton(onBackClick = {})
}