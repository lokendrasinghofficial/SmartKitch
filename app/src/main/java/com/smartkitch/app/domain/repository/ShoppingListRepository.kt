package com.smartkitch.app.domain.repository

import com.smartkitch.app.data.model.ShoppingListItem
import kotlinx.coroutines.flow.Flow

interface ShoppingListRepository {
    fun getShoppingList(userId: String): Flow<List<ShoppingListItem>>
    suspend fun addItem(userId: String, item: ShoppingListItem)
    suspend fun updateItem(userId: String, item: ShoppingListItem)
    suspend fun deleteItem(userId: String, itemId: String)
    suspend fun togglePurchased(userId: String, itemId: String, isPurchased: Boolean)
}
