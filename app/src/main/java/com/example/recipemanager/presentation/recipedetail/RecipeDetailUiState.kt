package com.example.recipemanager.presentation.recipedetail

import com.example.recipemanager.core.model.Recipe

sealed class RecipeDetailUiState {
    data object Loading : RecipeDetailUiState()
    data class Success(
        val recipe: Recipe,
        val showDeleteConfirm: Boolean = false
    ) : RecipeDetailUiState()
    data object NotFound : RecipeDetailUiState()
    data class Error(val message: String) : RecipeDetailUiState()
}
