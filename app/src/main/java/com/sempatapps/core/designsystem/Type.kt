package com.sempatapps.core.designsystem

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.sempatapps.R

val Sora = FontFamily(
    Font(R.font.sora_extrabold, FontWeight.ExtraBold),
)

val Figtree = FontFamily(
    Font(R.font.figtree_regular, FontWeight.Normal),
    Font(R.font.figtree_bold, FontWeight.Bold),
)

val HeadingLarge = TextStyle(
    fontFamily = Sora,
    fontWeight = FontWeight.ExtraBold,
    fontSize = 24.sp,
    lineHeight = 36.sp,
    color = Color(0xFF0C0C0C),
)
val HeadingLargeGreen = TextStyle(
    fontFamily = Sora,
    fontWeight = FontWeight.ExtraBold,
    fontSize = 24.sp,
    lineHeight = 36.sp,
    color = Color(0xFF0E7C66),
)

val BodyBold = TextStyle(
    fontFamily = Figtree,
    fontWeight = FontWeight.Bold,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    color = Color(0xFF7D7B7B),
)