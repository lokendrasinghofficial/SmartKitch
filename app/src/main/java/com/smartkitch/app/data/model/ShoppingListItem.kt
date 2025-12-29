package com.smartkitch.app.data.model

import com.google.firebase.firestore.DocumentId

data class ShoppingListItem(
    @DocumentId val id: String = "",
    val name: String = "",
    val quantity: Double = 1.0,
    val unit: String = "pcs",
    @get:com.google.firebase.firestore.PropertyName("isPurchased") val isPurchased: Boolean = false,
    @get:com.google.firebase.firestore.PropertyName("isSuggestion") val isSuggestion: Boolean = false,
    val imageUrl: String? = null,
    val suggestionReason: String? = null, // "Expired" or "Low Stock"
    val addedAt: Long = System.currentTimeMillis()
)
