package com.sempatapps.presentation.donation

import androidx.annotation.DrawableRes
import com.sempatapps.R
import com.sempatapps.domain.model.DonationRecipient
import com.sempatapps.domain.model.FoodCheckResult

enum class FoodCategory(
    val label: String,
    @DrawableRes val iconRes: Int
) {
    KEBUTUHAN_POKOK("Kebutuhan Pokok", R.drawable.cat_kebutuhan_pokok),
    SAYUR_BUAH("Sayur & Buah", R.drawable.cat_sayur_buah),
    MAKANAN_SIAP_SAJI("Makanan Siap Saji", R.drawable.cat_makanan_siap_saji),
    ROTI_KUE("Roti & Kue", R.drawable.cat_roti_kue),
    LAINNYA("Lainnya", R.drawable.cat_lainnya)
}

enum class WeightUnit(val shortLabel: String, val fullSuffix: String) {
    KG("Kg", "kg"),
    GRAM("g", "gram")
}

enum class DonationStep {
    FORM,
    CAMERA,
    PHOTO_PREVIEW,
    AI_CHECKING,
    AI_BUSY,
    RESULT,
    RECIPIENT_SELECTION
}

enum class DonationDialogType {
    NONE,
    CANCEL_DONATION,
    DONATION_SAVED,
    EXIT_RECIPIENT,
    DISTRIBUTED_SUCCESS
}

enum class RecipientSort {
    NEAREST,
    MOST_NEEDED
}

data class GalleryPhotoOption(
    val id: String,
    val imageName: String,
    @DrawableRes val drawableRes: Int
)

val sampleGalleryPhotos = listOf(
    GalleryPhotoOption("g1", "bread_photo", R.drawable.bread_photo),
    GalleryPhotoOption("g2", "veggie_photo", R.drawable.veggie_photo),
    GalleryPhotoOption("g3", "bread_photo_alt", R.drawable.bread_photo_alt),
    GalleryPhotoOption("g4", "bread_photo", R.drawable.bread_photo),
    GalleryPhotoOption("g5", "bread_photo_alt", R.drawable.bread_photo_alt),
    GalleryPhotoOption("g6", "veggie_photo", R.drawable.veggie_photo),
    GalleryPhotoOption("g7", "bread_photo", R.drawable.bread_photo),
    GalleryPhotoOption("g8", "bread_photo_alt", R.drawable.bread_photo_alt),
    GalleryPhotoOption("g9", "veggie_photo", R.drawable.veggie_photo)
)

val sampleDonationRecipients = listOf(
    DonationRecipient(
        id = "r1",
        type = "Yayasan",
        name = "Panti Asuhan Fasilkom",
        needCategory = "Roti & Kue",
        matchPercent = 94,
        distanceKm = 1.5,
        urgencyRank = 1
    ),
    DonationRecipient(
        id = "r2",
        type = "Yayasan",
        name = "Panti Jompo Drian",
        needCategory = "Roti & Kue",
        matchPercent = 87,
        distanceKm = 2.5,
        urgencyRank = 2
    ),
    DonationRecipient(
        id = "r3",
        type = "Individu",
        name = "Pak Zahran",
        needCategory = "Makanan Siap Saji",
        matchPercent = 83,
        distanceKm = 1.9,
        urgencyRank = 3
    )
)

data class DonationFlowState(
    val step: DonationStep = DonationStep.FORM,
    val dialog: DonationDialogType = DonationDialogType.NONE,
    val foodName: String = "",
    val weightInput: String = "",
    val weightUnit: WeightUnit = WeightUnit.KG,
    val selectedCategory: FoodCategory? = null,
    val selectedPhotoName: String? = null,
    val pendingPhotoName: String? = null,
    val checkResult: FoodCheckResult? = null,
    val recipientSort: RecipientSort = RecipientSort.NEAREST,
    val selectedRecipient: DonationRecipient? = null,
    val savedDonationId: String? = null
) {
    val isFormComplete: Boolean
        get() = selectedPhotoName != null &&
            foodName.isNotBlank() &&
            weightInput.isNotBlank() &&
            selectedCategory != null

    val hasUnsavedInput: Boolean
        get() = selectedPhotoName != null ||
            foodName.isNotBlank() ||
            weightInput.isNotBlank() ||
            selectedCategory != null

    val formattedWeight: String
        get() {
            val cleaned = weightInput.trim()
            if (cleaned.isEmpty()) return ""
            val hasUnit = cleaned.endsWith("kg", ignoreCase = true) ||
                cleaned.endsWith("g", ignoreCase = true) ||
                cleaned.endsWith("gram", ignoreCase = true)
            return if (hasUnit) cleaned else "$cleaned${weightUnit.fullSuffix}"
        }

    val compactWeight: String
        get() {
            val cleaned = weightInput.trim()
            if (cleaned.isEmpty()) return ""
            val hasUnit = cleaned.endsWith("kg", ignoreCase = true) ||
                cleaned.endsWith("g", ignoreCase = true)
            return if (hasUnit) cleaned else "$cleaned${if (weightUnit == WeightUnit.KG) "kg" else "g"}"
        }
}
