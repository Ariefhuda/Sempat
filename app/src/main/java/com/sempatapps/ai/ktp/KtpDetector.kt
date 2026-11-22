package com.sempatapps.ai.ktp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import org.tensorflow.lite.Interpreter
import java.io.Closeable
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

/**
 * Menjalankan `ktp_detector.tflite` (YOLO, input [1, 640, 640, 3] RGB 0..1, output [1, 21, 8400])
 * dan mengembalikan kotak tiap kolom KTP dalam koordinat foto asli.
 *
 * Interpreter tidak aman dipakai dari beberapa thread sekaligus, jadi [detect] disinkronkan.
 * Panggil dari background thread; satu deteksi butuh puluhan sampai ratusan milidetik.
 */
class KtpDetector(
    context: Context,
    modelAsset: String = MODEL_ASSET,
    labelsAsset: String = LABELS_ASSET,
    confidenceThreshold: Float = 0.25f,
) : Closeable {

    private val labels: List<String> =
        context.assets.open(labelsAsset).bufferedReader().readLines().map { it.trim() }.filter { it.isNotEmpty() }

    private val interpreter = Interpreter(
        loadModel(context, modelAsset),
        Interpreter.Options().setNumThreads(4),
    )

    private val decoder = YoloDecoder(labels, confidenceThreshold = confidenceThreshold)
    private val inputSize = Letterbox.INPUT_SIZE
    private val numCandidates: Int
    private val outputRows: Int

    init {
        val shape = interpreter.getOutputTensor(0).shape() // [1, 4 + kelas, N]
        outputRows = shape[1]
        numCandidates = shape[2]
        check(outputRows == 4 + labels.size) {
            "Model punya ${outputRows - 4} kelas, tapi $labelsAsset berisi ${labels.size} label"
        }
    }

    @Synchronized
    fun detect(photo: Bitmap): List<DetectedBox> {
        val letterbox = Letterbox(photo.width, photo.height, inputSize)
        val input = toInputBuffer(photo, letterbox)
        val output = Array(1) { Array(outputRows) { FloatArray(numCandidates) } }
        interpreter.run(input, output)

        val flat = FloatArray(outputRows * numCandidates)
        output[0].forEachIndexed { row, values -> values.copyInto(flat, row * numCandidates) }
        return decoder.decode(flat, numCandidates, letterbox)
    }

    /** Letterbox seperti ultralytics: perkecil dengan rasio tetap, bantalan abu-abu (114) di tengah. */
    private fun toInputBuffer(photo: Bitmap, letterbox: Letterbox): ByteBuffer {
        val canvasBitmap = Bitmap.createBitmap(inputSize, inputSize, Bitmap.Config.ARGB_8888)
        Canvas(canvasBitmap).apply {
            drawColor(Color.rgb(114, 114, 114))
            val dst = RectF(
                letterbox.padX,
                letterbox.padY,
                letterbox.padX + photo.width * letterbox.scale,
                letterbox.padY + photo.height * letterbox.scale,
            )
            drawBitmap(photo, null, dst, Paint(Paint.FILTER_BITMAP_FLAG))
        }

        val pixels = IntArray(inputSize * inputSize)
        canvasBitmap.getPixels(pixels, 0, inputSize, 0, 0, inputSize, inputSize)
        canvasBitmap.recycle()

        val buffer = ByteBuffer.allocateDirect(4 * 3 * pixels.size).order(ByteOrder.nativeOrder())
        for (p in pixels) {
            buffer.putFloat(((p shr 16) and 0xFF) / 255f)
            buffer.putFloat(((p shr 8) and 0xFF) / 255f)
            buffer.putFloat((p and 0xFF) / 255f)
        }
        buffer.rewind()
        return buffer
    }

    override fun close() = interpreter.close()

    companion object {
        const val MODEL_ASSET = "ktp_detector.tflite"
        const val LABELS_ASSET = "ktp_labels.txt"

        private fun loadModel(context: Context, asset: String): MappedByteBuffer =
            context.assets.openFd(asset).use { fd ->
                FileInputStream(fd.fileDescriptor).channel.use { channel ->
                    channel.map(FileChannel.MapMode.READ_ONLY, fd.startOffset, fd.declaredLength)
                }
            }
    }
}
