package com.sempatapps.domain.model

enum class HomeRole { DONOR, RECIPIENT }

data class Donation(
    val id: String,
    val name: String,
    val category: String,
    val quantity: String,
    val pickupAddress: String,
    val distance: String,
    val imageName: String,
    val status: String,
    val imageUri: String? = null
)

data class HomeNotice(
    val id: String,
    val title: String,
    val message: String,
    val day: String,
    val isRead: Boolean = false,
    val highlightText: String = "",
    val iconName: String = ""
)

data class PickupLocation(
    val id: String,
    val title: String,
    val address: String
)

data class HomeData(
    val displayName: String,
    val role: HomeRole,
    val donatedWeight: String = "",
    val sharedCount: String = "",
    val pickupAddress: String = "",
    val selectedLocationId: String = "",
    val locations: List<PickupLocation> = emptyList(),
    val donations: List<Donation> = emptyList(),
    val notices: List<HomeNotice> = emptyList(),
    val needsHelp: Boolean = false
) {
    val selectedLocation: PickupLocation?
        get() = locations.firstOrNull { it.id == selectedLocationId }
}

data class DonationRecipient(
    val id: String,
    val type: String,
    val name: String,
    val needCategory: String,
    val matchPercent: Int,
    val distanceKm: Double,
    val urgencyRank: Int
)

data class FoodCheckResult(
    val name: String,
    val category: String,
    val weightLabel: String,
    val condition: String,
    val shelfLife: String,
    val isEligible: Boolean,
    val recommendation: String,
    val imageName: String,
    val imageUri: String? = null
)
