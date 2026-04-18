package com.example.recipemanager.domain.usecase

import com.example.recipemanager.domain.repository.RecipeRepository

class ToggleFavoriteUseCase(private val repository: RecipeRepository) {
    suspend operator fun invoke(recipeId: String, isFavorite: Boolean) =
        repository.toggleFavorite(recipeId, isFavorite)
}
