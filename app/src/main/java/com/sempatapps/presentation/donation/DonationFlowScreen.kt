package com.sempatapps.presentation.donation

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.runtime.DisposableEffect
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.sempatapps.ai.food.FoodAiAnalyzer
import java.io.File
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sempatapps.R
import com.sempatapps.core.designsystem.Canvas as CanvasBg
import com.sempatapps.core.designsystem.DisabledContent
import com.sempatapps.core.designsystem.DividerGray
import com.sempatapps.core.designsystem.FieldBorder
import com.sempatapps.core.designsystem.Figtree
import com.sempatapps.core.designsystem.Heading2
import com.sempatapps.core.designsystem.IndicatorActive
import com.sempatapps.core.designsystem.Ink
import com.sempatapps.core.designsystem.MatchOrange
import com.sempatapps.core.designsystem.Muted
import com.sempatapps.core.designsystem.PaleTeal
import com.sempatapps.core.designsystem.Placeholder
import com.sempatapps.core.designsystem.Primary
import com.sempatapps.core.designsystem.PrimaryDark
import com.sempatapps.core.designsystem.PrimaryLightActive
import com.sempatapps.core.designsystem.PrimaryLightHover
import com.sempatapps.core.designsystem.SecondaryDark
import com.sempatapps.core.designsystem.Sora
import com.sempatapps.core.designsystem.SuccessTeal
import com.sempatapps.core.designsystem.SurfaceLight
import com.sempatapps.core.designsystem.component.BackButton
import com.sempatapps.core.designsystem.component.PrimaryButton
import com.sempatapps.core.designsystem.component.SecondaryButton
import com.sempatapps.domain.model.Donation
import com.sempatapps.domain.model.DonationRecipient
import com.sempatapps.domain.model.FoodCheckResult
import com.sempatapps.presentation.home.photoRes
import kotlinx.coroutines.delay

@Composable
fun DonationFlowScreen(
    defaultPickupAddress: String = "Alamat profil donatur",
    onCloseToHome: () -> Unit,
    onDonationCreated: (Donation, String?) -> Unit,
    onRecipientAssigned: (Donation, String) -> Unit = { _, _ -> },
    onViewDonationDetail: (Donation) -> Unit
) {
    var state by remember { mutableStateOf(DonationFlowState()) }

    val buildDonation: (String) -> Donation = { status ->
        val id = state.savedDonationId ?: "d_${System.currentTimeMillis()}"
        Donation(
            id = id,
            name = state.foodName.ifBlank { "Roti Enak" },
            category = state.selectedCategory?.label ?: "Roti & Kue",
            quantity = state.compactWeight.ifBlank { "250g" },
            pickupAddress = defaultPickupAddress,
            distance = state.selectedRecipient?.let { "${it.distanceKm} km" } ?: "1,5 km",
            imageName = state.selectedPhotoName ?: "bread_photo",
            status = status,
            imageUri = state.selectedPhotoUri
        )
    }

    val buildCheckResult: () -> FoodCheckResult = {
        FoodCheckResult(
            name = state.foodName.ifBlank { "Roti Enak" },
            category = state.selectedCategory?.label ?: "Roti & Kue",
            weightLabel = state.formattedWeight.ifBlank { "250gram" },
            condition = "Layak",
            shelfLife = when (state.selectedCategory) {
                FoodCategory.MAKANAN_SIAP_SAJI -> "1 Hari"
                FoodCategory.KEBUTUHAN_POKOK -> "7 Hari"
                else -> "2 Hari"
            },
            isEligible = true,
            recommendation = "Kondisi pangan masih baik dan cocok didonasikan ke yang membutuhkan",
            imageName = state.selectedPhotoName ?: "bread_photo",
            imageUri = state.selectedPhotoUri
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CanvasBg)
    ) {
        when (state.step) {
            DonationStep.FORM -> DonationFormScreen(
                state = state,
                onBack = {
                    if (state.hasUnsavedInput) {
                        state = state.copy(dialog = DonationDialogType.CANCEL_DONATION)
                    } else {
                        onCloseToHome()
                    }
                },
                onTakePhotoClick = {
                    state = state.copy(step = DonationStep.CAMERA)
                },
                onFoodNameChange = { state = state.copy(foodName = it) },
                onWeightChange = { state = state.copy(weightInput = it) },
                onToggleUnit = {
                    val nextUnit = if (state.weightUnit == WeightUnit.KG) WeightUnit.GRAM else WeightUnit.KG
                    state = state.copy(weightUnit = nextUnit)
                },
                onSelectCategory = { state = state.copy(selectedCategory = it) },
                onCheckWithAi = {
                    state = state.copy(step = DonationStep.AI_CHECKING)
                }
            )

            DonationStep.CAMERA -> CameraCaptureScreen(
                onBack = { state = state.copy(step = DonationStep.FORM) },
                onPhotoReady = { uriString ->
                    state = state.copy(
                        pendingPhotoUri = uriString,
                        step = DonationStep.PHOTO_PREVIEW
                    )
                }
            )

            DonationStep.PHOTO_PREVIEW -> PhotoPreviewScreen(
                imageUri = state.pendingPhotoUri,
                fallbackImageName = state.pendingPhotoName ?: "bread_photo",
                onRetake = { state = state.copy(step = DonationStep.CAMERA) },
                onContinue = {
                    state = state.copy(
                        selectedPhotoUri = state.pendingPhotoUri,
                        selectedPhotoName = state.pendingPhotoName ?: "bread_photo",
                        step = DonationStep.FORM
                    )
                }
            )

            DonationStep.AI_CHECKING -> AiCheckingScreen(
                state = state,
                onSuccess = { result ->
                    state = state.copy(
                        checkResult = result,
                        step = DonationStep.RESULT
                    )
                },
                onBusy = {
                    state = state.copy(step = DonationStep.AI_BUSY)
                }
            )

            DonationStep.AI_BUSY -> AiBusyScreen(
                onRetry = { state = state.copy(step = DonationStep.AI_CHECKING) },
                onManualFill = {
                    state = state.copy(
                        checkResult = buildCheckResult(),
                        step = DonationStep.RESULT
                    )
                }
            )

            DonationStep.RESULT -> FoodCheckResultScreen(
                result = state.checkResult ?: buildCheckResult(),
                onBack = {
                    state = state.copy(dialog = DonationDialogType.CANCEL_DONATION)
                },
                onDonateClick = {
                    val donation = buildDonation("Aktif")
                    onDonationCreated(donation, null)
                    state = state.copy(
                        savedDonationId = donation.id,
                        dialog = DonationDialogType.DONATION_SAVED
                    )
                }
            )

            DonationStep.RECIPIENT_SELECTION -> RecipientSelectionScreen(
                recipients = sampleDonationRecipients,
                sort = state.recipientSort,
                onBack = {
                    state = state.copy(dialog = DonationDialogType.EXIT_RECIPIENT)
                },
                onSortChange = { state = state.copy(recipientSort = it) },
                onSelectRecipient = { recipient ->
                    val updated = buildDonation("Didonasikan")
                    onRecipientAssigned(updated, recipient.name)
                    state = state.copy(
                        selectedRecipient = recipient,
                        savedDonationId = updated.id,
                        dialog = DonationDialogType.DISTRIBUTED_SUCCESS
                    )
                }
            )
        }
    }

    when (state.dialog) {
        DonationDialogType.NONE -> Unit

        DonationDialogType.CANCEL_DONATION -> CancelDonationDialog(
            onBack = { state = state.copy(dialog = DonationDialogType.NONE) },
            onConfirmCancel = {
                state = state.copy(dialog = DonationDialogType.NONE)
                onCloseToHome()
            }
        )

        DonationDialogType.DONATION_SAVED -> DonationSavedDialog(
            onGoHome = {
                state = state.copy(dialog = DonationDialogType.NONE)
                onCloseToHome()
            },
            onSelectRecipient = {
                state = state.copy(
                    dialog = DonationDialogType.NONE,
                    step = DonationStep.RECIPIENT_SELECTION
                )
            }
        )

        DonationDialogType.EXIT_RECIPIENT -> ExitRecipientDialog(
            onExit = {
                state = state.copy(dialog = DonationDialogType.NONE)
                onCloseToHome()
            },
            onStay = { state = state.copy(dialog = DonationDialogType.NONE) }
        )

        DonationDialogType.DISTRIBUTED_SUCCESS -> DistributedSuccessDialog(
            onViewDetail = {
                val finalDonation = buildDonation("Didonasikan")
                state = state.copy(dialog = DonationDialogType.NONE)
                onViewDonationDetail(finalDonation)
            }
        )
    }
}

@Composable
internal fun DonationBackHeader(
    title: String?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BackButton(onBackClick = onBack)
        if (!title.isNullOrBlank()) {
            Spacer(Modifier.width(17.dp))
            Text(
                text = title,
                style = Heading2,
                color = Ink
            )
        }
    }
}

@Composable
private fun DonationFormScreen(
    state: DonationFlowState,
    onBack: () -> Unit,
    onTakePhotoClick: () -> Unit,
    onFoodNameChange: (String) -> Unit,
    onWeightChange: (String) -> Unit,
    onToggleUnit: () -> Unit,
    onSelectCategory: (FoodCategory) -> Unit,
    onCheckWithAi: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CanvasBg)
            .navigationBarsPadding()
    ) {
        DonationBackHeader(title = "Donasi Pangan", onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = "Foto Panganmu",
                color = PrimaryDark,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Foto pangan dengan jelas SEMPAT akan membantu mengenali kondisinya",
                color = Ink,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
            )

            // Photo Upload / Preview Box (362x235, rounded 24dp, border #0E7C66)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(235.dp)
                    .clickable(onClick = onTakePhotoClick),
                shape = RoundedCornerShape(24.dp),
                color = CanvasBg,
                border = BorderStroke(1.dp, Primary),
                shadowElevation = 2.dp
            ) {
                if (!state.hasPhoto) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(Primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CameraAlt,
                                contentDescription = "Ambil Foto Pangan",
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = "Ambil Foto Pangan",
                            color = Ink,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize()) {
                        FoodPhotoImage(
                            photoUri = state.selectedPhotoUri,
                            fallbackName = state.selectedPhotoName ?: "bread_photo",
                            contentDescription = "Foto pangan",
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.4f))
                        )
                        Button(
                            onClick = onTakePhotoClick,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(14.dp)
                                .height(46.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Primary,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 20.dp)
                        ) {
                            Text(
                                text = "Ambil Ulang",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Nama Pangan
            Text(
                text = "Nama Pangan",
                color = PrimaryDark,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            DonationInputBox(
                value = state.foodName,
                onValueChange = onFoodNameChange,
                placeholder = "contoh : Tomat Segar"
            )

            Spacer(Modifier.height(14.dp))

            // Berat Pangan + Unit Selector
            Text(
                text = "Berat Pangan",
                color = PrimaryDark,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(1.dp, FieldBorder, RoundedCornerShape(12.dp))
                    .padding(start = 12.dp, end = 10.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        if (state.weightInput.isEmpty()) {
                            Text(
                                text = "contoh : 250g",
                                color = Color(0xFF767474),
                                fontSize = 14.sp
                            )
                        }
                        BasicTextField(
                            value = state.weightInput,
                            onValueChange = onWeightChange,
                            singleLine = true,
                            textStyle = TextStyle(color = Ink, fontSize = 15.sp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Surface(
                        onClick = onToggleUnit,
                        shape = RoundedCornerShape(24.dp),
                        color = CanvasBg,
                        border = BorderStroke(1.dp, PrimaryDark),
                        shadowElevation = 2.dp,
                        modifier = Modifier.height(38.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                                contentDescription = "Ganti satuan",
                                tint = PrimaryDark,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = state.weightUnit.shortLabel,
                                color = Primary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                contentDescription = "Ganti satuan",
                                tint = PrimaryDark,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Kategori Pangan
            Text(
                text = "Kategori Pangan",
                color = PrimaryDark,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(11.dp)
            ) {
                FoodCategory.entries.forEach { category ->
                    val isSelected = state.selectedCategory == category
                    Surface(
                        onClick = { onSelectCategory(category) },
                        modifier = Modifier
                            .width(120.dp)
                            .height(140.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) PaleTeal else Color.White,
                        border = BorderStroke(if (isSelected) 2.dp else 1.dp, Primary),
                        shadowElevation = 4.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 10.dp, vertical = 14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Image(
                                painter = painterResource(category.iconRes),
                                contentDescription = category.label,
                                modifier = Modifier.size(62.dp),
                                contentScale = ContentScale.Fit
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = category.label,
                                color = Color.Black,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Bottom CTA: incomplete banner vs primary AI check button
            if (!state.isFormComplete) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(PrimaryLightActive),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Lengkapi data pangan untuk melanjutkan",
                        color = PrimaryDark,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                PrimaryButton(
                    text = "Cek Pangan dengan AI",
                    onClick = onCheckWithAi
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "SEMPAT dapat melakukan kesalahan. Pastikan hasil verifikasi sesuai dengan kondisi pangan sebenarnya",
                    color = Color.Black,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 19.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DonationInputBox(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, FieldBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        if (value.isEmpty()) {
            Text(
                text = placeholder,
                color = Color(0xFF767474),
                fontSize = 14.sp
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(color = Ink, fontSize = 15.sp),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun FoodPhotoImage(
    photoUri: String?,
    fallbackName: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    if (!photoUri.isNullOrBlank()) {
        AsyncImage(
            model = Uri.parse(photoUri),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = ContentScale.Crop,
        )
    } else {
        Image(
            painter = painterResource(photoRes(fallbackName)),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = ContentScale.Crop,
        )
    }
}

@Composable
private fun CameraCaptureScreen(
    onBack: () -> Unit,
    onPhotoReady: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    var isCapturing by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onPhotoReady(uri.toString())
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val controller = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(CameraController.IMAGE_CAPTURE)
            imageCaptureMode = ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY
        }
    }

    DisposableEffect(lifecycleOwner, hasCameraPermission) {
        if (hasCameraPermission) {
            controller.bindToLifecycle(lifecycleOwner)
        }
        onDispose {
            controller.unbind()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (hasCameraPermission) {
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                        this.controller = controller
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Izin kamera diperlukan untuk memotret pangan secara langsung.",
                    color = Color.White,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(14.dp))
                PrimaryButton(
                    text = "Izinkan Kamera",
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }
                )
            }
        }

        // Top Bar
        DonationBackHeader(
            title = "Ambil Foto",
            onBack = onBack,
            modifier = Modifier.align(Alignment.TopStart)
        )

        // Green Scan Corner Brackets (Figma "Scan Foto")
        if (hasCameraPermission) {
            Canvas(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(bottom = 80.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(440.dp)
            ) {
                val stroke = 4.dp.toPx()
                val cornerLen = 46.dp.toPx()
                val bracketColor = Primary

                drawLine(bracketColor, Offset(0f, 0f), Offset(cornerLen, 0f), strokeWidth = stroke, cap = StrokeCap.Round)
                drawLine(bracketColor, Offset(0f, 0f), Offset(0f, cornerLen), strokeWidth = stroke, cap = StrokeCap.Round)
                drawLine(bracketColor, Offset(size.width - cornerLen, 0f), Offset(size.width, 0f), strokeWidth = stroke, cap = StrokeCap.Round)
                drawLine(bracketColor, Offset(size.width, 0f), Offset(size.width, cornerLen), strokeWidth = stroke, cap = StrokeCap.Round)
                drawLine(bracketColor, Offset(0f, size.height - cornerLen), Offset(0f, size.height), strokeWidth = stroke, cap = StrokeCap.Round)
                drawLine(bracketColor, Offset(0f, size.height), Offset(cornerLen, size.height), strokeWidth = stroke, cap = StrokeCap.Round)
                drawLine(bracketColor, Offset(size.width, size.height - cornerLen), Offset(size.width, size.height), strokeWidth = stroke, cap = StrokeCap.Round)
                drawLine(bracketColor, Offset(size.width - cornerLen, size.height), Offset(size.width, size.height), strokeWidth = stroke, cap = StrokeCap.Round)
            }
        }

        // Bottom Controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Pilih dari Galeri (Android Photo Picker)
            Button(
                onClick = {
                    galleryLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                modifier = Modifier
                    .width(222.dp)
                    .height(44.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Image,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Pilih dari Galeri",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(10.dp))

            CameraTipBanner(
                text = "Pastikan foto yang kamu ambil terlihat jelas agar SEMPAT dapat medeteksi dengan baik",
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(Modifier.height(14.dp))

            // White Bottom Dock with Camera Shutter
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp),
                color = SurfaceLight,
                shadowElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(vertical = 18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 81.dp, height = 76.dp)
                            .clip(CircleShape)
                            .background(Primary)
                            .clickable(enabled = hasCameraPermission && !isCapturing) {
                                isCapturing = true
                                val photoFile = File(context.cacheDir, "pangan_${System.currentTimeMillis()}.jpg")
                                val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
                                controller.takePicture(
                                    outputOptions,
                                    ContextCompat.getMainExecutor(context),
                                    object : ImageCapture.OnImageSavedCallback {
                                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                            isCapturing = false
                                            val savedUri = outputFileResults.savedUri ?: Uri.fromFile(photoFile)
                                            onPhotoReady(savedUri.toString())
                                        }

                                        override fun onError(exception: ImageCaptureException) {
                                            isCapturing = false
                                            Toast.makeText(context, "Gagal mengambil foto, coba lagi", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCapturing) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(32.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.CameraAlt,
                                contentDescription = "Ambil foto",
                                tint = Color.White,
                                modifier = Modifier.size(44.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CameraTipBanner(text: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(6.dp),
        color = CanvasBg,
        shadowElevation = 3.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Error,
                contentDescription = null,
                tint = IndicatorActive,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = text,
                color = Ink,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun PhotoPreviewScreen(
    imageUri: String?,
    fallbackImageName: String,
    onRetake: () -> Unit,
    onContinue: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        FoodPhotoImage(
            photoUri = imageUri,
            fallbackName = fallbackImageName,
            contentDescription = "Pratinjau foto pangan",
            modifier = Modifier.fillMaxSize()
        )

        CameraTipBanner(
            text = "Pastikan foto yang kamu ambil terlihat jelas",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp)
        )

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SecondaryButton(
                text = "Ambil Ulang",
                onClick = onRetake,
                modifier = Modifier.weight(1f)
            )
            PrimaryButton(
                text = "Lanjutkan",
                onClick = onContinue,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun AiCheckingScreen(
    state: DonationFlowState,
    onSuccess: (FoodCheckResult) -> Unit,
    onBusy: () -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(state.selectedPhotoUri, state.foodName, state.weightInput, state.selectedCategory) {
        val fallbackName = state.selectedPhotoName ?: "bread_photo"
        val result = runCatching {
            FoodAiAnalyzer.analyzeFood(
                context = context,
                foodName = state.foodName.ifBlank { "Pangan Surplus" },
                category = state.selectedCategory?.label ?: "Lainnya",
                weightLabel = state.formattedWeight.ifBlank { "250gram" },
                imageUri = state.selectedPhotoUri,
                fallbackDrawableRes = photoRes(fallbackName),
                fallbackImageName = fallbackName,
            )
        }
        result.onSuccess(onSuccess).onFailure { onBusy() }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "ai_spinner")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing)
        ),
        label = "spinner_rotation"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CanvasBg)
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Canvas(modifier = Modifier.size(109.dp)) {
            val strokeWidth = 12.dp.toPx()
            val inset = strokeWidth / 2
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
            drawArc(
                color = PrimaryLightHover,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = strokeWidth)
            )
            rotate(degrees = angle) {
                drawArc(
                    color = SuccessTeal,
                    startAngle = -90f,
                    sweepAngle = 110f,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        Text(
            text = "AI Sedang Memeriksa Panganmu",
            color = PrimaryDark,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Menganalisis kondisi dan kelayakan pangan...",
            color = Ink,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
internal fun AiBusyScreen(
    onRetry: () -> Unit,
    onManualFill: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CanvasBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(1f))
        Image(
            painter = painterResource(R.drawable.ic_ai_busy_warning),
            contentDescription = "Peringatan",
            modifier = Modifier.size(96.dp),
            contentScale = ContentScale.Fit
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "AI sedang sibuk",
            color = Primary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "Banyak yang sedang memakai pemeriksaan AI. Coba lagi sebentar, atau isi data panganmu sendiri.",
            color = DisabledContent,
            fontSize = 16.sp,
            lineHeight = 23.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.weight(1.2f))
        PrimaryButton(
            text = "Coba Lagi",
            onClick = onRetry
        )
        Spacer(Modifier.height(12.dp))
        SecondaryButton(
            text = "Isi Data Sendiri",
            onClick = onManualFill
        )
    }
}

@Composable
private fun FoodCheckResultScreen(
    result: FoodCheckResult,
    onBack: () -> Unit,
    onDonateClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CanvasBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 124.dp)
                .verticalScroll(rememberScrollState())
        ) {
            DonationBackHeader(title = null, onBack = onBack)

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = "Hasil Cek Pangan",
                    color = PrimaryDark,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "AI telah menganalisis panganmu dan memberikan rekomendasi donasi",
                    color = Ink,
                    fontSize = 14.sp,
                    lineHeight = 21.sp
                )
                Spacer(Modifier.height(22.dp))
                Text(
                    text = "Kondisi Pangan",
                    color = Color.Black,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(10.dp))

                // Main Condition Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = CanvasBg,
                    shadowElevation = 6.dp
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        FoodPhotoImage(
                            photoUri = result.imageUri,
                            fallbackName = result.imageName,
                            contentDescription = result.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(202.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, Primary, RoundedCornerShape(12.dp))
                        )
                        Spacer(Modifier.height(16.dp))
                        ResultRow("Nama Pangan", result.name)
                        Spacer(Modifier.height(14.dp))
                        ResultRow("Kategori Pangan", result.category)
                        Spacer(Modifier.height(14.dp))
                        ResultRow("Berat", result.weightLabel)
                        Spacer(Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Kondisi", color = Color.Black, fontSize = 14.sp)
                            Box(
                                modifier = Modifier
                                    .width(102.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(PrimaryLightHover)
                                    .padding(vertical = 3.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = result.condition,
                                    color = PrimaryDark,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                        ResultRow("Masa Tahan", result.shelfLife)
                        Spacer(Modifier.height(20.dp))

                        // Badge kelayakan donasi
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (result.isEligible) SecondaryDark else Color(0xFFB3261E)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (result.isEligible) Icons.Rounded.ThumbUp else Icons.Rounded.Error,
                                    contentDescription = null,
                                    tint = PaleTeal,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = if (result.isEligible) "LAYAK DIDONASIKAN" else "PERLU DIPERIKSA ULANG",
                                    color = PaleTeal,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Rekomendasi SEMPAT Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = CanvasBg,
                    border = BorderStroke(1.dp, Primary),
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_recommendation_sempat),
                            contentDescription = "Rekomendasi SEMPAT",
                            modifier = Modifier.size(width = 61.dp, height = 67.dp),
                            contentScale = ContentScale.Fit
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Rekomendasi SEMPAT",
                                color = PrimaryDark,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = buildAnnotatedString {
                                    val rec = result.recommendation
                                    val idx = rec.indexOf("didonasikan", ignoreCase = true)
                                    if (idx >= 0) {
                                        append(rec.substring(0, idx))
                                        withStyle(SpanStyle(color = Primary, fontWeight = FontWeight.Medium)) {
                                            append(rec.substring(idx, idx + "didonasikan".length))
                                        }
                                        append(rec.substring(idx + "didonasikan".length))
                                    } else {
                                        append(rec)
                                    }
                                },
                                color = Ink,
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }

        // Sticky Bottom CTA Bar
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
            color = SurfaceLight,
            shadowElevation = 10.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PrimaryButton(
                    text = "Ayo Donasikan",
                    onClick = onDonateClick
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Pastikan kembali kondisi pangan sebelum didonasikan",
                    color = Color.Black,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ResultRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = Color.Black, fontSize = 14.sp)
        Text(text = value, color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun RecipientSelectionScreen(
    recipients: List<DonationRecipient>,
    sort: RecipientSort,
    onBack: () -> Unit,
    onSortChange: (RecipientSort) -> Unit,
    onSelectRecipient: (DonationRecipient) -> Unit
) {
    val sortedRecipients = remember(recipients, sort) {
        when (sort) {
            RecipientSort.NEAREST -> recipients.sortedBy { it.distanceKm }
            RecipientSort.MOST_NEEDED -> recipients.sortedByDescending { it.matchPercent }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CanvasBg)
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        DonationBackHeader(title = "Pilih Penerima", onBack = onBack)

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = "Pilih Penerima Donasi",
                color = PrimaryDark,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Berikut beberapa penerima yang sesuai dengan panganmu",
                color = Ink,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
            Spacer(Modifier.height(14.dp))
            Text(
                text = "${sortedRecipients.size} penerima ditemukan",
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Urutkan",
                color = Color.Black,
                fontSize = 14.sp
            )
            Spacer(Modifier.height(10.dp))

            // Filter Donasi Sort Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = CanvasBg,
                shadowElevation = 4.dp
            ) {
                Column {
                    SortOptionRow(
                        label = "Terdekat",
                        selected = sort == RecipientSort.NEAREST,
                        onClick = { onSortChange(RecipientSort.NEAREST) }
                    )
                    HorizontalDivider(color = DividerGray, thickness = 1.dp)
                    SortOptionRow(
                        label = "Paling Membutuhkan",
                        selected = sort == RecipientSort.MOST_NEEDED,
                        onClick = { onSortChange(RecipientSort.MOST_NEEDED) }
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            // 2-Column Grid of Recipient Cards
            sortedRecipients.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowItems.forEach { recipient ->
                        RecipientCard(
                            recipient = recipient,
                            onDonate = { onSelectRecipient(recipient) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowItems.size == 1) {
                        Spacer(Modifier.weight(1f))
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun SortOptionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (selected) Primary else CanvasBg)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(CircleShape)
                .background(if (selected) IndicatorActive else Color.Transparent)
                .border(
                    width = 1.5.dp,
                    color = if (selected) Color.White else Muted,
                    shape = CircleShape
                )
        )
        Text(
            text = label,
            color = if (selected) CanvasBg else Ink,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun RecipientCard(
    recipient: DonationRecipient,
    onDonate: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.padding(top = 9.dp)) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp),
            shape = RoundedCornerShape(12.dp),
            color = CanvasBg,
            border = BorderStroke(1.dp, Primary),
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 12.dp, end = 12.dp, top = 20.dp, bottom = 14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = recipient.type,
                        color = Color.Black,
                        fontSize = 13.sp
                    )
                    Text(
                        text = recipient.name,
                        color = Color.Black,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 22.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "Kebutuhan",
                        color = Color.Black,
                        fontSize = 13.sp
                    )
                    Text(
                        text = recipient.needCategory,
                        color = Primary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Cocok\nUntukmu",
                        color = Color.Black,
                        fontSize = 13.sp,
                        lineHeight = 17.sp
                    )
                    Text(
                        text = "${recipient.matchPercent}%",
                        color = MatchOrange,
                        fontSize = 23.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                SmallPrimaryButton(
                    text = "Donasikan",
                    onClick = onDonate,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                )
            }
        }

        // Top-right Distance Badge (#B84E08)
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(y = (-9).dp),
            shape = RoundedCornerShape(
                topStart = 12.dp,
                topEnd = 12.dp,
                bottomEnd = 12.dp,
                bottomStart = 0.dp
            ),
            color = SecondaryDark
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.LocationOn,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold)) {
                            append("${recipient.distanceKm} ")
                        }
                        withStyle(SpanStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal)) {
                            append("Km")
                        }
                    },
                    color = Color.White
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 402, heightDp = 874)
@Composable
private fun DonationFlowPreview() {
    DonationFlowScreen(
        onCloseToHome = {},
        onDonationCreated = { _, _ -> },
        onViewDonationDetail = {}
    )
}
