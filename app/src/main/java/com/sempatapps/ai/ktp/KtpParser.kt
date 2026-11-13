package com.sempatapps.ai.ktp

import com.sempatapps.domain.model.KtpData
import com.sempatapps.domain.model.KtpField
import java.time.LocalDate

/**
 * Satu kolom yang ditemukan model deteksi, beserta teks hasil OCR dari crop-nya.
 *
 * @param label nama kelas dari `ktp_labels.txt` (misal "nik", "name").
 * @param text teks hasil ML Kit untuk crop kotak itu. Baris dipisah '\n'.
 * @param confidence skor deteksi 0..1 dari model.
 */
data class KtpDetection(
    val label: String,
    val text: String,
    val confidence: Float = 1f,
)

/**
 * Memetakan hasil deteksi + OCR ke [KtpData] dan merapikan salah baca yang umum.
 *
 * Model mengenali 17 kolom, tapi yang dipakai hanya [LABEL_DIPAKAI]; sisanya diabaikan.
 */
class KtpParser(
    private val minConfidence: Float = 0.5f,
    private val today: () -> LocalDate = { LocalDate.now() },
) {

    fun parse(detections: List<KtpDetection>): KtpData {
        // Kalau satu kolom terdeteksi lebih dari sekali, pakai yang skornya paling tinggi.
        val best = detections
            .groupBy { it.label.trim().lowercase() }
            .filterKeys { it in LABEL_DIPAKAI }
            .mapValues { (_, list) -> list.maxBy { it.confidence } }

        val ragu = mutableSetOf<KtpField>()
        fun raw(label: String, field: KtpField): String {
            val d = best[label] ?: return ""
            if (d.confidence < minConfidence) ragu += field
            return d.text
        }

        var data = KtpData(
            nik = cleanNik(raw("nik", KtpField.NIK)),
            nama = cleanText(raw("name", KtpField.NAMA)),
            tanggalLahir = birthDate(raw("birth", KtpField.TANGGAL_LAHIR)),
            kabupatenKota = city(raw("province_city", KtpField.KABUPATEN_KOTA)),
        )

        if (data.nik.length != 16) ragu += KtpField.NIK
        data = crossCheckWithNik(data, ragu)

        // Kolom yang tetap kosong selalu perlu dicek.
        mapOf(
            KtpField.NIK to data.nik,
            KtpField.NAMA to data.nama,
            KtpField.TANGGAL_LAHIR to data.tanggalLahir,
            KtpField.KABUPATEN_KOTA to data.kabupatenKota,
        ).filterValues { it.isBlank() }.keys.forEach { ragu += it }

        return data.copy(perluDicek = ragu)
    }

    /**
     * NIK menyimpan tanggal lahir di digit 7–12 (DDMMYY, tanggal +40 untuk perempuan).
     * Isi tanggal lahir yang kosong dari NIK, dan tandai kalau isinya bertentangan.
     */
    private fun crossCheckWithNik(data: KtpData, ragu: MutableSet<KtpField>): KtpData {
        if (data.nik.length != 16) return data
        val dd = data.nik.substring(6, 8).toInt()
        val mm = data.nik.substring(8, 10).toInt()
        val yy = data.nik.substring(10, 12).toInt()
        val day = if (dd > 40) dd - 40 else dd
        val century = if (yy > today().year % 100) 1900 else 2000
        val nikDate = runCatching { LocalDate.of(century + yy, mm, day) }.getOrNull()
        if (nikDate == null) {
            ragu += KtpField.NIK
            return data
        }
        val nikDateText = "%02d-%02d-%04d".format(nikDate.dayOfMonth, nikDate.monthValue, nikDate.year)

        return when {
            data.tanggalLahir.isBlank() -> data.copy(tanggalLahir = nikDateText)
            // Tahun di NIK hanya 2 digit, jadi cukup bandingkan hari, bulan, dan 2 digit terakhir tahun.
            data.tanggalLahir.substring(0, 6) + data.tanggalLahir.takeLast(2) !=
                nikDateText.substring(0, 6) + nikDateText.takeLast(2) -> {
                ragu += setOf(KtpField.TANGGAL_LAHIR, KtpField.NIK)
                data
            }
            else -> data
        }
    }

    companion object {
        /** Kelas model yang perlu di-OCR. Kotak kelas lain dilewati oleh [KtpReader]. */
        val LABEL_DIPAKAI = setOf("nik", "name", "birth", "province_city")

        /** Judul kolom yang kadang ikut terbaca di crop, misal "Nama : BUDI". */
        private val TITLE_PREFIX = Regex("^(NIK|NAMA|TEMPAT\\s*/?\\s*TGL\\.?\\s*LAHIR)\\b")

        /** Huruf yang sering tertukar dengan angka saat OCR. */
        private val DIGIT_FIX = mapOf(
            'O' to '0', 'D' to '0', 'Q' to '0', 'U' to '0',
            'I' to '1', 'L' to '1', '|' to '1', '!' to '1', 'J' to '1',
            'Z' to '2', 'S' to '5', 'B' to '8', 'G' to '6', 'b' to '6', 'T' to '7', '?' to '7',
        )

        private val DATE = Regex("(\\d{1,2})\\s*[-/. ]\\s*(\\d{1,2})\\s*[-/. ]\\s*(\\d{4})")

        internal fun cleanText(raw: String): String =
            raw.uppercase()
                .replace('\n', ' ')
                .replace(Regex("\\s+"), " ")
                .trim()
                .replace(TITLE_PREFIX, "")
                .trimStart(' ', ':', ';', '.', '-', '=')
                .trim()

        private fun fixDigits(s: String): String = s.map { DIGIT_FIX[it] ?: it }.joinToString("")

        internal fun cleanNik(raw: String): String {
            val text = cleanText(raw).replace(" ", "")
            return fixDigits(text).filter { it.isDigit() }
        }

        /** "KERAMAT, 14-04-1978" -> "14-04-1978". Tempat lahir tidak dipakai. */
        internal fun birthDate(raw: String): String {
            val text = cleanText(raw)
            val datePart = text.substringAfterLast(',', text)
            val m = DATE.find(fixDigits(datePart)) ?: return ""
            val (d, mo, y) = m.destructured
            val date = runCatching { LocalDate.of(y.toInt(), mo.toInt(), d.toInt()) }.getOrNull() ?: return ""
            return "%02d-%02d-%04d".format(date.dayOfMonth, date.monthValue, date.year)
        }

        /**
         * "PROVINSI KALIMANTAN TENGAH\nKABUPATEN KATINGAN" -> "KABUPATEN KATINGAN".
         * Juga menangani OCR yang menggabungkan dua baris jadi satu.
         */
        internal fun city(raw: String): String {
            val lines = raw.uppercase().lines().map { it.trim() }.filter { it.isNotEmpty() }
            if (lines.isEmpty()) return ""
            val cityLine = if (lines.size >= 2) {
                lines.drop(1).joinToString(" ")
            } else {
                val m = Regex("\\s(KABUPATEN|KAB\\.|KOTA|JAKARTA)\\s").find(lines[0]) ?: return ""
                lines[0].substring(m.range.first + 1)
            }
            return cityLine.replace(Regex("^KAB\\.\\s*"), "KABUPATEN ").replace(Regex("\\s+"), " ").trim()
        }
    }
}
