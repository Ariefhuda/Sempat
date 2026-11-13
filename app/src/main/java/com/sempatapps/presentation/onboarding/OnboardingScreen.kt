package com.sempatapps.presentation.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sempatapps.R
import com.sempatapps.core.designsystem.BodyBold
import com.sempatapps.core.designsystem.Canvas
import com.sempatapps.core.designsystem.Heading2
import com.sempatapps.core.designsystem.IndicatorActive
import com.sempatapps.core.designsystem.IndicatorInactive
import com.sempatapps.core.designsystem.component.PrimaryButton
import com.sempatapps.core.designsystem.component.SecondaryButton

data class OnboardingPage(
    val imageRes: Int,
    val title: String,
    val description: String,
)

val onboardingPages = listOf(
    OnboardingPage(
        imageRes = R.drawable.icon_splash1,
        title = "Setiap Pangan\nPunya Kesempatan",
        description = "Jangan biarkan pangan layak konsumsi berakhir menjadi limbah",
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

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Canvas)
            .statusBarsPadding(),
    ) {
        // Ukuran Figma dibuat untuk layar tinggi 874dp. Di layar pendek jaraknya dirapatkan
        // dan ilustrasi mengecil, supaya judul, deskripsi, dan tombol tetap kelihatan.
        val compact = maxHeight < 760.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(if (compact) 16.dp else 38.dp))
            Image(
                painter = painterResource(R.drawable.sempat_logo),
                contentDescription = "Logo SemPat",
            )

            Spacer(Modifier.height(if (compact) 24.dp else 86.dp))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.Top,
            ) { index ->
                OnboardingContent(page = onboardingPages[index])
            }

            Spacer(Modifier.height(19.dp))

            PageIndicator(
                pageCount = onboardingPages.size,
                currentPage = pagerState.currentPage,
            )

            Spacer(Modifier.height(if (compact) 24.dp else 35.dp))

            PrimaryButton(text = "Masuk", onClick = onLoginClick)
            Spacer(Modifier.height(11.dp))
            SecondaryButton(text = "Daftar/Buat Akun", onClick = onRegisterClick)

            Spacer(Modifier.height(if (compact) 24.dp else 60.dp))
        }
    }
}

@Composable
fun OnboardingContent(page: OnboardingPage) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Kotak ilustrasi tingginya sama di semua halaman (maks. 263dp, ilustrasi terbesar di Figma)
        // supaya teks tidak loncat saat digeser. weight: kotak ini yang mengalah kalau ruang kurang.
        Box(
            modifier = Modifier
                .weight(1f)
                .heightIn(max = 263.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(page.imageRes),
                contentDescription = null, // dekoratif; judul di bawahnya sudah menjelaskan
                contentScale = ContentScale.Fit,
            )
        }

        Spacer(Modifier.height(36.dp))

        // Tinggi minimum sama untuk semua halaman (judul 2 baris + deskripsi 3 baris),
        // supaya posisi teks tidak berubah walau deskripsinya beda panjang.
        Column(
            modifier = Modifier
                .padding(horizontal = 33.dp)
                .heightIn(min = 152.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = page.title,
                style = Heading2,
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
                targetValue = if (isActive) 19.dp else 8.dp,
                label = "dotWidth",
            )
            val color by animateColorAsState(
                targetValue = if (isActive) IndicatorActive else IndicatorInactive,
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

@Preview(showBackground = true, widthDp = 402, heightDp = 874)
@Composable
private fun OnboardingScreenPreview() {
    OnboardingScreen(onLoginClick = {}, onRegisterClick = {})
}
