package com.example.recipemanager.presentation.recipelist

import com.example.recipemanager.core.model.Recipe
import com.example.recipemanager.presentation.common.RecipeFilter

data class RecipeListUiState(
    val recipes: List<Recipe> = emptyList(),
    val isLoading: Boolean = false,
    val filter: RecipeFilter = RecipeFilter(),
    val availableCategories: List<String> = emptyList(),
    val error: String? = null
)
