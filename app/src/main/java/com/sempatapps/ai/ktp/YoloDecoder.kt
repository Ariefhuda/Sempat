package com.sempatapps.ai.ktp

import kotlin.math.max
import kotlin.math.min

/** Kotak hasil deteksi, dalam koordinat piksel foto asli. */
data class DetectedBox(
    val label: String,
    val confidence: Float,
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
)

/**
 * Cara foto asli dimasukkan ke input model yang persegi: diperkecil sampai muat
 * (rasio tetap), lalu sisa ruangnya diberi bantalan di tengah. Sama seperti
 * preprocessing ultralytics, supaya kotak yang keluar bisa dikembalikan ke foto asli.
 */
class Letterbox(
    val sourceWidth: Int,
    val sourceHeight: Int,
    val inputSize: Int = INPUT_SIZE,
) {
    val scale: Float = min(inputSize.toFloat() / sourceWidth, inputSize.toFloat() / sourceHeight)
    val padX: Float = (inputSize - sourceWidth * scale) / 2f
    val padY: Float = (inputSize - sourceHeight * scale) / 2f

    fun toSourceX(x: Float): Float = ((x - padX) / scale).coerceIn(0f, sourceWidth.toFloat())
    fun toSourceY(y: Float): Float = ((y - padY) / scale).coerceIn(0f, sourceHeight.toFloat())

    companion object {
        const val INPUT_SIZE = 640
    }
}

/**
 * Menerjemahkan output mentah model YOLO (ultralytics, diekspor ke TFLite) menjadi kotak berlabel.
 *
 * Output model berbentuk [1, 4 + jumlahKelas, N] (di KTP: [1, 21, 8400]) dan disimpan per baris:
 * `output[c * N + i]`. Baris 0–3 = cx, cy, w, h; baris 4 dst. = skor tiap kelas.
 * Model tidak menyertakan NMS, jadi kotak yang dobel dibuang di sini.
 */
class YoloDecoder(
    private val labels: List<String>,
    private val confidenceThreshold: Float = 0.25f,
    private val iouThreshold: Float = 0.45f,
) {

    fun decode(output: FloatArray, numCandidates: Int, letterbox: Letterbox): List<DetectedBox> {
        val n = numCandidates
        require(output.size >= (4 + labels.size) * n) { "Ukuran output tidak cocok dengan jumlah label" }

        // Ekspor TFLite ultralytics biasanya memberi koordinat 0..1; sebagian versi memberi piksel.
        val coordScale = if (looksNormalized(output, n)) letterbox.inputSize.toFloat() else 1f

        val candidates = ArrayList<DetectedBox>()
        for (i in 0 until n) {
            var bestClass = -1
            var bestScore = confidenceThreshold
            for (c in labels.indices) {
                val score = output[(4 + c) * n + i]
                if (score >= bestScore) {
                    bestScore = score
                    bestClass = c
                }
            }
            if (bestClass < 0) continue

            val cx = output[i] * coordScale
            val cy = output[n + i] * coordScale
            val w = output[2 * n + i] * coordScale
            val h = output[3 * n + i] * coordScale
            candidates += DetectedBox(
                label = labels[bestClass],
                confidence = bestScore,
                left = letterbox.toSourceX(cx - w / 2f),
                top = letterbox.toSourceY(cy - h / 2f),
                right = letterbox.toSourceX(cx + w / 2f),
                bottom = letterbox.toSourceY(cy + h / 2f),
            )
        }
        return nonMaxSuppression(candidates)
    }

    /** NMS per kelas: dari kotak yang saling tumpang, sisakan yang skornya tertinggi. */
    private fun nonMaxSuppression(boxes: List<DetectedBox>): List<DetectedBox> =
        boxes.groupBy { it.label }.values.flatMap { sameClass ->
            val kept = ArrayList<DetectedBox>()
            for (box in sameClass.sortedByDescending { it.confidence }) {
                if (kept.none { iou(it, box) > iouThreshold }) kept += box
            }
            kept
        }

    private fun looksNormalized(output: FloatArray, n: Int): Boolean {
        var maxCoord = 0f
        for (i in 0 until 4 * n) maxCoord = max(maxCoord, output[i])
        return maxCoord <= 2f
    }

    private fun iou(a: DetectedBox, b: DetectedBox): Float {
        val w = min(a.right, b.right) - max(a.left, b.left)
        val h = min(a.bottom, b.bottom) - max(a.top, b.top)
        if (w <= 0f || h <= 0f) return 0f
        val inter = w * h
        val union = (a.right - a.left) * (a.bottom - a.top) + (b.right - b.left) * (b.bottom - b.top) - inter
        return if (union <= 0f) 0f else inter / union
    }
}
