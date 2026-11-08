package com.sempatapps.presentation.onboarding

import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sempatapps.R
import com.sempatapps.core.designsystem.BodyBold
import com.sempatapps.core.designsystem.Canvas
import com.sempatapps.core.designsystem.HeadingLarge
import androidx.compose.runtime.getValue

data class OnboardingPage(
    val imageRes: Int,
    val title: String,
    val description: String,
)

val onboardingPages = listOf(
    OnboardingPage(
        imageRes = R.drawable.icon_splash1,
        title = "Setiap Pangan\nPunya Kesempatan",
        description = "Jangan biarkan pangan layak konsumsi\nberakhir menjadi limbah",
    ),
    OnboardingPage(
        imageRes = R.drawable.icon_splash2,
        title = "Foto Kenali\nSalurkan",
        description = "Cukup foto pangan, SEMPAT membantu menentukan pilihan terbaik",
    ),
    OnboardingPage(
        imageRes = R.drawable.icon_splash3,
        title = "Pangan Tepat\nManfaat Hebat",
        description = "Temukan penerima yang tepat di sekitar Anda",
    ),
)
@Composable
fun OnboardingScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Canvas)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(92.dp))
        Image(
            painter = painterResource(R.drawable.sempat_logo),
            contentDescription = "Logo SemPat",
            modifier = Modifier.size(width = 117.dp, height = 28.dp),
            contentScale = ContentScale.FillBounds,
        )

        Spacer(Modifier.height(60.dp))

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.Top,
        ) { index ->
            OnboardingContent(page = onboardingPages[index])
        }

        Spacer(Modifier.height(36.dp))

        PageIndicator(
            pageCount = onboardingPages.size,
            currentPage = pagerState.currentPage,
        )

        Spacer(Modifier.height(40.dp))

        Column(
            modifier = Modifier.padding(bottom = 80.dp),
        ) {
            LoginButton(text = "Masuk", onClick = onLoginClick)
            Spacer(Modifier.height(11.dp))
            RegisterButton(text = "Daftar/Buat Akun", onClick = onRegisterClick)
        }
    }
}
@Composable
fun OnboardingContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(page.imageRes),
            contentDescription = page.title,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
        )

        Spacer(Modifier.height(36.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = page.title,
                style = HeadingLarge,
                textAlign = TextAlign.Center,
            )
            Text(
                text = page.description,
                style = BodyBold,
                textAlign = TextAlign.Center,
            )
        }
    }
}
@Composable
fun PageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { index ->
            val isActive = index == currentPage

            val width by animateDpAsState(
                targetValue = if (isActive) 24.dp else 8.dp,
                label = "dotWidth",
            )
            val color by animateColorAsState(
                targetValue = if (isActive) Color(0xFFFF8A00) else Color(0xFFD9D9D9),
                label = "dotColor",
            )

            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(width)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

@Composable
fun LoginButton(
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
            .shadow(
                elevation = 4.dp,
                shape = shape,
                spotColor = Color(0x26000000),
                ambientColor = Color(0x26000000),
            ),
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF0E7C66),
            contentColor = Color.White,
        ),
        contentPadding = PaddingValues(10.dp),
    ) {
        Text(text = text, style = BodyBold, color = Color.White)
    }
}
@Composable
fun RegisterButton(
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
@Preview(showBackground = true, widthDp = 402, heightDp = 874)
@Composable
private fun OnboardingScreenPreview() {
    OnboardingScreen(onLoginClick = {}, onRegisterClick = {})
}