package com.smartkitch.app.data.model

import com.google.firebase.firestore.DocumentId

data class FoodItem(
    @DocumentId val id: String = "",
    val name: String = "",
    val category: String = "General",
    val quantity: Double = 0.0,
    val unit: String = "pcs",
    val expiryDate: Long = 0L,
    val addedDate: Long = System.currentTimeMillis(),
    val location: String = "Fridge",
    val imageUrl: String? = null
) {
    fun isExpiringSoon(): Boolean {
        val daysUntilExpiry = (expiryDate - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)
        return daysUntilExpiry in 0..3
    }
}
