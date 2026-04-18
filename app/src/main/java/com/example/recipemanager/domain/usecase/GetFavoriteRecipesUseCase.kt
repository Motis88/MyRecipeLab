package com.example.recipemanager.domain.usecase

import com.example.recipemanager.core.model.Recipe
import com.example.recipemanager.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow

class GetFavoriteRecipesUseCase(private val repository: RecipeRepository) {
    operator fun invoke(): Flow<List<Recipe>> = repository.getFavoriteRecipes()
}
