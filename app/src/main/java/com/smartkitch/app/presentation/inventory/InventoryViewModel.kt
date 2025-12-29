package com.smartkitch.app.presentation.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartkitch.app.data.model.FoodItem
import com.smartkitch.app.domain.repository.InventoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val repository: InventoryRepository,
    private val authRepository: com.smartkitch.app.domain.repository.AuthRepository,
    private val notificationHelper: com.smartkitch.app.util.NotificationHelper,
    private val profileRepository: com.smartkitch.app.domain.repository.ProfileRepository,
    private val shoppingListRepository: com.smartkitch.app.domain.repository.ShoppingListRepository
) : ViewModel() {

    private val userId = authRepository.getCurrentUserId() ?: ""

    val inventoryItems: StateFlow<List<FoodItem>> = if (userId.isNotEmpty()) {
        repository.getInventoryItems(userId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    } else {
        MutableStateFlow(emptyList())
    }

    init {
        startAutoRemoveCheck()
    }

    private fun startAutoRemoveCheck() {
        if (userId.isEmpty()) return
        
        viewModelScope.launch {
            // Combine inventory and profile settings to check for auto-removal
            // We use collectLatest to cancel previous work if state changes
            kotlinx.coroutines.flow.combine(
                inventoryItems,
                profileRepository.getUserProfile(userId)
            ) { items, profile ->
                Pair(items, profile)
            }.collect { (items, profile) ->
                if (profile?.autoRemoveExpired == true) {
                    val currentTime = System.currentTimeMillis()
                    val expiredItems = items.filter { it.expiryDate < currentTime }
                    
                    expiredItems.forEach { item ->
                        // 1. Add to Shopping List as Suggestion
                        val shoppingItem = com.smartkitch.app.data.model.ShoppingListItem(
                            name = item.name,
                            quantity = item.quantity,
                            unit = item.unit,
                            isSuggestion = true,
                            imageUrl = item.imageUrl
                        )
                        shoppingListRepository.addItem(userId, shoppingItem)
                        
                        // 2. Remove from Inventory
                        repository.deleteItem(userId, item.id)
                    }
                }
            }
        }
    }

    fun addItem(name: String, category: String, quantity: Double, unit: String, expiryDate: Long, location: String) {
        viewModelScope.launch {
            val newItem = FoodItem(
                name = name,
                category = category,
                quantity = quantity,
                unit = unit,
                expiryDate = expiryDate,
                location = location
            )
            repository.addItem(userId, newItem)
            
            // Simple check for immediate notification if added item is already expiring soon (for demo)
            if (newItem.isExpiringSoon()) {
                notificationHelper.showExpiryNotification(newItem)
            }
        }
    }

    fun updateItem(item: FoodItem) {
        if (userId.isEmpty()) return
        viewModelScope.launch {
            repository.updateItem(userId, item)
        }
    }

    fun deleteItem(itemId: String) {
        if (userId.isEmpty()) return
        viewModelScope.launch {
            repository.deleteItem(userId, itemId)
        }
    }
}
