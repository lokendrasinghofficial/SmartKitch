package com.smartkitch.app.presentation.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartkitch.app.data.model.SavedRecipe
import com.smartkitch.app.domain.repository.AuthRepository
import com.smartkitch.app.domain.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SavedRecipesViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val userId = authRepository.getCurrentUserId() ?: ""

    val savedRecipes: StateFlow<List<SavedRecipe>> = if (userId.isNotEmpty()) {
        recipeRepository.getSavedRecipes(userId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    } else {
        MutableStateFlow(emptyList())
    }

    fun deleteRecipe(recipeId: String) {
        if (userId.isEmpty()) return
        viewModelScope.launch {
            recipeRepository.deleteSavedRecipe(userId, recipeId)
        }
    }
}
