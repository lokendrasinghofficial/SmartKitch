package com.smartkitch.app.presentation.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smartkitch.app.data.model.FoodItem
import com.smartkitch.app.ui.theme.SageGreen
import java.util.Locale

@Composable
fun CircularStorageIndicator(
    title: String,
    count: Int,
    modifier: Modifier = Modifier,
    color: Color = SageGreen,
    onClick: () -> Unit
) {
    val animatedCount = androidx.compose.runtime.remember { androidx.compose.animation.core.Animatable(0f) }
    
    androidx.compose.runtime.LaunchedEffect(count) {
        animatedCount.animateTo(
            targetValue = count.toFloat(),
            animationSpec = androidx.compose.animation.core.tween(
                durationMillis = 1000,
                easing = androidx.compose.animation.core.FastOutSlowInEasing
            )
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(80.dp)) {
                drawCircle(
                    color = Color.LightGray.copy(alpha = 0.3f),
                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                )
                // Draw a full circle for now as "fullness" is just item count
                drawCircle(
                    color = color,
                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = animatedCount.value.toInt().toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = "Items",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ExpiringItemCard(item: FoodItem) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(200.dp), // Increased height for better image visibility
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Image or Icon Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(SageGreen.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                // 1. Show Emoji as Placeholder/Fallback (Always visible behind)
                Text(
                    text = getEmojiForItem(item.name, item.category),
                    style = MaterialTheme.typography.displayMedium,
                    modifier = Modifier.padding(8.dp)
                )

                // 2. Overlay AI Image on top
                val imageUrl = if (!item.imageUrl.isNullOrEmpty()) {
                    item.imageUrl
                } else {
                    "https://image.pollinations.ai/prompt/${item.name}%20food%20ingredient%20minimalist%20style%20white%20background?width=320&height=200&nologo=true"
                }

                coil.compose.AsyncImage(
                    model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true)
                        .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                        .build(),
                    contentDescription = item.name,
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${item.quantity} ${item.unit}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                val daysUntilExpiry = ((item.expiryDate - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).toInt()
                val expiryText = if (daysUntilExpiry < 0) "Expired" else if (daysUntilExpiry == 0) "Today" else "$daysUntilExpiry days"
                val expiryColor = if (daysUntilExpiry < 3) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (daysUntilExpiry < 3) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = expiryText,
                        style = MaterialTheme.typography.labelSmall,
                        color = expiryColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

fun getEmojiForItem(name: String, category: String): String {
    val lowerName = name.lowercase(Locale.getDefault())
    
    // Specific name matching
    if (lowerName.contains("onion")) return "🧅"
    if (lowerName.contains("garlic")) return "🧄"
    if (lowerName.contains("potato")) return "🥔"
    if (lowerName.contains("carrot")) return "🥕"
    if (lowerName.contains("tomato")) return "🍅"
    if (lowerName.contains("cucumber")) return "🥒"
    if (lowerName.contains("lettuce") || lowerName.contains("salad")) return "🥗"
    if (lowerName.contains("broccoli")) return "🥦"
    if (lowerName.contains("corn")) return "🌽"
    if (lowerName.contains("egg")) return "🥚"
    if (lowerName.contains("milk")) return "🥛"
    if (lowerName.contains("cheese")) return "🧀"
    if (lowerName.contains("butter")) return "🧈"
    if (lowerName.contains("bread")) return "🍞"
    if (lowerName.contains("rice")) return "🍚"
    if (lowerName.contains("pasta") || lowerName.contains("spaghetti")) return "🍝"
    if (lowerName.contains("chicken")) return "🍗"
    if (lowerName.contains("beef") || lowerName.contains("steak")) return "🥩"
    if (lowerName.contains("pork") || lowerName.contains("bacon")) return "🥓"
    if (lowerName.contains("fish") || lowerName.contains("salmon")) return "🐟"
    if (lowerName.contains("apple")) return "🍎"
    if (lowerName.contains("banana")) return "🍌"
    if (lowerName.contains("orange")) return "🍊"
    if (lowerName.contains("grape")) return "🍇"
    if (lowerName.contains("strawberry")) return "🍓"
    if (lowerName.contains("watermelon")) return "🍉"
    if (lowerName.contains("lemon")) return "🍋"
    if (lowerName.contains("coffee")) return "☕"
    if (lowerName.contains("tea")) return "🍵"
    if (lowerName.contains("juice")) return "🧃"
    if (lowerName.contains("water")) return "💧"
    if (lowerName.contains("soda") || lowerName.contains("coke")) return "🥤"
    if (lowerName.contains("beer")) return "🍺"
    if (lowerName.contains("wine")) return "🍷"
    if (lowerName.contains("cake")) return "🍰"
    if (lowerName.contains("cookie")) return "🍪"
    if (lowerName.contains("chocolate")) return "🍫"
    if (lowerName.contains("ice cream")) return "🍦"
    if (lowerName.contains("pizza")) return "🍕"
    if (lowerName.contains("burger")) return "🍔"
    if (lowerName.contains("fries")) return "🍟"

    // Category fallback
    return when (category.lowercase(Locale.getDefault())) {
        "fruit", "fruits" -> "🍎"
        "vegetable", "vegetables" -> "🥦"
        "dairy" -> "🥛"
        "meat" -> "🥩"
        "fish", "seafood" -> "🐟"
        "pantry", "grains" -> "🍝"
        "frozen" -> "❄️"
        "drink", "drinks" -> "🥤"
        "snack", "snacks" -> "🍪"
        else -> "🍽️"
    }
}

@Composable
fun QuickActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        modifier = Modifier.width(100.dp).height(80.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = SageGreen,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}

@Composable
fun WasteSaverCard(
    recipe: com.smartkitch.app.data.model.Recipe,
    expiringItems: List<String>,
    isSaved: Boolean,
    onViewRecipe: () -> Unit,
    onGenerateAnother: () -> Unit,
    onSaveClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Save your food",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SageGreen
                )
                Row {
                    Icon(
                        imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Save Recipe",
                        tint = if (isSaved) SageGreen else Color.Gray,
                        modifier = Modifier
                            .clickable { onSaveClick() }
                            .padding(end = 16.dp)
                    )
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Generate Another",
                        tint = SageGreen,
                        modifier = Modifier.clickable { onGenerateAnother() }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = recipe.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "Uses: ${expiringItems.joinToString(", ")}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "⏱️ ${recipe.cookingTime}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "👥 ${recipe.servingSize}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onViewRecipe,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SageGreen
                )
            ) {
                Text("View Full Recipe")
            }
        }
    }
}
