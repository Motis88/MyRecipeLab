package com.example.recipemanager.domain.usecase

import com.example.recipemanager.core.model.Recipe
import com.example.recipemanager.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow

class GetRecipeByIdUseCase(private val repository: RecipeRepository) {
    operator fun invoke(id: String): Flow<Recipe?> = repository.getRecipeById(id)
}
