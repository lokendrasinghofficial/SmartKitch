package com.smartkitch.app.domain.repository

import com.smartkitch.app.data.model.FoodItem
import kotlinx.coroutines.flow.Flow

interface InventoryRepository {
    fun getInventoryItems(userId: String): Flow<List<FoodItem>>
    suspend fun addItem(userId: String, item: FoodItem)
    suspend fun updateItem(userId: String, item: FoodItem)
    suspend fun deleteItem(userId: String, itemId: String)
    suspend fun scanImage(bitmap: android.graphics.Bitmap): List<FoodItem>
}
