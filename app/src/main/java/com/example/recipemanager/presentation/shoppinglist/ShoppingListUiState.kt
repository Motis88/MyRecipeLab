package com.example.recipemanager.presentation.shoppinglist

import com.example.recipemanager.core.model.GroceryCategory
import com.example.recipemanager.core.model.GroceryItem
import com.example.recipemanager.core.model.Recipe

data class ShoppingListUiState(
    val allRecipes: List<Recipe> = emptyList(),
    val selectedRecipeIds: Set<String> = emptySet(),
    /** Flat aggregated grocery list, sorted by category then name. */
    val groceryItems: List<GroceryItem> = emptyList(),
    /** Items grouped by [GroceryCategory] for section display. */
    val groceryByCategory: Map<GroceryCategory, List<GroceryItem>> = emptyMap(),
    val isLoading: Boolean = true
)
