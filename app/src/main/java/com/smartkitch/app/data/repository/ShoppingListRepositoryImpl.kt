package com.smartkitch.app.data.repository

import com.smartkitch.app.data.model.ShoppingListItem
import com.smartkitch.app.domain.repository.ShoppingListRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ShoppingListRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ShoppingListRepository {

    override fun getShoppingList(userId: String): Flow<List<ShoppingListItem>> {
        return firestore.collection("users")
            .document(userId)
            .collection("shopping_list")
            .orderBy("addedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    doc.toObject(ShoppingListItem::class.java)?.copy(id = doc.id)
                }
            }
    }

    override suspend fun addItem(userId: String, item: ShoppingListItem) {
        firestore.collection("users")
            .document(userId)
            .collection("shopping_list")
            .add(item)
            .await()
    }

    override suspend fun updateItem(userId: String, item: ShoppingListItem) {
        if (item.id.isNotEmpty()) {
            firestore.collection("users")
                .document(userId)
                .collection("shopping_list")
                .document(item.id)
                .set(item)
                .await()
        }
    }

    override suspend fun deleteItem(userId: String, itemId: String) {
        firestore.collection("users")
            .document(userId)
            .collection("shopping_list")
            .document(itemId)
            .delete()
            .await()
    }

    override suspend fun togglePurchased(userId: String, itemId: String, isPurchased: Boolean) {
        firestore.collection("users")
            .document(userId)
            .collection("shopping_list")
            .document(itemId)
            .update("isPurchased", isPurchased)
            .await()
    }
}
