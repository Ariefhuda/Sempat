package com.sempatapps.ai.ktp

import org.junit.Assert.assertEquals
import org.junit.Test

class OcrSizeTest {

    @Test
    fun `crop yang cukup besar tidak diubah`() {
        assertEquals(OcrSize(350, 43), ocrSizeFor(350, 43, minSide = 32))
    }

    @Test
    fun `crop kecil diperbesar sampai sisi pendeknya memenuhi batas dengan rasio tetap`() {
        // Kotak golongan darah 27x30: sisi pendek 27 -> 64, faktor 64/27.
        assertEquals(OcrSize(64, 71), ocrSizeFor(27, 30, minSide = 64))
    }

    @Test
    fun `crop pendek dan lebar diperbesar berdasarkan tingginya`() {
        // Kotak kewarganegaraan 54x25: tinggi 25 -> 64.
        assertEquals(OcrSize(138, 64), ocrSizeFor(54, 25, minSide = 64))
    }
}
