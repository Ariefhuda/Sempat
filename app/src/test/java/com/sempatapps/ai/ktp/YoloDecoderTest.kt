package com.sempatapps.ai.ktp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class YoloDecoderTest {

    private val labels = listOf("nik", "name", "birth")
    private val decoder = YoloDecoder(labels = labels, confidenceThreshold = 0.25f, iouThreshold = 0.45f)

    /** Kandidat buatan: kotak (cx, cy, w, h) dalam koordinat input 640 dan skor per kelas. */
    private data class Candidate(val cx: Float, val cy: Float, val w: Float, val h: Float, val scores: List<Float>)

    /** Susun output persis seperti tensor model [1, 4 + kelas, N]: output[c * N + i]. */
    private fun output(vararg candidates: Candidate, normalized: Boolean = true): Pair<FloatArray, Int> {
        val n = candidates.size
        val rows = 4 + labels.size
        val out = FloatArray(rows * n)
        val k = if (normalized) 1f / 640f else 1f
        candidates.forEachIndexed { i, c ->
            out[0 * n + i] = c.cx * k
            out[1 * n + i] = c.cy * k
            out[2 * n + i] = c.w * k
            out[3 * n + i] = c.h * k
            c.scores.forEachIndexed { cls, s -> out[(4 + cls) * n + i] = s }
        }
        return out to n
    }

    private val square = Letterbox(sourceWidth = 640, sourceHeight = 640)

    @Test
    fun `kandidat di atas ambang jadi kotak berlabel`() {
        val (out, n) = output(Candidate(320f, 100f, 200f, 40f, listOf(0.9f, 0.1f, 0f)))

        val boxes = decoder.decode(out, n, square)

        assertEquals(1, boxes.size)
        val b = boxes.single()
        assertEquals("nik", b.label)
        assertEquals(0.9f, b.confidence, 1e-6f)
        assertEquals(220f, b.left, 1e-3f)
        assertEquals(80f, b.top, 1e-3f)
        assertEquals(420f, b.right, 1e-3f)
        assertEquals(120f, b.bottom, 1e-3f)
    }

    @Test
    fun `kandidat di bawah ambang dibuang`() {
        val (out, n) = output(Candidate(320f, 100f, 200f, 40f, listOf(0.2f, 0.1f, 0.05f)))

        assertTrue(decoder.decode(out, n, square).isEmpty())
    }

    @Test
    fun `label diambil dari kelas dengan skor tertinggi`() {
        val (out, n) = output(Candidate(320f, 100f, 200f, 40f, listOf(0.3f, 0.8f, 0.5f)))

        assertEquals("name", decoder.decode(out, n, square).single().label)
    }

    @Test
    fun `koordinat dikembalikan ke ukuran foto asli melewati letterbox`() {
        // Foto 1280x800 -> skala 0.5 jadi 640x400, diberi bantalan 120 di atas dan bawah.
        val landscape = Letterbox(sourceWidth = 1280, sourceHeight = 800)
        val (out, n) = output(Candidate(320f, 320f, 100f, 50f, listOf(0.9f, 0f, 0f)))

        val b = decoder.decode(out, n, landscape).single()

        assertEquals(540f, b.left, 1e-3f)
        assertEquals(350f, b.top, 1e-3f)
        assertEquals(740f, b.right, 1e-3f)
        assertEquals(450f, b.bottom, 1e-3f)
    }

    @Test
    fun `output dalam satuan piksel juga terbaca`() {
        val (out, n) = output(Candidate(320f, 100f, 200f, 40f, listOf(0.9f, 0f, 0f)), normalized = false)

        val b = decoder.decode(out, n, square).single()

        assertEquals(220f, b.left, 1e-3f)
        assertEquals(420f, b.right, 1e-3f)
    }

    @Test
    fun `kotak dobel di kelas yang sama disisakan yang skornya tertinggi`() {
        val (out, n) = output(
            Candidate(320f, 100f, 200f, 40f, listOf(0.7f, 0f, 0f)),
            Candidate(325f, 102f, 200f, 40f, listOf(0.95f, 0f, 0f)),
        )

        val b = decoder.decode(out, n, square).single()

        assertEquals(0.95f, b.confidence, 1e-6f)
    }

    @Test
    fun `kotak bertumpuk dari kelas berbeda dua-duanya dipertahankan`() {
        val (out, n) = output(
            Candidate(320f, 100f, 200f, 40f, listOf(0.9f, 0f, 0f)),
            Candidate(320f, 100f, 200f, 40f, listOf(0f, 0.8f, 0f)),
        )

        assertEquals(setOf("nik", "name"), decoder.decode(out, n, square).map { it.label }.toSet())
    }

    @Test
    fun `kotak yang keluar dari foto dipotong ke tepi foto`() {
        val (out, n) = output(Candidate(20f, 630f, 100f, 40f, listOf(0.9f, 0f, 0f)))

        val b = decoder.decode(out, n, square).single()

        assertEquals(0f, b.left, 1e-3f)
        assertEquals(640f, b.bottom, 1e-3f)
    }

    @Test
    fun `letterbox menghitung skala dan bantalan untuk foto potret`() {
        // 600x1200 -> skala 0.5333 jadi 320x640, bantalan kiri-kanan 160.
        val lb = Letterbox(sourceWidth = 600, sourceHeight = 1200)

        assertEquals(640f / 1200f, lb.scale, 1e-6f)
        assertEquals(160f, lb.padX, 1e-3f)
        assertEquals(0f, lb.padY, 1e-3f)
    }
}
