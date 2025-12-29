package com.smartkitch.app.data.model

data class Recipe(
    val title: String,
    val type: String,
    val description: String,
    val missingIngredients: List<String>,
    val ingredients: List<String> = emptyList(),
    val instructions: List<String> = emptyList(),
    val cookingTime: String = "Unknown",
    val difficulty: String = "Medium",
    val servingSize: String = "2 servings",
    val tips: String? = null,
    val youtubeQuery: String? = null,
    val youtubeVideoId: String? = null
)
