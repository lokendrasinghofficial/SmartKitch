package com.smartkitch.app.data.model

import com.google.firebase.firestore.DocumentId

data class UserProfile(
    @DocumentId val id: String = "",
    val name: String = "",
    val age: Int = 0,
    val foodPreference: String = "", // Vegan, Vegetarian, Non-Vegetarian, Halal
    val dietRestrictions: List<String> = emptyList(), // Dairy-free, Gluten-free, etc.
    val profileImageUrl: String? = null,
    val preferredCuisine: String = "Italian",
    val spiceLevel: Float = 0.5f,
    val cookingTime: String = "30 min",
    val voiceAssistantEnabled: Boolean = false,
    val autoRemoveExpired: Boolean = false,
    val appLanguage: String = "English",
    val region: String = "United States",
    val expiryAlerts: Boolean = true,
    val aiSuggestions: Boolean = true
)
