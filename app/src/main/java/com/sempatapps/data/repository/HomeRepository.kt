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
}

class DummyHomeRepository : HomeRepository {
    private val donor = MutableStateFlow(
        HomeData(
            displayName = "Pengguna",
            role = HomeRole.DONOR,
            donatedWeight = "8 kg",
            sharedCount = "3 pangan",
            donations = listOf(
                Donation("d1", "Roti Enak", "Roti & Kue", "250 g", "Zahran Bakery, Medan", "1,5 km", "bread_photo", "Tersedia"),
                Donation("d2", "Roti Hangat", "Roti & Kue", "250 g", "Zahran Bakery, Medan", "1,2 km", "bread_photo_alt", "Disalurkan")
            ),
            notices = listOf(
                HomeNotice("n1", "Pangan berhasil didonasikan", "Roti Enak sudah tersedia untuk penerima di sekitar.", "Hari ini"),
                HomeNotice("n2", "Pangan sudah diterima", "Roti Hangat telah diambil oleh penerima.", "Kemarin")
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
}
