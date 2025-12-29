package com.smartkitch.app.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smartkitch.app.presentation.inventory.InventoryViewModel
import com.smartkitch.app.ui.theme.SageGreen
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.layout.Box

@Composable
fun DashboardScreen(
    viewModel: InventoryViewModel = hiltViewModel(),
    profileViewModel: com.smartkitch.app.presentation.profile.ProfileViewModel = hiltViewModel(),
    recipeViewModel: com.smartkitch.app.presentation.recipe.RecipeViewModel = hiltViewModel(),
    onNavigateToInventory: (String?) -> Unit,
    onNavigateToAddItem: () -> Unit,
    onNavigateToShoppingList: () -> Unit,
    onNavigateToScan: () -> Unit,
    onNavigateToRecipe: (com.smartkitch.app.data.model.Recipe) -> Unit
) {
    val items by viewModel.inventoryItems.collectAsState()
    val userName by profileViewModel.name.collectAsState()
    
    val fridgeCount = items.count { it.location == "Fridge" }
    val freezerCount = items.count { it.location == "Freezer" }
    val pantryCount = items.count { it.location == "Pantry" }
    
    // Filter for expiring items (next 7 days)
    val expiringItems = items.filter { 
        val daysUntilExpiry = ((it.expiryDate - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).toInt()
        daysUntilExpiry in 0..7
    }.sortedBy { it.expiryDate }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA)) // Very light gray background
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (userName.isNotBlank()) "Hello, $userName" else "Hello, Chef",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )
            androidx.compose.material3.IconButton(onClick = onNavigateToShoppingList) {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = "Shopping List",
                    tint = Color(0xFF333333)
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        // Storage Summary
        Text(
            text = "Storage Overview",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF666666)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            CircularStorageIndicator(
                title = "Fridge", 
                count = fridgeCount, 
                color = Color(0xFF66BB6A), // Green
                onClick = { onNavigateToInventory("Fridge") }
            )
            CircularStorageIndicator(
                title = "Freezer", 
                count = freezerCount, 
                color = Color(0xFF42A5F5), // Blue
                onClick = { onNavigateToInventory("Freezer") }
            )
            CircularStorageIndicator(
                title = "Pantry", 
                count = pantryCount, 
                color = Color(0xFFFFA726), // Orange
                onClick = { onNavigateToInventory("Pantry") }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Expiring Soon Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Expiring Soon",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF666666)
            )
            TextButton(onClick = { onNavigateToInventory("Expiring") }) {
                Text("See All", color = SageGreen)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        
        if (expiringItems.isEmpty()) {
            Text(
                text = "No items expiring soon. Good job!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(expiringItems) { item ->
                    ExpiringItemCard(item = item)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Save Food with AI Section
        val wasteSaverState by recipeViewModel.wasteSaverUiState.collectAsState()
        
        // Trigger generation if initial
        androidx.compose.runtime.LaunchedEffect(Unit) {
            if (wasteSaverState is com.smartkitch.app.presentation.recipe.WasteSaverUiState.Initial) {
                recipeViewModel.generateWasteSaverRecipe()
            }
        }

        when (val state = wasteSaverState) {
            is com.smartkitch.app.presentation.recipe.WasteSaverUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color.White, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = SageGreen)
                }
            }
            is com.smartkitch.app.presentation.recipe.WasteSaverUiState.Success -> {
                val savedRecipeTitles by recipeViewModel.savedRecipeTitles.collectAsState()
                val isSaved = savedRecipeTitles.contains(state.recipe.title)

                WasteSaverCard(
                    recipe = state.recipe,
                    expiringItems = state.expiringItems,
                    isSaved = isSaved,
                    onViewRecipe = { onNavigateToRecipe(state.recipe) },
                    onGenerateAnother = { recipeViewModel.generateWasteSaverRecipe() },
                    onSaveClick = { recipeViewModel.saveRecipe(state.recipe) }
                )
            }
            is com.smartkitch.app.presentation.recipe.WasteSaverUiState.Empty -> {
                // Do nothing or show a specific message if desired, but "No items expiring soon" above covers it mostly.
                // Maybe a specific "AI Chef is happy" card?
                // For now, let's just show a small text if we want, or nothing.
                // Requirement says: "If no items are near expiry: Show a message... Hide the recipe card."
                // We already show "No items expiring soon" above.
            }
            is com.smartkitch.app.presentation.recipe.WasteSaverUiState.Error -> {
                // Silently fail or show retry?
                // Let's show a retry button
                TextButton(onClick = { recipeViewModel.generateWasteSaverRecipe() }) {
                    Text("Retry AI Chef", color = SageGreen)
                }
            }
            else -> {}
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Quick Actions
        Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF666666)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            QuickActionButton(
                text = "Add Item",
                icon = Icons.Default.Add,
                onClick = onNavigateToAddItem
            )
            QuickActionButton(
                text = "Shopping List",
                icon = Icons.Default.List,
                onClick = onNavigateToShoppingList
            )
            QuickActionButton(
                text = "Scan",
                icon = Icons.Default.Search, // Placeholder for Scanner
                onClick = onNavigateToScan
            )
        }
        
        Spacer(modifier = Modifier.height(80.dp)) // Bottom padding for navigation bar
    }
}
