package com.sempatapps.ai.ktp

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.common.MlKitException
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.sempatapps.domain.model.KtpData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.roundToInt

/**
 * Alur baca KTP: foto → [KtpDetector] (kotak tiap kolom) → crop → ML Kit OCR → [KtpParser] → [KtpData].
 *
 * Model dimuat saat pertama kali dipakai, lalu disimpan selama aplikasi hidup.
 */
@Singleton
class KtpReader @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val detector by lazy { KtpDetector(context) }
    private val recognizer: TextRecognizer by lazy { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }
    private val parser = KtpParser()

    /** Foto harus sudah tegak (rotasi EXIF/kamera sudah diterapkan). */
    suspend fun read(photo: Bitmap): KtpData {
        val boxes = withContext(Dispatchers.Default) { detector.detect(photo) }
            // Kolom yang tidak dipakai form (golongan darah, agama, dst.) tidak perlu di-OCR.
            .filter { it.label in KtpParser.LABEL_DIPAKAI }
        val detections = boxes.map { box ->
            val crop = crop(photo, box)
            val text = try {
                recognize(crop)
            } catch (e: MlKitException) {
                // Satu kolom gagal dibaca jangan menggagalkan semuanya; kolom kosong ditandai perluDicek oleh parser.
                Log.w(TAG, "OCR gagal untuk ${box.label}", e)
                ""
            } finally {
                // createBitmap bisa mengembalikan foto aslinya kalau crop = seluruh foto; jangan ikut dihapus.
                if (crop !== photo) crop.recycle()
            }
            KtpDetection(label = box.label, text = text, confidence = box.confidence)
        }
        return parser.parse(detections)
    }

    /** Crop dengan sedikit kelonggaran supaya huruf di tepi kotak tidak terpotong, lalu perbesar kalau terlalu kecil. */
    private fun crop(photo: Bitmap, box: DetectedBox): Bitmap {
        val marginX = (box.right - box.left) * CROP_MARGIN
        val marginY = (box.bottom - box.top) * CROP_MARGIN
        val left = (box.left - marginX).roundToInt().coerceIn(0, photo.width - 1)
        val top = (box.top - marginY).roundToInt().coerceIn(0, photo.height - 1)
        val right = (box.right + marginX).roundToInt().coerceIn(left + 1, photo.width)
        val bottom = (box.bottom + marginY).roundToInt().coerceIn(top + 1, photo.height)
        val cropped = Bitmap.createBitmap(photo, left, top, right - left, bottom - top)

        val size = ocrSizeFor(cropped.width, cropped.height, minSide = MIN_OCR_SIDE)
        if (size.width == cropped.width && size.height == cropped.height) return cropped
        val scaled = Bitmap.createScaledBitmap(cropped, size.width, size.height, true)
        if (cropped !== photo) cropped.recycle()
        return scaled
    }

    private suspend fun recognize(bitmap: Bitmap): String = suspendCancellableCoroutine { cont ->
        recognizer.process(InputImage.fromBitmap(bitmap, 0))
            .addOnSuccessListener { cont.resume(it.text) }
            .addOnFailureListener { cont.resumeWithException(it) }
    }

    private companion object {
        const val TAG = "KtpReader"
        const val CROP_MARGIN = 0.08f
        const val MIN_OCR_SIDE = 64
    }
}
