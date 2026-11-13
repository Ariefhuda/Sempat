package com.sempatapps.core.designsystem.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sempatapps.R
import com.sempatapps.core.designsystem.PaleTeal

@Composable
fun BackButton(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(width = 74.dp, height = 48.dp)
            .clip(RoundedCornerShape(28.8.dp))
            .background(PaleTeal)
            .clickable(role = Role.Button, onClick = onBackClick),
    ) {
        Image(
            painter = painterResource(R.drawable.ic_arrow_back),
            contentDescription = "Kembali",
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BackButtonPreview() {
    BackButton(onBackClick = {})
}
