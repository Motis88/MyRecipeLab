package com.example.recipemanager.presentation.favorites

import com.example.recipemanager.core.model.Recipe
import com.example.recipemanager.presentation.common.SortOrder

data class FavoritesUiState(
    val recipes: List<Recipe> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val sortOrder: SortOrder = SortOrder.RECENT,
    val error: String? = null
)
