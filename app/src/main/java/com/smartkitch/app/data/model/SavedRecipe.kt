package com.smartkitch.app.data.model

data class SavedRecipe(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val type: String = "",
    val description: String = "",
    val missingIngredients: List<String> = emptyList(),
    val ingredients: List<String> = emptyList(),
    val instructions: List<String> = emptyList(),
    val cookingTime: String = "Unknown",
    val difficulty: String = "Medium",
    val timestamp: Long = System.currentTimeMillis()
)
