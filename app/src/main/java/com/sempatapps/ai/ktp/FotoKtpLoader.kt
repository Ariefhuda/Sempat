package com.sempatapps.ai.ktp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Urusan file foto KTP: tempat simpan sementara untuk aplikasi kamera, membaca jadi Bitmap
 * yang sudah tegak, dan menghapusnya lagi. Foto KTP tidak pernah dikirim ke server.
 */
@Singleton
class FotoKtpLoader @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val folder get() = File(context.cacheDir, "ktp").apply { mkdirs() }

    /** Uri kosong untuk diisi aplikasi kamera (ActivityResultContracts.TakePicture). */
    fun buatUriBaru(): Uri {
        val file = File(folder, "ktp_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    /** Baca foto, kecilkan kalau terlalu besar, lalu putar sesuai EXIF supaya tegak. */
    suspend fun muat(uri: Uri): Bitmap = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        val ukuran = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)!!.use { BitmapFactory.decodeStream(it, null, ukuran) }

        var sampleSize = 1
        while (maxOf(ukuran.outWidth, ukuran.outHeight) / (sampleSize * 2) >= SISI_MAKS) sampleSize *= 2
        val bitmap = resolver.openInputStream(uri)!!.use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sampleSize })
        } ?: error("Foto tidak bisa dibaca")

        val rotasi = resolver.openInputStream(uri)!!.use { ExifInterface(it).rotationDegrees }
        if (rotasi == 0) return@withContext bitmap
        val diputar = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, Matrix().apply { postRotate(rotasi.toFloat()) }, true)
        if (diputar !== bitmap) bitmap.recycle()
        diputar
    }

    /** Hapus semua foto KTP sementara. */
    fun hapusSemua() {
        folder.listFiles()?.forEach { it.delete() }
    }

    private companion object {
        // Cukup untuk OCR; foto kamera 12MP tidak perlu dimuat penuh ke memori.
        const val SISI_MAKS = 2000
    }
}
