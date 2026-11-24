package com.sempatapps.data.repository

import com.sempatapps.data.local.SessionStore
import com.sempatapps.data.remote.BuatDonasiRequest
import com.sempatapps.data.remote.DonasiDto
import com.sempatapps.data.remote.NotifikasiDto
import com.sempatapps.data.remote.SempatApi
import com.sempatapps.domain.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

interface HomeRepository {
    fun observe(role: HomeRole): StateFlow<HomeData>
    suspend fun refresh(role: HomeRole) {}
    fun selectLocation(id: String)
    fun markNoticeRead(role: HomeRole, id: String)
    fun setNeedsHelp(enabled: Boolean)
    fun addDonation(donation: Donation, recipientName: String? = null)
    fun markDonationAssigned(donation: Donation, recipientName: String) {}
}

@Singleton
class ApiHomeRepository @Inject constructor(
    private val api: SempatApi,
    private val sessions: SessionStore,
) : HomeRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val fallback = DummyHomeRepository()
    private val localPhotoByKey = mutableMapOf<String, String>()

    override fun observe(role: HomeRole): StateFlow<HomeData> = fallback.observe(role)

    override suspend fun refresh(role: HomeRole) {
        val sesi = sessions.ambil() ?: return
        val token = sesi.token
        val displayName = sesi.nama.ifBlank { "Pengguna" }

        runCatching {
            val profil = runCatching { api.profilSaya(token) }.getOrNull()
            val nama = profil?.nama?.ifBlank { displayName } ?: displayName
            val alamatProfil = profil?.lokasi?.alamat?.takeIf { it.isNotBlank() }.orEmpty()
            val notices = runCatching { api.notifikasiSaya(token) }.getOrNull()?.map { it.toHomeNotice() }

            if (role == HomeRole.DONOR) {
                val donasiList = api.donasiSaya(token)
                val ringkasan = runCatching { api.ringkasanDonatur(token) }.getOrNull()
                val totalAllGram = donasiList.sumOf { it.beratGram.toLong() }
                val totalSelesaiGram = ringkasan?.totalBeratGram ?: 0L
                fallback.replaceDonor(
                    displayName = nama,
                    donatedWeight = formatWeightKg(totalAllGram),
                    sharedCount = formatWeightKg(totalSelesaiGram),
                    pickupAddress = alamatProfil,
                    donations = donasiList.map { it.toDonation() },
                    notices = notices,
                )
            } else {
                val donasiList = api.donasiTerdekat(token)
                val lokasi = profil?.lokasi?.let {
                    listOf(PickupLocation("loc_profil", "Lokasi Saya", it.alamat))
                }
                fallback.replaceRecipient(
                    displayName = nama,
                    locations = lokasi,
                    donations = donasiList.map { it.toDonation() },
                    notices = notices,
                )
            }
        }
    }

    override fun selectLocation(id: String) {
        fallback.selectLocation(id)
    }

    override fun markNoticeRead(role: HomeRole, id: String) {
        fallback.markNoticeRead(role, id)
        scope.launch {
            val token = sessions.ambil()?.token ?: return@launch
            runCatching { api.tandaiNotifikasiDibaca(token, id) }
        }
    }

    override fun setNeedsHelp(enabled: Boolean) {
        fallback.setNeedsHelp(enabled)
    }

    override fun addDonation(donation: Donation, recipientName: String?) {
        val photoUri = donation.imageUri
        if (!photoUri.isNullOrBlank()) {
            localPhotoByKey[photoKey(donation.name, donation.quantity)] = photoUri
        }
        fallback.addDonation(donation, recipientName)
        scope.launch {
            val token = sessions.ambil()?.token ?: return@launch
            val beratGram = parseBeratGram(donation.quantity)
            val batasAmbil = Instant.now().plus(2, ChronoUnit.DAYS).toString()
            val catatan = recipientName?.let { "Ditujukan untuk $it" }.orEmpty()
            val created = runCatching {
                api.buatDonasi(
                    token = token,
                    request = BuatDonasiRequest(
                        nama = donation.name,
                        kategori = donation.category,
                        jumlah = donation.quantity,
                        beratGram = beratGram,
                        batasAmbil = batasAmbil,
                        catatan = catatan,
                    ),
                )
            }.getOrNull()
            if (created != null) {
                if (!photoUri.isNullOrBlank()) {
                    localPhotoByKey[created.id] = photoUri
                }
                refresh(HomeRole.DONOR)
            }
        }
    }

    override fun markDonationAssigned(donation: Donation, recipientName: String) {
        fallback.markDonationAssigned(donation, recipientName)
    }

    private fun photoKey(name: String, quantity: String): String =
        "${name.trim().lowercase()}|${quantity.trim().lowercase()}"

    private fun DonasiDto.toDonation(): Donation {
        val statusLabel = when (status.uppercase()) {
            "TERSEDIA" -> "Aktif"
            "DIPROSES" -> "Didonasikan"
            "SELESAI" -> "Selesai"
            "DIBATALKAN" -> "Dibatalkan"
            else -> status
        }
        val imageName = when {
            kategori.contains("sayur", ignoreCase = true) || kategori.contains("buah", ignoreCase = true) -> "veggie_photo"
            else -> "bread_photo"
        }
        val resolvedUri = fotoUrl?.takeIf { it.isNotBlank() }
            ?: localPhotoByKey[id]
            ?: localPhotoByKey[photoKey(nama, jumlah)]
        val jarak = jarakKm?.let { String.format(Locale.forLanguageTag("id-ID"), "%.1f km", it) } ?: "1,5 km"
        return Donation(
            id = id,
            name = nama,
            category = kategori,
            quantity = jumlah,
            pickupAddress = alamatAmbil,
            distance = jarak,
            imageName = imageName,
            status = statusLabel,
            imageUri = resolvedUri,
        )
    }

    private fun NotifikasiDto.toHomeNotice(): HomeNotice {
        val today = LocalDate.now()
        val parsedDate = runCatching {
            Instant.parse(dibuatPada).atZone(ZoneId.systemDefault()).toLocalDate()
        }.getOrNull()
        val dayLabel = when (parsedDate) {
            today -> "Hari ini"
            today.minusDays(1) -> "Kemarin"
            else -> "Sebelumnya"
        }
        val iconName = if (judul.contains("diambil", ignoreCase = true)) "picked_up" else "donated"
        return HomeNotice(
            id = id,
            title = judul,
            message = pesan,
            day = dayLabel,
            isRead = dibaca,
            iconName = iconName,
        )
    }

    private fun formatWeightKg(gram: Long): String {
        if (gram <= 0L) return "0 kg"
        val kg = gram / 1000.0
        return if (gram % 1000L == 0L) {
            "${gram / 1000L} kg"
        } else if (gram < 1000L) {
            "${gram} g"
        } else {
            String.format(Locale.forLanguageTag("id-ID"), "%.1f kg", kg)
        }
    }

    private fun parseBeratGram(quantity: String): Int {
        val cleaned = quantity.trim().lowercase()
        val number = Regex("""\d+(?:[.,]\d+)?""").find(cleaned)?.value?.replace(',', '.')?.toDoubleOrNull() ?: 250.0
        val gram = if (cleaned.contains("kg")) (number * 1000).toInt() else number.toInt()
        return gram.coerceAtLeast(1)
    }
}

class DummyHomeRepository : HomeRepository {
    private val donor = MutableStateFlow(
        HomeData(
            displayName = "Pengguna",
            role = HomeRole.DONOR,
            donatedWeight = "24 kg",
            sharedCount = "8 kg",
            donations = listOf(
                Donation("d1", "Roti Enak", "Roti & Kue", "250g", "Zahran Bakery, Medan", "1,5 km", "bread_photo", "Aktif"),
                Donation("d2", "Sayur Bu Fitri", "Sayur & Buah", "5kg", "Pasar Baru, Medan", "1,2 km", "veggie_photo", "Selesai"),
                Donation("d3", "Roti Reagan", "Roti & Kue", "250g", "Zahran Bakery, Medan", "1,8 km", "bread_photo_alt", "Didonasikan")
            ),
            notices = listOf(
                HomeNotice(
                    id = "n1",
                    title = "Panganmu Sudah Diambil",
                    message = "Tomat Segar 2 kg telah diambil oleh penerima",
                    day = "Hari ini",
                    highlightText = "Tomat Segar 2 kg",
                    iconName = "picked_up"
                ),
                HomeNotice(
                    id = "n2",
                    title = "Pangan Berhasil Didonasikan",
                    message = "Roti Reagan 5 kg berhasil didonasikan ke penerima",
                    day = "Hari ini",
                    highlightText = "Roti Reagan 5 kg",
                    iconName = "donated"
                )
            )
        )
    )

    private val recipient = MutableStateFlow(
        HomeData(
            displayName = "Pengguna",
            role = HomeRole.RECIPIENT,
            selectedLocationId = "loc1",
            locations = listOf(
                PickupLocation("loc1", "Lokasi Saya", "Jalan Setia Budi, Medan, Sumatera Utara"),
                PickupLocation("loc2", "Kampus", "Jalan Dr. T. Mansur, Medan Baru, Medan")
            ),
            donations = listOf(
                Donation("d1", "Roti Enak", "Roti & Kue", "250 g", "Zahran Bakery, Medan", "1,5 km", "bread_photo", "Tersedia"),
                Donation("d2", "Roti Hangat", "Roti & Kue", "250 g", "Zahran Bakery, Medan", "1,2 km", "bread_photo_alt", "Tersedia")
            ),
            notices = listOf(
                HomeNotice("n3", "Pangan siap diambil", "Roti Enak tersedia untuk diambil di Zahran Bakery.", "Hari ini"),
                HomeNotice("n4", "Bantuan pangan tersedia", "Ada donasi pangan di dekat lokasi pilihanmu.", "Kemarin")
            )
        )
    )

    internal fun replaceDonor(
        displayName: String,
        donatedWeight: String,
        sharedCount: String,
        pickupAddress: String,
        donations: List<Donation>,
        notices: List<HomeNotice>?,
    ) {
        donor.update { current ->
            current.copy(
                displayName = displayName,
                donatedWeight = donatedWeight,
                sharedCount = sharedCount,
                pickupAddress = pickupAddress.ifBlank { current.pickupAddress },
                donations = donations,
                notices = notices ?: current.notices,
            )
        }
    }

    internal fun replaceRecipient(
        displayName: String,
        locations: List<PickupLocation>?,
        donations: List<Donation>,
        notices: List<HomeNotice>?,
    ) {
        recipient.update { current ->
            val updatedLocations = if (!locations.isNullOrEmpty()) locations else current.locations
            current.copy(
                displayName = displayName,
                selectedLocationId = updatedLocations.firstOrNull()?.id ?: current.selectedLocationId,
                locations = updatedLocations,
                donations = donations,
                notices = notices ?: current.notices,
            )
        }
    }

    override fun observe(role: HomeRole): StateFlow<HomeData> =
        if (role == HomeRole.DONOR) donor.asStateFlow() else recipient.asStateFlow()

    override fun selectLocation(id: String) {
        recipient.update { data ->
            if (data.locations.any { it.id == id }) data.copy(selectedLocationId = id) else data
        }
    }

    override fun markNoticeRead(role: HomeRole, id: String) {
        val source = if (role == HomeRole.DONOR) donor else recipient
        source.update { data ->
            data.copy(notices = data.notices.map { notice ->
                if (notice.id == id) notice.copy(isRead = true) else notice
            })
        }
    }

    override fun setNeedsHelp(enabled: Boolean) {
        recipient.update { it.copy(needsHelp = enabled) }
    }

    override fun addDonation(donation: Donation, recipientName: String?) {
        donor.update { data ->
            val updatedList = listOf(donation) + data.donations.filterNot { it.id == donation.id }
            val highlight = "${donation.name} ${donation.quantity}"
            val targetText = recipientName?.let { "ke $it" } ?: "ke penerima"
            val newNotice = HomeNotice(
                id = "n_${donation.id}",
                title = "Pangan Berhasil Didonasikan",
                message = "$highlight berhasil didonasikan $targetText",
                day = "Hari ini",
                highlightText = highlight,
                iconName = "donated"
            )
            data.copy(
                donations = updatedList,
                notices = listOf(newNotice) + data.notices.filterNot { it.id == newNotice.id }
            )
        }
    }

    override fun markDonationAssigned(donation: Donation, recipientName: String) {
        donor.update { data ->
            val updatedList = data.donations.map { item ->
                val isSame = item.id == donation.id ||
                    (item.name.equals(donation.name, ignoreCase = true) && item.quantity.equals(donation.quantity, ignoreCase = true))
                if (isSame) item.copy(status = "Didonasikan") else item
            }
            val highlight = "${donation.name} ${donation.quantity}"
            val newNotice = HomeNotice(
                id = "n_assign_${donation.id}",
                title = "Pangan Berhasil Didonasikan",
                message = "$highlight berhasil didonasikan ke $recipientName",
                day = "Hari ini",
                highlightText = highlight,
                iconName = "donated"
            )
            data.copy(
                donations = updatedList,
                notices = listOf(newNotice) + data.notices.filterNot { it.id == newNotice.id }
            )
        }
    }
}
