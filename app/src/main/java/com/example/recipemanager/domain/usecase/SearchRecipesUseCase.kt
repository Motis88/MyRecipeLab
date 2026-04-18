package com.example.recipemanager.domain.usecase

import com.example.recipemanager.core.model.Recipe
import com.example.recipemanager.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow

class SearchRecipesUseCase(private val repository: RecipeRepository) {
    operator fun invoke(query: String): Flow<List<Recipe>> =
        if (query.isBlank()) repository.getAllRecipes()
        else repository.searchRecipes(query)
}
