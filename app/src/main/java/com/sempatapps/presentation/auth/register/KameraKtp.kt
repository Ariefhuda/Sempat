package com.sempatapps.presentation.auth.register

import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.sempatapps.ai.ktp.LEBAR_BINGKAI
import com.sempatapps.ai.ktp.RASIO_KTP
import com.sempatapps.ai.ktp.bingkaiDiGambar
import com.sempatapps.core.designsystem.Primary
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicReference

/**
 * Kamera foto KTP dengan bingkai bantuan.
 *
 * Tiap preview dipotong sesuai bingkai lalu diperiksa [deteksi] (model KTP, tanpa OCR).
 * Bingkai jadi hijau kalau KTP terlihat, jadi pengguna tahu kapan posisinya sudah pas.
 * Tombol potret tetap bisa ditekan kapan saja; foto disimpan ke [buatUri] lalu diteruskan ke [onFoto].
 */
@Composable
fun KameraKtp(
    deteksi: (Bitmap) -> Boolean,
    buatUri: () -> Uri,
    onFoto: (Uri) -> Unit,
    onTutup: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val deteksiTerbaru by rememberUpdatedState(deteksi)

    var terdeteksi by remember { mutableStateOf(false) }
    var memotret by remember { mutableStateOf(false) }
    // Dibaca dari thread analisis, jadi disimpan di AtomicReference, bukan state Compose.
    val ukuranLayar = remember { AtomicReference(IntSize.Zero) }
    val analisisExecutor = remember { Executors.newSingleThreadExecutor() }

    val controller = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(CameraController.IMAGE_CAPTURE or CameraController.IMAGE_ANALYSIS)
            imageCaptureMode = ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY
            imageAnalysisBackpressureStrategy = ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
            imageAnalysisOutputImageFormat = ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888
        }
    }

    DisposableEffect(lifecycleOwner) {
        controller.setImageAnalysisAnalyzer(analisisExecutor) { proxy ->
            try {
                val layar = ukuranLayar.get()
                if (layar.width > 0 && layar.height > 0) {
                    terdeteksi = periksaBingkai(proxy, layar, deteksiTerbaru)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Analisis bingkai gagal", e)
            } finally {
                proxy.close()
            }
        }
        controller.bindToLifecycle(lifecycleOwner)
        onDispose {
            controller.clearImageAnalysisAnalyzer()
            controller.unbind()
            analisisExecutor.shutdown()
        }
    }

    val warnaBingkai by animateColorAsState(if (terdeteksi) Primary else Color.White, label = "warnaBingkai")

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .onSizeChanged { ukuranLayar.set(it) },
    ) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    this.controller = controller
                }
            },
            modifier = Modifier.fillMaxSize(),
        )

        // Gelapkan semua kecuali bingkai KTP, lalu gambar garis bingkainya.
        Canvas(
            Modifier
                .fillMaxSize()
                .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen),
        ) {
            val lebar = size.width * LEBAR_BINGKAI
            val tinggi = lebar / RASIO_KTP
            val kiriAtas = Offset((size.width - lebar) / 2f, (size.height - tinggi) / 2f)
            val sudut = CornerRadius(16.dp.toPx())
            drawRect(Color.Black.copy(alpha = 0.55f))
            drawRoundRect(Color.Transparent, kiriAtas, Size(lebar, tinggi), sudut, blendMode = BlendMode.Clear)
            drawRoundRect(warnaBingkai, kiriAtas, Size(lebar, tinggi), sudut, style = Stroke(3.dp.toPx()))
        }

        Row(
            Modifier
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onTutup) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = Color.White)
            }
            Text("Foto KTP", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }

        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 32.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Text(
                text = if (terdeteksi) "KTP terdeteksi. Tahan, lalu ambil foto." else "Arahkan KTP ke dalam bingkai",
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (terdeteksi) Primary else Color.Black.copy(alpha = 0.6f))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(76.dp)
                    .border(4.dp, warnaBingkai, CircleShape)
                    .padding(8.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable(enabled = !memotret, role = Role.Button, onClickLabel = "Ambil foto") {
                        memotret = true
                        potret(
                            controller = controller,
                            context = context,
                            uri = buatUri(),
                            onBerhasil = onFoto,
                            onGagal = {
                                memotret = false
                                Toast.makeText(context, "Foto gagal diambil, coba lagi", Toast.LENGTH_SHORT).show()
                            },
                        )
                    },
            ) {
                if (memotret) CircularProgressIndicator(color = Primary, strokeWidth = 3.dp, modifier = Modifier.size(28.dp))
            }
        }
    }
}

/** Potong preview sesuai bingkai di layar, lalu tanya model apakah ada KTP di situ. */
private fun periksaBingkai(proxy: ImageProxy, layar: IntSize, deteksi: (Bitmap) -> Boolean): Boolean {
    val mentah = proxy.toBitmap()
    val rotasi = proxy.imageInfo.rotationDegrees
    val tegak = if (rotasi == 0) {
        mentah
    } else {
        Bitmap.createBitmap(mentah, 0, 0, mentah.width, mentah.height, Matrix().apply { postRotate(rotasi.toFloat()) }, true)
    }
    val p = bingkaiDiGambar(layar.width, layar.height, tegak.width, tegak.height)
    val potong = Bitmap.createBitmap(tegak, p.kiri, p.atas, p.lebar, p.tinggi)
    return try {
        deteksi(potong)
    } finally {
        if (potong !== tegak) potong.recycle()
        if (tegak !== mentah) tegak.recycle()
        mentah.recycle()
    }
}

private fun potret(
    controller: LifecycleCameraController,
    context: android.content.Context,
    uri: Uri,
    onBerhasil: (Uri) -> Unit,
    onGagal: () -> Unit,
) {
    val keluaran = context.contentResolver.openOutputStream(uri) ?: return onGagal()
    controller.takePicture(
        ImageCapture.OutputFileOptions.Builder(keluaran).build(),
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                keluaran.close()
                onBerhasil(uri)
            }

            override fun onError(exception: ImageCaptureException) {
                keluaran.close()
                Log.w(TAG, "Potret gagal", exception)
                onGagal()
            }
        },
    )
}

private const val TAG = "KameraKtp"
