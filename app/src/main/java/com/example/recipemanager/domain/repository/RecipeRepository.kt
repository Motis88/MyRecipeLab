package com.example.recipemanager.domain.repository

import com.example.recipemanager.core.model.Recipe
import kotlinx.coroutines.flow.Flow

interface RecipeRepository {
    fun getAllRecipes(): Flow<List<Recipe>>
    fun getRecipeById(id: String): Flow<Recipe?>
    fun getFavoriteRecipes(): Flow<List<Recipe>>
    fun getRecipesByCategory(category: String): Flow<List<Recipe>>
    fun getAllRecipesSortedByTitle(): Flow<List<Recipe>>
    fun searchRecipes(query: String): Flow<List<Recipe>>
    suspend fun insertRecipe(recipe: Recipe)
    suspend fun updateRecipe(recipe: Recipe)
    suspend fun deleteRecipe(recipeId: String)
    suspend fun toggleFavorite(recipeId: String, isFavorite: Boolean)
    suspend fun getAllRecipesSync(): List<Recipe>
}
