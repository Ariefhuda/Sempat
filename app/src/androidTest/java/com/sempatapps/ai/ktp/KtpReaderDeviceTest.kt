package com.sempatapps.ai.ktp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * Uji model KTP di perangkat/emulator sungguhan (LiteRT + ML Kit tidak bisa jalan di JVM biasa).
 *
 * Memakai `ktp_sintetis.png`: KTP buatan dengan data karangan, bukan data orang sungguhan.
 * Gambar dengan kotak deteksi disimpan ke getExternalFilesDir untuk dilihat manual.
 */
@RunWith(AndroidJUnit4::class)
class KtpReaderDeviceTest {

    private val appContext = InstrumentationRegistry.getInstrumentation().targetContext
    private val testContext = InstrumentationRegistry.getInstrumentation().context

    private fun syntheticKtp(): Bitmap =
        testContext.assets.open("ktp_sintetis.png").use { BitmapFactory.decodeStream(it) }

    @Test
    fun modelTermuatDanFotoKosongTidakMenghasilkanKolom() {
        KtpDetector(appContext).use { detector ->
            val blank = Bitmap.createBitmap(1280, 800, Bitmap.Config.ARGB_8888).apply { eraseColor(Color.GRAY) }

            val boxes = detector.detect(blank)

            assertTrue("Foto kosong seharusnya tidak berisi kolom KTP, dapat: $boxes", boxes.none { it.confidence > 0.5f })
        }
    }

    @Test
    fun kolomNikTerdeteksiDiBarisNik() {
        val photo = syntheticKtp()
        val boxes = KtpDetector(appContext).use { detector ->
            val start = System.nanoTime()
            detector.detect(photo).also {
                Log.i(TAG, "detect ${(System.nanoTime() - start) / 1_000_000} ms, ${it.size} kotak")
            }
        }
        boxes.sortedBy { it.top }.forEach {
            Log.i(TAG, "%-15s %.2f  [%.0f, %.0f, %.0f, %.0f]".format(it.label, it.confidence, it.left, it.top, it.right, it.bottom))
        }
        saveAnnotated(photo, boxes)

        val nik = boxes.firstOrNull { it.label == "nik" }
        assertTrue("Kolom NIK tidak terdeteksi. Kotak: ${boxes.map { it.label }}", nik != null)
        // Di gambar sintetis, angka NIK ada di sekitar y = 120..165.
        val centerY = (nik!!.top + nik.bottom) / 2f
        assertTrue("Kotak NIK salah tempat (tengah y = $centerY)", centerY in 110f..180f)
    }

    @Test
    fun ktpSintetisTerbacaSampaiKeKtpData() = runBlocking {
        val data = KtpReader(appContext).read(syntheticKtp())
        Log.i(TAG, "KtpData = $data")

        assertEquals("3174011203990002", data.nik)
        assertEquals("CONTOH SINTETIS", data.nama)
        assertEquals("12-03-1999", data.tanggalLahir)
        assertEquals("KOTA JAKARTA SELATAN", data.kabupatenKota)
        assertTrue("tidak ada yang perlu dicek, dapat ${data.perluDicek}", data.perluDicek.isEmpty())
    }

    private fun saveAnnotated(photo: Bitmap, boxes: List<DetectedBox>) {
        val out = photo.copy(Bitmap.Config.ARGB_8888, true)
        val stroke = Paint().apply { style = Paint.Style.STROKE; strokeWidth = 3f; color = Color.RED }
        val text = Paint().apply { color = Color.RED; textSize = 18f; isAntiAlias = true }
        Canvas(out).apply {
            boxes.forEach {
                drawRect(it.left, it.top, it.right, it.bottom, stroke)
                drawText("${it.label} ${"%.2f".format(it.confidence)}", it.left, it.top - 4f, text)
            }
        }
        val file = File(appContext.getExternalFilesDir(null), "ktp_deteksi.png")
        file.outputStream().use { out.compress(Bitmap.CompressFormat.PNG, 100, it) }
        Log.i(TAG, "Gambar deteksi: ${file.absolutePath}")
    }

    private companion object {
        const val TAG = "KtpReaderDeviceTest"
    }
}
