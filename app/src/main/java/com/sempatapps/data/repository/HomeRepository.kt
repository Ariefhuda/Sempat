package com.sempatapps.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import com.sempatapps.domain.model.*


interface HomeRepository {
    fun observe(role: HomeRole): StateFlow<HomeData>
    fun selectLocation(id: String)
    fun markNoticeRead(role: HomeRole, id: String)
    fun setNeedsHelp(enabled: Boolean)
    fun addDonation(donation: Donation, recipientName: String? = null)
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
}
