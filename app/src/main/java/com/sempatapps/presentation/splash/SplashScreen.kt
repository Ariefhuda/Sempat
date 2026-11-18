package com.sempatapps.presentation.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import com.sempatapps.R
import com.sempatapps.core.designsystem.Canvas
import com.sempatapps.core.designsystem.IndicatorActive
import com.sempatapps.core.designsystem.Sora
import com.sempatapps.core.designsystem.Teal
import kotlinx.coroutines.delay

/**
 * Splash Screen. Figma: section "1. Akun & KTP / Splash Screen" (prototype 3 frame).
 *
 * 1. Layar kosong, logo masih di luar layar kanan (frame 789:2893).
 * 2. Logo meluncur ke tengah, garis oranye "melesat" keluar dari badan, daun tegak (789:2874).
 * 3. Logo naik sedikit dan tulisan "SemPat" muncul dari bawah (789:2853).
 */
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val currentOnFinished by rememberUpdatedState(onFinished)
    val enter = remember { Animatable(0f) }
    val reveal = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        delay(300)
        enter.animateTo(1f, tween(durationMillis = 700, easing = FastOutSlowInEasing))
        delay(150)
        reveal.animateTo(1f, tween(durationMillis = 500, easing = FastOutSlowInEasing))
        delay(800)
        currentOnFinished()
    }

    SplashContent(enter = enter.value, reveal = reveal.value)
}

/**
 * @param enter 0 = logo di luar layar kanan, 1 = logo di tengah.
 * @param reveal 0 = tulisan tersembunyi, 1 = tulisan tampil penuh.
 */
@Composable
fun SplashContent(enter: Float, reveal: Float) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Canvas),
        contentAlignment = Alignment.Center,
    ) {
        // Frame 1 menaruh logo tepat di luar tepi kanan layar; dihitung dari lebar layar
        // supaya di HP sempit pun logo tetap tidak kelihatan sebelum masuk.
        val offscreenX = maxWidth / 2 + LogoWidth / 2
        // Tengah logo di Figma: frame 1 = +26dp, frame 2 = +34dp, frame 3 = 0 dari tengah layar.
        val offsetY = lerp(lerp(26.dp, 34.dp, enter), 0.dp, reveal)

        SempatLogo(
            enter = enter,
            reveal = reveal,
            modifier = Modifier.offset(x = lerp(offscreenX, 0.dp, enter), y = offsetY),
        )
    }
}

private val LogoWidth = 224.dp
private val LogoHeight = 258.dp

/** Logo SemPat. Posisi tiap bagian diambil dari Figma (frame 224 x 258). */
@Composable
private fun SempatLogo(enter: Float, reveal: Float, modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(LogoWidth, LogoHeight).clipToBounds()) {
        Image(
            painter = painterResource(R.drawable.logo_body),
            contentDescription = null,
            modifier = Modifier.offset(0.dp, 81.dp).size(167.498.dp, 109.dp),
        )

        // Garis oranye: di frame 1 tersembunyi di dalam badan, lalu melesat ke kanan.
        Bar(from = Pos(84.dp, 81.dp), to = Pos(150.26.dp, 81.dp), width = 49.dp, height = 23.dp, t = enter)
        Bar(from = Pos(53.dp, 111.dp), to = Pos(193.dp, 107.dp), width = 31.dp, height = 15.dp, t = enter)
        Bar(from = Pos(59.dp, 166.dp), to = Pos(172.26.dp, 167.dp), width = 49.dp, height = 23.dp, t = enter)
        Bar(from = Pos(136.26.dp, 126.dp), to = Pos(136.26.dp, 126.dp), width = 49.dp, height = 20.dp, t = enter)

        // Daun: miring di frame 1, tegak di frame 2. Berputar di titik tengahnya.
        Image(
            painter = painterResource(R.drawable.logo_leaf_right),
            contentDescription = null,
            modifier = Modifier
                .offset(103.62.dp, 0.dp)
                .size(81.543.dp, 70.dp)
                .graphicsLayer { rotationZ = -9.63f * (1f - enter) },
        )
        Image(
            painter = painterResource(R.drawable.logo_leaf_left),
            contentDescription = null,
            modifier = Modifier
                .offset(22.dp, 0.dp)
                .size(81.543.dp, 70.dp)
                .graphicsLayer { rotationZ = 9.05f * (1f - enter) },
        )

        // Tulisan naik dari y=250 (terpotong batas logo) ke y=191.
        // Ukuran pakai dp, bukan sp: ini logo, jadi tidak ikut membesar dengan setelan font HP.
        val fontSize = with(LocalDensity.current) { 48.dp.toSp() }
        val letterSpacing = with(LocalDensity.current) { (-1.92).dp.toSp() }
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = Teal)) { append("Sem") }
                withStyle(SpanStyle(color = IndicatorActive)) { append("Pat") }
            },
            style = TextStyle(
                fontFamily = Sora,
                fontWeight = FontWeight.ExtraBold,
                fontSize = fontSize,
                letterSpacing = letterSpacing,
            ),
            softWrap = false,
            modifier = Modifier.offset(x = 17.dp, y = lerp(250.dp, 191.dp, reveal)),
        )
    }
}

private data class Pos(val x: Dp, val y: Dp)

@Composable
private fun Bar(from: Pos, to: Pos, width: Dp, height: Dp, t: Float) {
    Box(
        modifier = Modifier
            .offset(x = lerp(from.x, to.x, t), y = lerp(from.y, to.y, t))
            .size(width, height)
            .background(IndicatorActive, RoundedCornerShape(24.dp)),
    )
}

@Preview(name = "Frame 2: logo di tengah", widthDp = 402, heightDp = 874)
@Composable
private fun SplashLogoPreview() {
    SplashContent(enter = 1f, reveal = 0f)
}

@Preview(name = "Frame 3: logo + tulisan", widthDp = 402, heightDp = 874)
@Composable
private fun SplashFinalPreview() {
    SplashContent(enter = 1f, reveal = 1f)
}
