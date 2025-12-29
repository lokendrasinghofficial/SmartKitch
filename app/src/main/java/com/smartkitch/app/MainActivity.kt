package com.smartkitch.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.smartkitch.app.presentation.inventory.AddItemScreen
import com.smartkitch.app.presentation.inventory.InventoryScreen
import com.smartkitch.app.presentation.navigation.NavigationItem
import com.smartkitch.app.presentation.profile.ProfileSetupScreen
import com.smartkitch.app.presentation.recipe.RecipeScreen
import com.smartkitch.app.presentation.recipe.SavedRecipesScreen
import com.smartkitch.app.ui.theme.KitchAppTheme
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import java.net.URLEncoder
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var authRepository: com.smartkitch.app.domain.repository.AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
        }

        setContent {
            KitchAppTheme {
                val navController = rememberNavController()
                val currentUser = authRepository.getCurrentUserId()
                val isVerified = authRepository.isEmailVerified()
                val startDestination = if (currentUser != null) {
                    if (isVerified) "dashboard" else "verification"
                } else {
                    "login"
                }
                
                // Determine if we should show the bottom bar
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val showBottomBar = currentRoute?.let { route ->
                    route == "dashboard" || 
                    route.startsWith("inventory") || 
                    route == "recipes" || 
                    route == "settings" ||
                    route == "scan"
                } ?: false

                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            NavigationBar(
                                containerColor = androidx.compose.ui.graphics.Color.White,
                                tonalElevation = 8.dp
                            ) {
                                val items = listOf(
                                    NavigationItem.Dashboard,
                                    NavigationItem.Inventory,
                                    NavigationItem.Scan,
                                    NavigationItem.Recipes,
                                    NavigationItem.Settings
                                )
                                items.forEach { item ->
                                    val isSelected = currentRoute == item.route || (item.route == "inventory" && currentRoute?.startsWith("inventory") == true)
                                    NavigationBarItem(
                                        icon = { 
                                            Icon(
                                                imageVector = item.icon, 
                                                contentDescription = item.title,
                                                tint = if (isSelected) androidx.compose.ui.graphics.Color(0xFF4CAF50) else androidx.compose.ui.graphics.Color.Gray // Green for selected, Gray for unselected
                                            ) 
                                        },
                                        label = { 
                                            Text(
                                                text = item.title,
                                                color = if (isSelected) androidx.compose.ui.graphics.Color(0xFF4CAF50) else androidx.compose.ui.graphics.Color.Gray
                                            ) 
                                        },
                                        selected = isSelected,
                                        colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                                            selectedIconColor = androidx.compose.ui.graphics.Color(0xFF4CAF50),
                                            selectedTextColor = androidx.compose.ui.graphics.Color(0xFF4CAF50),
                                            indicatorColor = androidx.compose.ui.graphics.Color.Transparent, // Remove default indicator pill
                                            unselectedIconColor = androidx.compose.ui.graphics.Color.Gray,
                                            unselectedTextColor = androidx.compose.ui.graphics.Color.Gray
                                        ),
                                        onClick = {
                                            navController.navigate(item.route) {
                                                popUpTo("dashboard") {
                                                    inclusive = false
                                                }
                                                launchSingleTop = true
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    KitchAppNavHost(
                        navController = navController,
                        startDestination = startDestination,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun KitchAppNavHost(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable("login") {
            com.smartkitch.app.presentation.auth.LoginScreen(
                onLoginSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate("register") },
                onNavigateToVerification = { navController.navigate("verification") }
            )
        }
        composable("register") {
            com.smartkitch.app.presentation.auth.RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate("verification") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }
        composable("verification") {
            com.smartkitch.app.presentation.auth.VerificationScreen(
                onVerificationSuccess = {
                    navController.navigate("profile_setup") {
                        popUpTo("verification") { inclusive = true }
                    }
                },
                onBackClick = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable("profile_setup") {
            ProfileSetupScreen(
                onProfileSaved = {
                    navController.navigate("dashboard") {
                        popUpTo("profile_setup") { inclusive = true }
                    }
                }
            )
        }
        composable("dashboard") {
            com.smartkitch.app.presentation.dashboard.DashboardScreen(
                onNavigateToInventory = { location ->
                    if (location != null) {
                        navController.navigate("inventory?location=$location")
                    } else {
                        navController.navigate("inventory")
                    }
                },
                onNavigateToAddItem = { navController.navigate("add_item") },
                onNavigateToShoppingList = { navController.navigate("shopping_list") },
                onNavigateToScan = { navController.navigate("scan") },
                onNavigateToRecipe = { recipe ->
                    val recipeJson = Gson().toJson(recipe)
                    val encodedJson = URLEncoder.encode(recipeJson, StandardCharsets.UTF_8.toString())
                    navController.navigate("recipe_detail/$encodedJson")
                }
            )
        }
        composable(
            route = "inventory?location={location}",
            arguments = listOf(androidx.navigation.navArgument("location") { 
                nullable = true
                defaultValue = null
                type = androidx.navigation.NavType.StringType 
            })
        ) { backStackEntry ->
            val location = backStackEntry.arguments?.getString("location")
            InventoryScreen(
                locationFilter = location,
                onAddItemClick = { navController.navigate("add_item") },
                onEditItemClick = { itemId -> navController.navigate("edit_item/$itemId") }
            )
        }
        composable("add_item") {
            AddItemScreen(
                onBackClick = { navController.popBackStack() },
                onItemAdded = { navController.popBackStack() }
            )
        }
        composable("edit_item/{itemId}") { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: return@composable
            com.smartkitch.app.presentation.inventory.EditItemScreen(
                itemId = itemId,
                onBackClick = { navController.popBackStack() },
                onItemUpdated = { navController.popBackStack() }
            )
        }
        composable("recipes") {
            RecipeScreen(
                onNavigateToSavedRecipes = { navController.navigate("saved_recipes") },
                onRecipeClick = { recipe ->
                    val recipeJson = Gson().toJson(recipe)
                    val encodedJson = URLEncoder.encode(recipeJson, StandardCharsets.UTF_8.toString())
                    navController.navigate("recipe_detail/$encodedJson")
                }
            )
        }
        composable("saved_recipes") {
            SavedRecipesScreen(
                onBackClick = { navController.popBackStack() },
                onRecipeClick = { recipe ->
                    val recipeJson = Gson().toJson(recipe)
                    val encodedJson = URLEncoder.encode(recipeJson, StandardCharsets.UTF_8.toString())
                    navController.navigate("recipe_detail/$encodedJson")
                }
            )
        }
        composable(
            route = "recipe_detail/{recipeJson}",
            arguments = listOf(androidx.navigation.navArgument("recipeJson") { type = androidx.navigation.NavType.StringType })
        ) { backStackEntry ->
            val recipeJson = backStackEntry.arguments?.getString("recipeJson")
            android.util.Log.d("MainActivity", "Recipe JSON received: $recipeJson")
            val decodedJson = URLDecoder.decode(recipeJson, StandardCharsets.UTF_8.toString())
            val recipe = Gson().fromJson(decodedJson, com.smartkitch.app.data.model.Recipe::class.java)
            
            val recipeViewModel = androidx.hilt.navigation.compose.hiltViewModel<com.smartkitch.app.presentation.recipe.RecipeViewModel>()
            val savedRecipeTitles by recipeViewModel.savedRecipeTitles.collectAsState()
            val isSaved = savedRecipeTitles.contains(recipe.title)

            com.smartkitch.app.presentation.recipe.RecipeDetailScreen(
                recipe = recipe,
                isSaved = isSaved,
                onBackClick = { navController.popBackStack() },
                onSaveClick = { recipeToSave ->
                    recipeViewModel.saveRecipe(recipeToSave)
                },
                onStartCookingClick = {
                    val recipeJson = Gson().toJson(recipe)
                    val encodedJson = URLEncoder.encode(recipeJson, StandardCharsets.UTF_8.toString())
                    navController.navigate("cooking_mode/$encodedJson")
                }
            )
        }
        composable("settings") {
            com.smartkitch.app.presentation.settings.SettingsScreen(
                onEditProfile = { navController.navigate("profile_setup") },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToInfo = { type ->
                    if (type == "help_support") {
                        navController.navigate("help_support")
                    } else {
                        navController.navigate("info/$type")
                    }
                }
            )
        }
        composable("scan") {
            com.smartkitch.app.presentation.scan.ScanScreen(
                onScanComplete = { items ->
                    navController.popBackStack()
                },
                onBackClick = { navController.popBackStack() }
            )
        }
        composable("shopping_list") {
            com.smartkitch.app.presentation.shopping.ShoppingListScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "cooking_mode/{recipeJson}",
            arguments = listOf(androidx.navigation.navArgument("recipeJson") { type = androidx.navigation.NavType.StringType })
        ) { backStackEntry ->
            val recipeJson = backStackEntry.arguments?.getString("recipeJson")
            val decodedJson = URLDecoder.decode(recipeJson, StandardCharsets.UTF_8.toString())
            val recipe = Gson().fromJson(decodedJson, com.smartkitch.app.data.model.Recipe::class.java)

            val settingsViewModel = androidx.hilt.navigation.compose.hiltViewModel<com.smartkitch.app.presentation.settings.SettingsViewModel>()
            val voiceAssistantEnabled by settingsViewModel.voiceAssistant.collectAsState()

            com.smartkitch.app.presentation.recipe.CookingModeScreen(
                recipe = recipe,
                initialVoiceEnabled = voiceAssistantEnabled,
                onCloseClick = { navController.popBackStack() }
            )
        }
        composable("help_support") {
            com.smartkitch.app.presentation.settings.HelpSupportScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(
            route = "info/{type}",
            arguments = listOf(androidx.navigation.navArgument("type") { type = androidx.navigation.NavType.StringType })
        ) { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type") ?: return@composable
            val (title, content) = com.smartkitch.app.presentation.settings.InfoContent.getContent(type)
            com.smartkitch.app.presentation.settings.InfoScreen(
                title = title,
                content = content,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
