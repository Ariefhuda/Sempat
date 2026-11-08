package com.sempatapps.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color

@Composable
fun RoleCard(
    title: String,
    imageRes: Int,
    onClickRole: () -> Unit,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
){
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .shadow(elevation = 4.dp, spotColor = Color(0x0D000000), ambientColor = Color(0x0D000000))
            .width(174.dp)
            .height(185.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(color = Color(0xFFE8F9F7), shape = RoundedCornerShape(size = 24.dp))
            .clickable(onClick = onClickRole)
    ) {

    }
}