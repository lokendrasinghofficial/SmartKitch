package com.smartkitch.app.data.repository

import com.smartkitch.app.data.model.FoodItem
import com.smartkitch.app.domain.repository.InventoryRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class InventoryRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val generativeModel: com.google.ai.client.generativeai.GenerativeModel
) : InventoryRepository {

    override fun getInventoryItems(userId: String): Flow<List<FoodItem>> {
        return firestore.collection("users")
            .document(userId)
            .collection("inventory")
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    doc.toObject(FoodItem::class.java)?.copy(id = doc.id)
                }
            }
    }

    override suspend fun addItem(userId: String, item: FoodItem) {
        firestore.collection("users")
            .document(userId)
            .collection("inventory")
            .add(item)
            .await()
    }

    override suspend fun updateItem(userId: String, item: FoodItem) {
        if (item.id.isNotEmpty()) {
            firestore.collection("users")
                .document(userId)
                .collection("inventory")
                .document(item.id)
                .set(item)
                .await()
        }
    }

    override suspend fun deleteItem(userId: String, itemId: String) {
        firestore.collection("users")
            .document(userId)
            .collection("inventory")
            .document(itemId)
            .delete()
            .await()
    }

    override suspend fun scanImage(bitmap: android.graphics.Bitmap): List<FoodItem> {
        val prompt = """
            Identify all food items in this image. 
            Return a JSON array where each object has:
            - "name": String (e.g., "Apple")
            - "quantity": Number (estimated count, default 1)
            - "unit": String (e.g., "pcs", "kg", "pack")
            - "category": String (e.g., "Fruit", "Vegetable", "Dairy", "Meat", "Grains")
            - "location": String (Must be one of: "Fridge", "Freezer", "Pantry")
            - "expiryDate": Long (timestamp in milliseconds from now, estimate based on item type)
            
            Use these rules for "location":
            - FRIDGE: fruits (apple, banana, etc.), vegetables, dairy (milk, cheese), drinks, fresh herbs.
            - FREEZER: meat (chicken, beef), fish, frozen foods, ice cream.
            - PANTRY: oil, grains (rice, pasta), spices, sauces, packaged goods (chips, biscuits).
            
            Return ONLY the JSON array. Do not include markdown formatting.
        """.trimIndent()

        try {
            val inputContent = com.google.ai.client.generativeai.type.content {
                image(bitmap)
                text(prompt)
            }

            val response = generativeModel.generateContent(inputContent)
            val responseText = response.text?.replace("```json", "")?.replace("```", "")?.trim() ?: "[]"
            
            val gson = com.google.gson.Gson()
            val itemType = object : com.google.gson.reflect.TypeToken<List<ScanResultItem>>() {}.type
            val scannedItems: List<ScanResultItem> = gson.fromJson(responseText, itemType)
            
            return scannedItems.map { 
                FoodItem(
                    name = it.name,
                    quantity = it.quantity.toDouble(),
                    unit = it.unit,
                    category = it.category,
                    expiryDate = System.currentTimeMillis() + (it.expiryDate ?: (7 * 24 * 60 * 60 * 1000L)), // Default 7 days if null
                    location = it.location ?: "Pantry" // Default to Pantry if null
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("InventoryRepository", "Error scanning image", e)
            return emptyList()
        }
    }

    // Helper class for parsing JSON
    private data class ScanResultItem(
        val name: String,
        val quantity: Int,
        val unit: String,
        val category: String,
        val location: String?,
        val expiryDate: Long?
    )
}
