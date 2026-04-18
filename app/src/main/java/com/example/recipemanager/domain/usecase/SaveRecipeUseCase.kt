package com.example.recipemanager.domain.usecase

import com.example.recipemanager.core.model.Recipe
import com.example.recipemanager.domain.repository.RecipeRepository

class SaveRecipeUseCase(private val repository: RecipeRepository) {
    suspend operator fun invoke(recipe: Recipe, isNew: Boolean) {
        if (isNew) repository.insertRecipe(recipe)
        else repository.updateRecipe(recipe)
    }
}
