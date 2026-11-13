package com.sempatapps.ai.ktp

import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

data class OcrSize(val width: Int, val height: Int)

/**
 * Ukuran crop untuk OCR. ML Kit menolak gambar di bawah 32 piksel dan kurang akurat
 * kalau hurufnya kecil, jadi crop yang sisi pendeknya < [minSide] diperbesar (rasio tetap).
 */
fun ocrSizeFor(width: Int, height: Int, minSide: Int): OcrSize {
    val shortSide = min(width, height)
    if (shortSide >= minSide) return OcrSize(width, height)
    val factor = minSide.toFloat() / shortSide
    return OcrSize(
        width = max(minSide, (width * factor).roundToInt()),
        height = max(minSide, (height * factor).roundToInt()),
    )
}
