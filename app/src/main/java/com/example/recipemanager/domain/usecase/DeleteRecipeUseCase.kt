package com.example.recipemanager.domain.usecase

import com.example.recipemanager.domain.repository.RecipeRepository

class DeleteRecipeUseCase(private val repository: RecipeRepository) {
    suspend operator fun invoke(recipeId: String) = repository.deleteRecipe(recipeId)
}
