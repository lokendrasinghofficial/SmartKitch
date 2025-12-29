package com.smartkitch.app.presentation.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartkitch.app.domain.repository.InventoryRepository
import com.google.ai.client.generativeai.GenerativeModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipeViewModel @Inject constructor(
    private val generativeModel: GenerativeModel,
    private val repository: InventoryRepository,
    private val recipeRepository: com.smartkitch.app.domain.repository.RecipeRepository,
    private val authRepository: com.smartkitch.app.domain.repository.AuthRepository,
    private val profileRepository: com.smartkitch.app.domain.repository.ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<RecipeUiState>(RecipeUiState.Initial)
    val uiState: StateFlow<RecipeUiState> = _uiState.asStateFlow()

    private val _wasteSaverUiState = MutableStateFlow<WasteSaverUiState>(WasteSaverUiState.Initial)
    val wasteSaverUiState: StateFlow<WasteSaverUiState> = _wasteSaverUiState.asStateFlow()

    private val userId = authRepository.getCurrentUserId() ?: ""
    
    // Set of saved recipe titles for simple UI state management (in a real app, use IDs)
    private val _savedRecipeTitles = MutableStateFlow<Set<String>>(emptySet())
    val savedRecipeTitles: StateFlow<Set<String>> = _savedRecipeTitles.asStateFlow()

    private val _selectedRecipe = MutableStateFlow<com.smartkitch.app.data.model.Recipe?>(null)
    val selectedRecipe: StateFlow<com.smartkitch.app.data.model.Recipe?> = _selectedRecipe.asStateFlow()

    fun selectRecipe(recipe: com.smartkitch.app.data.model.Recipe) {
        _selectedRecipe.value = recipe
    }

    fun saveRecipe(recipe: com.smartkitch.app.data.model.Recipe) {
        if (userId.isEmpty()) return
        viewModelScope.launch {
            val savedRecipe = com.smartkitch.app.data.model.SavedRecipe(
                userId = userId,
                title = recipe.title,
                type = recipe.type,
                description = recipe.description,
                missingIngredients = recipe.missingIngredients,
                ingredients = recipe.ingredients,
                instructions = recipe.instructions,
                cookingTime = recipe.cookingTime,
                difficulty = recipe.difficulty
            )
            recipeRepository.saveRecipe(userId, savedRecipe)
            _savedRecipeTitles.value = _savedRecipeTitles.value + recipe.title
        }
    }

    fun generateRecipes() {
        if (userId.isEmpty()) {
            _uiState.value = RecipeUiState.Error("User not logged in")
            return
        }
        viewModelScope.launch {
            _uiState.value = RecipeUiState.Loading
            try {
                val recipes = fetchRecipes()
                if (recipes.isNotEmpty()) {
                    _uiState.value = RecipeUiState.Success(recipes)
                } else {
                    _uiState.value = RecipeUiState.Error("No recipes found.")
                }
            } catch (e: Exception) {
                _uiState.value = RecipeUiState.Error("Failed to generate recipes: ${e.message}")
            }
        }
    }

    fun generateMoreRecipes() {
        val currentState = _uiState.value
        if (currentState is RecipeUiState.Success && !currentState.isLoadingMore) {
            viewModelScope.launch {
                _uiState.value = currentState.copy(isLoadingMore = true)
                try {
                    val newRecipes = fetchRecipes()
                    if (newRecipes.isNotEmpty()) {
                        _uiState.value = currentState.copy(
                            recipes = currentState.recipes + newRecipes,
                            isLoadingMore = false
                        )
                    } else {
                        _uiState.value = currentState.copy(isLoadingMore = false)
                    }
                } catch (e: Exception) {
                    // On error, just stop loading more, keep existing recipes
                    _uiState.value = currentState.copy(isLoadingMore = false)
                }
            }
        }
    }

    private suspend fun fetchRecipes(): List<com.smartkitch.app.data.model.Recipe> {
        val inventoryItems = repository.getInventoryItems(userId).first()
        if (inventoryItems.isEmpty()) {
            return emptyList()
        }

        val ingredients = inventoryItems.joinToString { it.name }
        
        // Fetch user preferences
        val userProfile = profileRepository.getUserProfile(userId).first()
        val cuisinePref = userProfile?.preferredCuisine ?: "Any"
        val spicePref = if ((userProfile?.spiceLevel ?: 0.5f) > 0.7f) "High" else if ((userProfile?.spiceLevel ?: 0.5f) > 0.3f) "Medium" else "Low"
        val timePref = userProfile?.cookingTime ?: "Any"
        val foodPref = userProfile?.foodPreference ?: "Any"
        val dietRestrictions = userProfile?.dietRestrictions?.joinToString(", ") ?: "None"

        val prompt = """
            I have the following ingredients in my kitchen: $ingredients.
            My cooking preferences and restrictions are:
            - Preferred Cuisine: $cuisinePref
            - Spice Level: $spicePref
            - Preferred Cooking Time: $timePref
            - Food Preference: $foodPref (STRICTLY FOLLOW THIS. If Vegan/Vegetarian, NO meat/animal products.)
            - Diet Restrictions: $dietRestrictions (STRICTLY FOLLOW THESE)
            
            Please suggest 3-4 distinct types of recipes (e.g., Breakfast, Lunch, Dinner, Snack) that I can make using these ingredients and matching my preferences where possible.
            
            Return ONLY a valid JSON array of objects. Do not include markdown formatting like ```json ... ```.
            Each object should have the following fields:
            - "title": String
            - "type": String (e.g., Breakfast)
            - "description": String
            - "missingIngredients": List<String>
            - "ingredients": List<String> (Full list of ingredients with quantities)
            - "instructions": List<String> (Step-by-step cooking instructions)
            - "cookingTime": String (e.g., "25 minutes")
            - "difficulty": String (Easy, Medium, or Hard)
            - "youtubeQuery": String (A specific search query to find a video for this recipe on YouTube)
            - "youtubeVideoId": String (A valid YouTube video ID for a tutorial on this recipe, if known. e.g. "dQw4w9WgXcQ". If unknown, leave null or empty)
        """.trimIndent()

        val response = generativeModel.generateContent(prompt)
        val responseText = response.text?.replace("```json", "")?.replace("```", "")?.trim() ?: "[]"
        android.util.Log.d("RecipeViewModel", "Gemini Response: $responseText")

        val gson = com.google.gson.Gson()
        val recipeType = object : com.google.gson.reflect.TypeToken<List<com.smartkitch.app.data.model.Recipe>>() {}.type
        return gson.fromJson(responseText, recipeType)
    }

    fun generateWasteSaverRecipe() {
        if (userId.isEmpty()) return
        
        viewModelScope.launch {
            _wasteSaverUiState.value = WasteSaverUiState.Loading
            try {
                val inventoryItems = repository.getInventoryItems(userId).first()
                val expiringItems = inventoryItems.filter { 
                    val daysUntilExpiry = ((it.expiryDate - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).toInt()
                    daysUntilExpiry in 0..3
                }

                if (expiringItems.isEmpty()) {
                    _wasteSaverUiState.value = WasteSaverUiState.Empty
                    return@launch
                }

                val ingredients = expiringItems.joinToString { it.name }
                val userProfile = profileRepository.getUserProfile(userId).first()
                val foodPref = userProfile?.foodPreference ?: "Any"
                val dietRestrictions = userProfile?.dietRestrictions?.joinToString(", ") ?: "None"

                val prompt = """
                    I have these ingredients expiring soon: $ingredients.
                    My dietary requirements are:
                    - Food Preference: $foodPref (STRICTLY FOLLOW THIS)
                    - Diet Restrictions: $dietRestrictions (STRICTLY FOLLOW THESE)
                    
                    Suggest ONE recipe to use as many of them as possible while strictly adhering to my dietary requirements.
                    
                    Return ONLY a valid JSON object (NOT an array). Do not include markdown formatting.
                    The object should have:
                    - "title": String
                    - "type": String
                    - "description": String
                    - "missingIngredients": List<String>
                    - "ingredients": List<String>
                    - "instructions": List<String>
                    - "cookingTime": String
                    - "difficulty": String
                    - "servingSize": String
                    - "youtubeQuery": String
                    - "youtubeVideoId": String
                """.trimIndent()

                val response = generativeModel.generateContent(prompt)
                val responseText = response.text?.replace("```json", "")?.replace("```", "")?.trim() ?: "{}"
                
                val gson = com.google.gson.Gson()
                val recipe = gson.fromJson(responseText, com.smartkitch.app.data.model.Recipe::class.java)
                
                _wasteSaverUiState.value = WasteSaverUiState.Success(recipe, expiringItems.map { it.name })
                
            } catch (e: Exception) {
                _wasteSaverUiState.value = WasteSaverUiState.Error("Failed to generate recipe: ${e.message}")
            }
        }
    }
}

sealed class RecipeUiState {
    object Initial : RecipeUiState()
    object Loading : RecipeUiState()
    data class Success(
        val recipes: List<com.smartkitch.app.data.model.Recipe>,
        val isLoadingMore: Boolean = false
    ) : RecipeUiState()
    data class Error(val message: String) : RecipeUiState()
}

sealed class WasteSaverUiState {
    object Initial : WasteSaverUiState()
    object Loading : WasteSaverUiState()
    object Empty : WasteSaverUiState() // No expiring items
    data class Success(
        val recipe: com.smartkitch.app.data.model.Recipe,
        val expiringItems: List<String>
    ) : WasteSaverUiState()
    data class Error(val message: String) : WasteSaverUiState()
}
