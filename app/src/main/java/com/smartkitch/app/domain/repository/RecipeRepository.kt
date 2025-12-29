package com.smartkitch.app.domain.repository

import com.smartkitch.app.data.model.SavedRecipe
import kotlinx.coroutines.flow.Flow

interface RecipeRepository {
    fun getSavedRecipes(userId: String): Flow<List<SavedRecipe>>
    suspend fun saveRecipe(userId: String, recipe: SavedRecipe)
    suspend fun deleteSavedRecipe(userId: String, recipeId: String)
}
