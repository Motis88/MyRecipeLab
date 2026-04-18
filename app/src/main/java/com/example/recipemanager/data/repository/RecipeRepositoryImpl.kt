package com.example.recipemanager.data.repository

import com.example.recipemanager.core.dispatchers.AppDispatchers
import com.example.recipemanager.core.model.Recipe
import com.example.recipemanager.core.util.SearchQueryHelper
import com.example.recipemanager.data.local.dao.RecipeDao
import com.example.recipemanager.data.local.dao.SearchDao
import com.example.recipemanager.data.local.mapper.RecipeMapper
import com.example.recipemanager.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

@OptIn(ExperimentalCoroutinesApi::class)
class RecipeRepositoryImpl(
    private val recipeDao: RecipeDao,
    private val searchDao: SearchDao,
    private val dispatchers: AppDispatchers
) : RecipeRepository {

    override fun getAllRecipes(): Flow<List<Recipe>> =
        recipeDao.getAllRecipes()
            .map { list -> list.map { RecipeMapper.toDomain(it) } }
            .flowOn(dispatchers.io)

    override fun getRecipeById(id: String): Flow<Recipe?> =
        recipeDao.getRecipeById(id)
            .map { it?.let { entity -> RecipeMapper.toDomain(entity) } }
            .flowOn(dispatchers.io)

    override fun getFavoriteRecipes(): Flow<List<Recipe>> =
        recipeDao.getFavoriteRecipes()
            .map { list -> list.map { RecipeMapper.toDomain(it) } }
            .flowOn(dispatchers.io)

    override fun getRecipesByCategory(category: String): Flow<List<Recipe>> =
        recipeDao.getRecipesByCategory(category)
            .map { list -> list.map { RecipeMapper.toDomain(it) } }
            .flowOn(dispatchers.io)

    override fun getAllRecipesSortedByTitle(): Flow<List<Recipe>> =
        recipeDao.getAllRecipesSortedByTitle()
            .map { list -> list.map { RecipeMapper.toDomain(it) } }
            .flowOn(dispatchers.io)

    override fun searchRecipes(query: String): Flow<List<Recipe>> {
        val escaped = SearchQueryHelper.escapeFtsQuery(query)
        if (escaped.isBlank()) return getAllRecipes()
        return searchDao.search(escaped)
            .flatMapLatest { ids ->
                if (ids.isEmpty()) flowOf(emptyList())
                else recipeDao.getRecipesByIds(ids)
                    .map { list -> list.map { RecipeMapper.toDomain(it) } }
            }
            .flowOn(dispatchers.io)
    }

    override suspend fun insertRecipe(recipe: Recipe) = withContext(dispatchers.io) {
        recipeDao.insertRecipeWithDetails(
            recipe = RecipeMapper.toEntity(recipe),
            ingredients = RecipeMapper.toIngredientEntities(recipe),
            steps = RecipeMapper.toStepEntities(recipe),
            recipeNotes = RecipeMapper.toRecipeNoteEntities(recipe),
            ingredientNotes = RecipeMapper.toIngredientNoteEntities(recipe),
            stepNotes = RecipeMapper.toStepNoteEntities(recipe)
        )
        searchDao.upsertSearchIndex(recipe.id, RecipeMapper.buildSearchableContent(recipe))
    }

    override suspend fun updateRecipe(recipe: Recipe) = withContext(dispatchers.io) {
        recipeDao.updateRecipeWithDetails(
            recipe = RecipeMapper.toEntity(recipe),
            ingredients = RecipeMapper.toIngredientEntities(recipe),
            steps = RecipeMapper.toStepEntities(recipe),
            recipeNotes = RecipeMapper.toRecipeNoteEntities(recipe),
            ingredientNotes = RecipeMapper.toIngredientNoteEntities(recipe),
            stepNotes = RecipeMapper.toStepNoteEntities(recipe)
        )
        searchDao.upsertSearchIndex(recipe.id, RecipeMapper.buildSearchableContent(recipe))
    }

    override suspend fun deleteRecipe(recipeId: String) = withContext(dispatchers.io) {
        searchDao.deleteSearchIndex(recipeId)
        recipeDao.deleteRecipe(recipeId)
    }

    override suspend fun toggleFavorite(recipeId: String, isFavorite: Boolean) =
        withContext(dispatchers.io) {
            recipeDao.updateFavoriteStatus(recipeId, isFavorite)
        }

    override suspend fun getAllRecipesSync(): List<Recipe> = withContext(dispatchers.io) {
        recipeDao.getAllRecipesWithDetailsSync().map { RecipeMapper.toDomain(it) }
    }
}
