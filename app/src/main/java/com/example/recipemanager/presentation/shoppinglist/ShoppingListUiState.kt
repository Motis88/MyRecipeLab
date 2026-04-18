package com.example.recipemanager.presentation.shoppinglist

import com.example.recipemanager.core.model.Recipe

data class ShoppingListUiState(
    val allRecipes: List<Recipe> = emptyList(),
    val selectedRecipeIds: Set<String> = emptySet(),
    val shoppingItems: List<String> = emptyList(),
    val isLoading: Boolean = true
)
