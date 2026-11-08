package com.sempatapps.core.designsystem.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sempatapps.core.designsystem.BodyBold

@Composable
fun Button(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(24.dp)
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .border(width = 1.dp, color = Color(0xFF0B5F4F), shape = RoundedCornerShape(size = 24.dp))
            .shadow(
                elevation = 4.dp,
                shape = shape,
                spotColor = Color(0x26000000),
                ambientColor = Color(0x26000000),
            ),
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFE8F9F7),
            contentColor = Color(0xFF0B5F4F),
        ),
        contentPadding = PaddingValues(10.dp),
    ) {
        Text(text = text, style = BodyBold, color = Color(0xFF0B5F4F))
    }
}