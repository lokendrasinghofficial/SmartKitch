package com.smartkitch.app.di

import com.smartkitch.app.data.repository.InventoryRepositoryImpl
import com.smartkitch.app.domain.repository.InventoryRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore {
        return Firebase.firestore
    }

    @Provides
    @Singleton
    fun provideInventoryRepository(
        firestore: FirebaseFirestore,
        generativeModel: com.google.ai.client.generativeai.GenerativeModel
    ): InventoryRepository {
        return InventoryRepositoryImpl(firestore, generativeModel)
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): com.google.firebase.auth.FirebaseAuth {
        return com.google.firebase.auth.FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseAuth: com.google.firebase.auth.FirebaseAuth
    ): com.smartkitch.app.domain.repository.AuthRepository {
        return com.smartkitch.app.data.repository.AuthRepositoryImpl(firebaseAuth)
    }

    @Provides
    @Singleton
    fun provideGenerativeModel(): com.google.ai.client.generativeai.GenerativeModel {
        // FIXME: Replace with actual API key or use BuildConfig
        return com.google.ai.client.generativeai.GenerativeModel(
            modelName = "gemini-2.0-flash",
            apiKey = "AIzaSyCnUGcZYmKv3-O89AAvUSbfzZZEVDItF4M"
        )
    }

    @Provides
    @Singleton
    fun provideFirebaseStorage(): com.google.firebase.storage.FirebaseStorage {
        return com.google.firebase.storage.FirebaseStorage.getInstance()
    }

    @Provides
    @Singleton
    fun provideProfileRepository(
        firestore: FirebaseFirestore,
        storage: com.google.firebase.storage.FirebaseStorage
    ): com.smartkitch.app.domain.repository.ProfileRepository {
        return com.smartkitch.app.data.repository.ProfileRepositoryImpl(firestore, storage)
    }

    @Provides
    @Singleton
    fun provideRecipeRepository(
        firestore: FirebaseFirestore
    ): com.smartkitch.app.domain.repository.RecipeRepository {
        return com.smartkitch.app.data.repository.RecipeRepositoryImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideShoppingListRepository(
        firestore: FirebaseFirestore
    ): com.smartkitch.app.domain.repository.ShoppingListRepository {
        return com.smartkitch.app.data.repository.ShoppingListRepositoryImpl(firestore)
    }
}
