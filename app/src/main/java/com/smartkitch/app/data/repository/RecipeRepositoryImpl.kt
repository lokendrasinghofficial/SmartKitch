package com.smartkitch.app.data.repository

import com.smartkitch.app.data.model.SavedRecipe
import com.smartkitch.app.domain.repository.RecipeRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class RecipeRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : RecipeRepository {

    override fun getSavedRecipes(userId: String): Flow<List<SavedRecipe>> {
        return firestore.collection("users")
            .document(userId)
            .collection("saved_recipes")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(SavedRecipe::class.java)
            }
    }

    override suspend fun saveRecipe(userId: String, recipe: SavedRecipe) {
        val collectionRef = firestore.collection("users")
            .document(userId)
            .collection("saved_recipes")
        
        val newDocRef = collectionRef.document()
        val recipeWithId = recipe.copy(id = newDocRef.id, userId = userId)
        
        newDocRef.set(recipeWithId).await()
    }

    override suspend fun deleteSavedRecipe(userId: String, recipeId: String) {
        firestore.collection("users")
            .document(userId)
            .collection("saved_recipes")
            .document(recipeId)
            .delete()
            .await()
    }
}
