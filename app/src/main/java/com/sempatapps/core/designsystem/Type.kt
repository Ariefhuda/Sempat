package com.sempatapps.core.designsystem

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

// Nama style mengikuti Figma. Warna default bisa ditimpa: Text(style = Heading1, color = Primary)

/** Figma "Heading 1": Sora ExtraBold 32. */
val Heading1 = TextStyle(
    fontFamily = Sora,
    fontWeight = FontWeight.ExtraBold,
    fontSize = 32.sp,
    lineHeight = 48.sp,
    color = Ink,
)

/** Figma "Heading 2": Sora ExtraBold 24. */
val Heading2 = TextStyle(
    fontFamily = Sora,
    fontWeight = FontWeight.ExtraBold,
    fontSize = 24.sp,
    lineHeight = 36.sp,
    color = Heading,
)

/** Figma "Label/Button": Figtree Bold 16. Juga dipakai untuk teks pendukung tebal. */
val BodyBold = TextStyle(
    fontFamily = Figtree,
    fontWeight = FontWeight.Bold,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    color = Muted,
)

/** Figtree Regular 12: teks kecil seperti deskripsi di kartu peran. */
val Caption = TextStyle(
    fontFamily = Figtree,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp,
    lineHeight = 18.sp,
)
