package com.smartkitch.app.presentation.shopping

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartkitch.app.data.model.ShoppingListItem
import com.smartkitch.app.domain.repository.AuthRepository
import com.smartkitch.app.domain.repository.ShoppingListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShoppingListViewModel @Inject constructor(
    private val repository: ShoppingListRepository,
    private val authRepository: AuthRepository,
    private val inventoryRepository: com.smartkitch.app.domain.repository.InventoryRepository,
    private val profileRepository: com.smartkitch.app.domain.repository.ProfileRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<ShoppingListUiState>(ShoppingListUiState.Loading)
    val uiState: StateFlow<ShoppingListUiState> = _uiState.asStateFlow()

    init {
        val userId = authRepository.getCurrentUserId()
        if (userId != null) {
            viewModelScope.launch {
                repository.getShoppingList(userId)
                    .catch { e -> _uiState.value = ShoppingListUiState.Error(e.message ?: "Unknown error") }
                    .collect { items ->
                        _uiState.value = ShoppingListUiState.Success(items)
                    }
            }
            startAutoRemoveCheck(userId)
        } else {
            _uiState.value = ShoppingListUiState.Error("User not logged in")
        }
    }

    private fun startAutoRemoveCheck(userId: String) {
        viewModelScope.launch {
            // Combine inventory and profile settings to check for auto-removal
            // This ensures that even if InventoryViewModel didn't catch it, we do it here.
            kotlinx.coroutines.flow.combine(
                inventoryRepository.getInventoryItems(userId),
                profileRepository.getUserProfile(userId)
            ) { items, profile ->
                Pair(items, profile)
            }.collect { (items, profile) ->
                android.util.Log.d("ShoppingListVM", "AutoRemoveCheck: profile=${profile?.autoRemoveExpired}, items=${items.size}")
                val currentTime = System.currentTimeMillis()

                // 1. Expired Items (Only if Auto Remove is ON)
                if (profile?.autoRemoveExpired == true) {
                    val expiredItems = items.filter { it.expiryDate < currentTime }
                    android.util.Log.d("ShoppingListVM", "Found ${expiredItems.size} expired items")
                    
                    expiredItems.forEach { item ->
                        val shoppingItem = com.smartkitch.app.data.model.ShoppingListItem(
                            name = item.name,
                            quantity = item.quantity,
                            unit = item.unit,
                            isSuggestion = true,
                            imageUrl = item.imageUrl,
                            suggestionReason = "Expired"
                        )
                        repository.addItem(userId, shoppingItem)
                        inventoryRepository.deleteItem(userId, item.id)
                    }
                }

                // 2. Low Stock Items (Always check)
                // Suggest if quantity <= 2 and NOT expired
                val lowStockItems = items.filter { it.expiryDate >= currentTime && it.quantity <= 2.0 }
                android.util.Log.d("ShoppingListVM", "Found ${lowStockItems.size} low stock items")

                lowStockItems.forEach { item ->
                    // Optimization: In a real app, check for duplicates in shopping list.
                    // For now, we just add as suggestion.
                    val shoppingItem = com.smartkitch.app.data.model.ShoppingListItem(
                        name = item.name,
                        quantity = item.quantity,
                        unit = item.unit,
                        isSuggestion = true,
                        imageUrl = item.imageUrl,
                        suggestionReason = "Low Stock"
                    )
                    repository.addItem(userId, shoppingItem)
                }
            }
        }
    }

    fun addItem(name: String, quantity: Double, unit: String) {
        val userId = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            val newItem = ShoppingListItem(
                name = name,
                quantity = quantity,
                unit = unit
            )
            repository.addItem(userId, newItem)
        }
    }

    fun deleteItem(itemId: String) {
        val userId = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            repository.deleteItem(userId, itemId)
        }
    }

    fun togglePurchased(itemId: String, isPurchased: Boolean) {
        android.util.Log.d("ShoppingListVM", "togglePurchased: id=$itemId, new=$isPurchased")
        val userId = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            repository.togglePurchased(userId, itemId, isPurchased)
        }
    }

    fun updateItem(item: ShoppingListItem) {
        val userId = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            repository.updateItem(userId, item)
        }
    }

    fun clearSuggestions() {
        val userId = authRepository.getCurrentUserId() ?: return
        val currentState = _uiState.value
        if (currentState is ShoppingListUiState.Success) {
            val suggestionItems = currentState.items.filter { it.isSuggestion }
            viewModelScope.launch {
                suggestionItems.forEach { item ->
                    repository.deleteItem(userId, item.id)
                }
            }
        }
    }

    fun shareShoppingList() {
        val currentState = _uiState.value
        if (currentState is ShoppingListUiState.Success) {
            // Filter only purchased (selected) items
            val selectedItems = currentState.items.filter { it.isPurchased }
            if (selectedItems.isEmpty()) return

            val sb = StringBuilder()
            sb.append("🛒 *Grocery Shopping List*\n")
            sb.append("———————————————\n")
            selectedItems.forEach { item ->
                sb.append("✅ ${item.name} — ${item.quantity} ${item.unit}\n")
            }
            sb.append("\nSent from the KitchApp 🧑‍🍳")

            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, sb.toString())
                type = "text/plain"
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val shareIntent = Intent.createChooser(sendIntent, "Share Shopping List")
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(shareIntent)
        }
    }
}

sealed class ShoppingListUiState {
    object Loading : ShoppingListUiState()
    data class Success(val items: List<ShoppingListItem>) : ShoppingListUiState()
    data class Error(val message: String) : ShoppingListUiState()
}
