package com.sempatapps.server.donasi

import com.sempatapps.server.auth.ApiException
import com.sempatapps.server.auth.LokasiDto
import com.sempatapps.server.auth.Pengguna
import com.sempatapps.server.db.Donasi
import com.sempatapps.server.db.Pengajuan
import com.sempatapps.server.db.StatusDonasi
import com.sempatapps.server.db.StatusPengajuan
import com.sempatapps.server.db.Users
import com.sempatapps.server.notifikasi.tambahNotifikasi
import com.sempatapps.server.profil.ProfilService
import com.sempatapps.server.profil.validasiLokasi
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.Clock
import java.time.Instant
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException
import java.util.UUID
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Alur satu donasi:
 *
 *   donatur buat donasi ............................ donasi TERSEDIA
 *   penerima ajukan ................................ pengajuan MENUNGGU
 *   donatur setujui satu pengajuan ................. pengajuan DISETUJUI, donasi DIPROSES,
 *                                                    pengajuan lain yang menunggu jadi DITOLAK
 *   penerima konfirmasi sudah terima ............... pengajuan DITERIMA, donasi SELESAI
 *
 * Batal: donatur bisa batalkan donasi selama belum SELESAI; penerima bisa batalkan pengajuannya
 * selama MENUNGGU/DISETUJUI (kalau sudah disetujui, donasi kembali TERSEDIA).
 */
class DonasiService(private val db: Database, private val clock: Clock = Clock.systemUTC()) {

    // ---------- Donasi ----------

    fun buat(p: Pengguna, req: BuatDonasiRequest): DonasiDto {
        p.wajibDonatur()
        val nama = req.nama.trim().ifEmpty { throw ApiException(400, "Nama pangan wajib diisi") }
        val kategori = req.kategori.trim().ifEmpty { throw ApiException(400, "Kategori wajib diisi") }
        val jumlah = req.jumlah.trim().ifEmpty { throw ApiException(400, "Jumlah wajib diisi") }
        if (req.beratGram !in 1..1_000_000) throw ApiException(400, "Berat tidak valid")
        val sekarang = clock.instant()
        val batasAmbil = parseWaktu(req.batasAmbil)
        if (batasAmbil <= sekarang) throw ApiException(400, "Batas waktu ambil harus di masa depan")
        req.lokasi?.let(::validasiLokasi)

        val id = UUID.randomUUID()
        transaction(db) {
            val lokasi: LokasiDto = req.lokasi ?: ProfilService.lokasi(p.id)
                ?: throw ApiException(400, "Isi lokasi pengambilan dulu")
            Donasi.insert {
                it[Donasi.id] = id
                it[donaturId] = p.id
                it[Donasi.nama] = nama.take(100)
                it[Donasi.kategori] = kategori.take(50)
                it[Donasi.jumlah] = jumlah.take(50)
                it[beratGram] = req.beratGram
                it[catatan] = req.catatan.trim().take(500)
                it[fotoUrl] = req.fotoUrl?.take(500)
                it[alamatAmbil] = lokasi.alamat.trim().take(255)
                it[latitude] = lokasi.latitude
                it[longitude] = lokasi.longitude
                it[Donasi.batasAmbil] = batasAmbil
                it[status] = StatusDonasi.TERSEDIA
                it[dibuatPada] = sekarang
                it[diperbaruiPada] = sekarang
            }
        }
        return detail(id)
    }

    fun detail(id: UUID): DonasiDto = transaction(db) {
        donasiDenganDonatur().where { Donasi.id eq id }.singleOrNull()?.let(::keDonasiDto)
            ?: throw ApiException(404, "Donasi tidak ditemukan")
    }

    /** Daftar pangan terdekat untuk penerima: yang masih TERSEDIA dan belum lewat batas ambil. */
    fun terdekat(p: Pengguna, lat: Double?, lng: Double?, radiusKm: Double): List<DonasiDto> = transaction(db) {
        val (asalLat, asalLng) = if (lat != null && lng != null) {
            lat to lng
        } else {
            val lokasi = ProfilService.lokasi(p.id) ?: throw ApiException(400, "Isi lokasimu dulu")
            lokasi.latitude to lokasi.longitude
        }
        donasiDenganDonatur()
            .where {
                (Donasi.status eq StatusDonasi.TERSEDIA) and
                    (Donasi.batasAmbil greater clock.instant()) and
                    (Donasi.donaturId neq p.id)
            }
            .map { keDonasiDto(it).copy(jarakKm = jarakKm(asalLat, asalLng, it[Donasi.latitude], it[Donasi.longitude])) }
            .filter { it.jarakKm!! <= radiusKm }
            .sortedBy { it.jarakKm }
            .take(50)
    }

    fun milikSaya(p: Pengguna, status: String?): List<DonasiDto> {
        p.wajibDonatur()
        return transaction(db) {
            donasiDenganDonatur()
                .where { if (status == null) Donasi.donaturId eq p.id else (Donasi.donaturId eq p.id) and (Donasi.status eq status) }
                .orderBy(Donasi.dibuatPada, SortOrder.DESC)
                .map(::keDonasiDto)
        }
    }

    fun ringkasan(p: Pengguna): RingkasanDonaturDto {
        p.wajibDonatur()
        return transaction(db) {
            val semua = Donasi.selectAll().where { Donasi.donaturId eq p.id }.toList()
            val selesai = semua.filter { it[Donasi.status] == StatusDonasi.SELESAI }
            RingkasanDonaturDto(
                totalBeratGram = selesai.sumOf { it[Donasi.beratGram].toLong() },
                jumlahSelesai = selesai.size.toLong(),
                jumlahAktif = semua.count { it[Donasi.status] in listOf(StatusDonasi.TERSEDIA, StatusDonasi.DIPROSES) }.toLong(),
            )
        }
    }

    fun batalkanDonasi(p: Pengguna, id: UUID): DonasiDto {
        p.wajibDonatur()
        transaction(db) {
            val d = kunciDonasi(id)
            if (d[Donasi.donaturId] != p.id) throw ApiException(403, "Ini bukan donasimu")
            if (d[Donasi.status] !in listOf(StatusDonasi.TERSEDIA, StatusDonasi.DIPROSES)) {
                throw ApiException(409, "Donasi ini sudah tidak bisa dibatalkan")
            }
            val sekarang = clock.instant()
            ubahStatusDonasi(id, StatusDonasi.DIBATALKAN)
            pengajuanAktif(id).forEach {
                ubahStatusPengajuan(it[Pengajuan.id], StatusPengajuan.DIBATALKAN)
                tambahNotifikasi(
                    it[Pengajuan.penerimaId], "Donasi dibatalkan",
                    "${d[Donasi.nama]} dibatalkan oleh donatur.", sekarang,
                )
            }
        }
        return detail(id)
    }

    // ---------- Pengajuan ----------

    fun ajukan(p: Pengguna, donasiId: UUID, req: AjukanRequest): PengajuanDto {
        p.wajibPenerima()
        val id = UUID.randomUUID()
        transaction(db) {
            val d = kunciDonasi(donasiId)
            val sekarang = clock.instant()
            if (d[Donasi.status] != StatusDonasi.TERSEDIA || d[Donasi.batasAmbil] <= sekarang) {
                throw ApiException(409, "Donasi ini sudah tidak tersedia")
            }
            val sudahAda = Pengajuan.selectAll().where {
                (Pengajuan.donasiId eq donasiId) and (Pengajuan.penerimaId eq p.id) and
                    (Pengajuan.status inList StatusPengajuan.AKTIF)
            }.any()
            if (sudahAda) throw ApiException(409, "Kamu sudah mengajukan donasi ini")

            Pengajuan.insert {
                it[Pengajuan.id] = id
                it[Pengajuan.donasiId] = donasiId
                it[penerimaId] = p.id
                it[catatan] = req.catatan.trim().take(500)
                it[buktiUrl] = req.buktiUrl?.take(500)
                it[status] = StatusPengajuan.MENUNGGU
                it[dibuatPada] = sekarang
                it[diperbaruiPada] = sekarang
            }
            tambahNotifikasi(
                d[Donasi.donaturId], "Ada pengajuan baru",
                "Seseorang mengajukan ${d[Donasi.nama]}.", sekarang,
            )
        }
        return pengajuanDto(p, id)
    }

    /** Untuk donatur: siapa saja yang mengajukan donasinya. */
    fun pengajuanUntukDonasi(p: Pengguna, donasiId: UUID): List<PengajuanDto> {
        p.wajibDonatur()
        return transaction(db) {
            val d = Donasi.selectAll().where { Donasi.id eq donasiId }.singleOrNull()
                ?: throw ApiException(404, "Donasi tidak ditemukan")
            if (d[Donasi.donaturId] != p.id) throw ApiException(403, "Ini bukan donasimu")
            pengajuanDenganPenerima()
                .where { Pengajuan.donasiId eq donasiId }
                .orderBy(Pengajuan.dibuatPada, SortOrder.ASC)
                .map { kePengajuanDto(it, kontak = it[Users.noHp]) }
        }
    }

    /** Untuk penerima: semua pengajuannya, lengkap dengan data donasinya. */
    fun pengajuanSaya(p: Pengguna): List<PengajuanDto> {
        p.wajibPenerima()
        return transaction(db) {
            val baris = pengajuanDenganPenerima()
                .where { Pengajuan.penerimaId eq p.id }
                .orderBy(Pengajuan.dibuatPada, SortOrder.DESC)
                .toList()
            val donasi = donasiDenganDonatur()
                .where { Donasi.id inList baris.map { it[Pengajuan.donasiId] }.distinct() }
                .associate { it[Donasi.id] to (keDonasiDto(it) to it[Users.noHp]) }
            baris.map {
                val (dto, noHpDonatur) = donasi.getValue(it[Pengajuan.donasiId])
                kePengajuanDto(it, kontak = noHpDonatur).copy(donasi = dto)
            }
        }
    }

    fun setujui(p: Pengguna, pengajuanId: UUID): PengajuanDto {
        p.wajibDonatur()
        transaction(db) {
            val (aju, d) = kunciPengajuanDanDonasi(pengajuanId)
            if (d[Donasi.donaturId] != p.id) throw ApiException(403, "Ini bukan donasimu")
            if (aju[Pengajuan.status] != StatusPengajuan.MENUNGGU) throw ApiException(409, "Pengajuan ini sudah diproses")
            if (d[Donasi.status] != StatusDonasi.TERSEDIA) throw ApiException(409, "Donasi ini sudah tidak tersedia")

            val sekarang = clock.instant()
            ubahStatusPengajuan(pengajuanId, StatusPengajuan.DISETUJUI)
            ubahStatusDonasi(d[Donasi.id], StatusDonasi.DIPROSES)
            tambahNotifikasi(
                aju[Pengajuan.penerimaId], "Pengajuan disetujui",
                "${d[Donasi.nama]} siap diambil di ${d[Donasi.alamatAmbil]}.", sekarang,
            )
            // Satu donasi hanya untuk satu penerima: yang lain otomatis ditolak.
            pengajuanAktif(d[Donasi.id]).filter { it[Pengajuan.id] != pengajuanId }.forEach {
                ubahStatusPengajuan(it[Pengajuan.id], StatusPengajuan.DITOLAK)
                tambahNotifikasi(
                    it[Pengajuan.penerimaId], "Pengajuan belum berhasil",
                    "${d[Donasi.nama]} sudah diberikan ke penerima lain.", sekarang,
                )
            }
        }
        return pengajuanDto(p, pengajuanId)
    }

    fun tolak(p: Pengguna, pengajuanId: UUID): PengajuanDto {
        p.wajibDonatur()
        transaction(db) {
            val (aju, d) = kunciPengajuanDanDonasi(pengajuanId)
            if (d[Donasi.donaturId] != p.id) throw ApiException(403, "Ini bukan donasimu")
            if (aju[Pengajuan.status] != StatusPengajuan.MENUNGGU) throw ApiException(409, "Pengajuan ini sudah diproses")
            ubahStatusPengajuan(pengajuanId, StatusPengajuan.DITOLAK)
            tambahNotifikasi(
                aju[Pengajuan.penerimaId], "Pengajuan belum berhasil",
                "Pengajuan ${d[Donasi.nama]} ditolak donatur.", clock.instant(),
            )
        }
        return pengajuanDto(p, pengajuanId)
    }

    /** Penerima konfirmasi pangan sudah diterima: menutup donasi di sisi donatur. */
    fun konfirmasiDiterima(p: Pengguna, pengajuanId: UUID): PengajuanDto {
        p.wajibPenerima()
        transaction(db) {
            val (aju, d) = kunciPengajuanDanDonasi(pengajuanId)
            if (aju[Pengajuan.penerimaId] != p.id) throw ApiException(403, "Ini bukan pengajuanmu")
            if (aju[Pengajuan.status] != StatusPengajuan.DISETUJUI) {
                throw ApiException(409, "Pengajuan belum disetujui donatur")
            }
            ubahStatusPengajuan(pengajuanId, StatusPengajuan.DITERIMA)
            ubahStatusDonasi(d[Donasi.id], StatusDonasi.SELESAI)
            tambahNotifikasi(
                d[Donasi.donaturId], "Pangan sudah diterima",
                "${d[Donasi.nama]} telah diterima oleh penerima.", clock.instant(),
            )
        }
        return pengajuanDto(p, pengajuanId)
    }

    fun batalkanPengajuan(p: Pengguna, pengajuanId: UUID): PengajuanDto {
        p.wajibPenerima()
        transaction(db) {
            val (aju, d) = kunciPengajuanDanDonasi(pengajuanId)
            if (aju[Pengajuan.penerimaId] != p.id) throw ApiException(403, "Ini bukan pengajuanmu")
            val status = aju[Pengajuan.status]
            if (status !in StatusPengajuan.AKTIF) throw ApiException(409, "Pengajuan ini sudah tidak bisa dibatalkan")

            ubahStatusPengajuan(pengajuanId, StatusPengajuan.DIBATALKAN)
            if (status == StatusPengajuan.DISETUJUI && d[Donasi.status] == StatusDonasi.DIPROSES) {
                ubahStatusDonasi(d[Donasi.id], StatusDonasi.TERSEDIA)
                tambahNotifikasi(
                    d[Donasi.donaturId], "Pengajuan dibatalkan",
                    "Penerima membatalkan ${d[Donasi.nama]}. Donasi tersedia lagi.", clock.instant(),
                )
            }
        }
        return pengajuanDto(p, pengajuanId)
    }

    // ---------- Bantuan internal (dipanggil di dalam transaction) ----------

    private fun donasiDenganDonatur() =
        Donasi.join(Users, org.jetbrains.exposed.sql.JoinType.INNER, Donasi.donaturId, Users.id).selectAll()

    private fun pengajuanDenganPenerima() =
        Pengajuan.join(Users, org.jetbrains.exposed.sql.JoinType.INNER, Pengajuan.penerimaId, Users.id).selectAll()

    /** SELECT ... FOR UPDATE: dua donatur/penerima yang menekan tombol bersamaan tidak saling tabrak. */
    private fun kunciDonasi(id: UUID): ResultRow =
        Donasi.selectAll().where { Donasi.id eq id }.forUpdate().singleOrNull()
            ?: throw ApiException(404, "Donasi tidak ditemukan")

    private fun kunciPengajuanDanDonasi(pengajuanId: UUID): Pair<ResultRow, ResultRow> {
        val aju = Pengajuan.selectAll().where { Pengajuan.id eq pengajuanId }.singleOrNull()
            ?: throw ApiException(404, "Pengajuan tidak ditemukan")
        // Kunci donasinya dulu, baru baca ulang pengajuan supaya statusnya pasti terbaru.
        val d = kunciDonasi(aju[Pengajuan.donasiId])
        val terbaru = Pengajuan.selectAll().where { Pengajuan.id eq pengajuanId }.forUpdate().single()
        return terbaru to d
    }

    private fun pengajuanAktif(donasiId: UUID) = Pengajuan.selectAll().where {
        (Pengajuan.donasiId eq donasiId) and (Pengajuan.status inList StatusPengajuan.AKTIF)
    }.toList()

    private fun ubahStatusDonasi(id: UUID, status: String) {
        Donasi.update({ Donasi.id eq id }) {
            it[Donasi.status] = status
            it[diperbaruiPada] = clock.instant()
        }
    }

    private fun ubahStatusPengajuan(id: UUID, status: String) {
        Pengajuan.update({ Pengajuan.id eq id }) {
            it[Pengajuan.status] = status
            it[diperbaruiPada] = clock.instant()
        }
    }

    /** Pengajuan dilihat dari sisi [p]: kontak = nomor pihak lain, hanya kalau sudah disetujui. */
    private fun pengajuanDto(p: Pengguna, id: UUID): PengajuanDto = transaction(db) {
        val aju = pengajuanDenganPenerima().where { Pengajuan.id eq id }.single()
        val kontak = if (p.donatur) {
            aju[Users.noHp]
        } else {
            donasiDenganDonatur().where { Donasi.id eq aju[Pengajuan.donasiId] }.single()[Users.noHp]
        }
        kePengajuanDto(aju, kontak)
    }

    private fun kePengajuanDto(r: ResultRow, kontak: String?): PengajuanDto {
        val status = r[Pengajuan.status]
        return PengajuanDto(
            id = r[Pengajuan.id].toString(),
            donasiId = r[Pengajuan.donasiId].toString(),
            penerimaId = r[Pengajuan.penerimaId].toString(),
            namaPenerima = r[Users.nama],
            catatan = r[Pengajuan.catatan],
            buktiUrl = r[Pengajuan.buktiUrl],
            status = status,
            noHpKontak = kontak.takeIf { status in listOf(StatusPengajuan.DISETUJUI, StatusPengajuan.DITERIMA) },
            dibuatPada = r[Pengajuan.dibuatPada].toString(),
            diperbaruiPada = r[Pengajuan.diperbaruiPada].toString(),
        )
    }

    private fun keDonasiDto(r: ResultRow) = DonasiDto(
        id = r[Donasi.id].toString(),
        donaturId = r[Donasi.donaturId].toString(),
        namaDonatur = r[Users.nama],
        nama = r[Donasi.nama],
        kategori = r[Donasi.kategori],
        jumlah = r[Donasi.jumlah],
        beratGram = r[Donasi.beratGram],
        catatan = r[Donasi.catatan],
        fotoUrl = r[Donasi.fotoUrl],
        alamatAmbil = r[Donasi.alamatAmbil],
        latitude = r[Donasi.latitude],
        longitude = r[Donasi.longitude],
        batasAmbil = r[Donasi.batasAmbil].toString(),
        status = r[Donasi.status],
        dibuatPada = r[Donasi.dibuatPada].toString(),
    )

    companion object {
        val STATUS_DONASI = setOf(StatusDonasi.TERSEDIA, StatusDonasi.DIPROSES, StatusDonasi.SELESAI, StatusDonasi.DIBATALKAN)

        fun parseWaktu(input: String): Instant = try {
            OffsetDateTime.parse(input).toInstant()
        } catch (_: DateTimeParseException) {
            throw ApiException(400, "Format waktu harus ISO-8601, mis. 2026-11-22T17:00:00+07:00")
        }

        /** Jarak garis lurus (haversine), dibulatkan 1 angka di belakang koma. */
        fun jarakKm(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
            val dLat = Math.toRadians(lat2 - lat1)
            val dLng = Math.toRadians(lng2 - lng1)
            val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLng / 2).pow(2)
            return round(2 * 6371.0 * asin(sqrt(a)) * 10) / 10
        }
    }
}
