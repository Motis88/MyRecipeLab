package com.example.recipemanager.data.repository

import com.example.recipemanager.core.dispatchers.AppDispatchers
import com.example.recipemanager.data.backup.BackupSerializer
import com.example.recipemanager.data.backup.BackupSerializer.toRecipeList
import com.example.recipemanager.data.local.dao.RecipeDao
import com.example.recipemanager.data.local.dao.SearchDao
import com.example.recipemanager.data.local.mapper.RecipeMapper
import com.example.recipemanager.domain.repository.BackupRepository
import com.example.recipemanager.domain.repository.ImportResult
import com.example.recipemanager.domain.repository.ImportStrategy
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException

class BackupRepositoryImpl(
    private val recipeDao: RecipeDao,
    private val searchDao: SearchDao,
    private val dispatchers: AppDispatchers
) : BackupRepository {

    override suspend fun exportAllRecipes(): String = withContext(dispatchers.io) {
        val entities = recipeDao.getAllRecipesWithDetailsSync()
        val recipes = entities.map { RecipeMapper.toDomain(it) }
        BackupSerializer.serialize(recipes)
    }

    override suspend fun importRecipes(json: String, strategy: ImportStrategy): ImportResult =
        withContext(dispatchers.io) {
            try {
                val root = BackupSerializer.deserialize(json)
                val recipesToImport = root.toRecipeList()

                when (strategy) {
                    ImportStrategy.OVERWRITE -> {
                        recipeDao.deleteAllRecipes()
                        searchDao.clearSearchIndex()
                        recipesToImport.forEach { recipe ->
                            recipeDao.insertRecipeWithDetails(
                                recipe = RecipeMapper.toEntity(recipe),
                                ingredients = RecipeMapper.toIngredientEntities(recipe),
                                steps = RecipeMapper.toStepEntities(recipe),
                                recipeNotes = RecipeMapper.toRecipeNoteEntities(recipe),
                                ingredientNotes = RecipeMapper.toIngredientNoteEntities(recipe),
                                stepNotes = RecipeMapper.toStepNoteEntities(recipe)
                            )
                            searchDao.upsertSearchIndex(
                                recipe.id,
                                RecipeMapper.buildSearchableContent(recipe)
                            )
                        }
                        ImportResult.Success(recipesToImport.size)
                    }

                    ImportStrategy.MERGE -> {
                        var importedCount = 0
                        recipesToImport.forEach { importedRecipe ->
                            val existingUpdatedAt = recipeDao.getRecipeUpdatedAt(importedRecipe.id)
                            if (existingUpdatedAt == null) {
                                // No conflict — insert new
                                recipeDao.insertRecipeWithDetails(
                                    recipe = RecipeMapper.toEntity(importedRecipe),
                                    ingredients = RecipeMapper.toIngredientEntities(importedRecipe),
                                    steps = RecipeMapper.toStepEntities(importedRecipe),
                                    recipeNotes = RecipeMapper.toRecipeNoteEntities(importedRecipe),
                                    ingredientNotes = RecipeMapper.toIngredientNoteEntities(importedRecipe),
                                    stepNotes = RecipeMapper.toStepNoteEntities(importedRecipe)
                                )
                                searchDao.upsertSearchIndex(
                                    importedRecipe.id,
                                    RecipeMapper.buildSearchableContent(importedRecipe)
                                )
                                importedCount++
                            } else {
                                // Conflict: explicit conflict strategy — imported wins if newer
                                if (importedRecipe.updatedAt > existingUpdatedAt) {
                                    recipeDao.updateRecipeWithDetails(
                                        recipe = RecipeMapper.toEntity(importedRecipe),
                                        ingredients = RecipeMapper.toIngredientEntities(importedRecipe),
                                        steps = RecipeMapper.toStepEntities(importedRecipe),
                                        recipeNotes = RecipeMapper.toRecipeNoteEntities(importedRecipe),
                                        ingredientNotes = RecipeMapper.toIngredientNoteEntities(importedRecipe),
                                        stepNotes = RecipeMapper.toStepNoteEntities(importedRecipe)
                                    )
                                    searchDao.upsertSearchIndex(
                                        importedRecipe.id,
                                        RecipeMapper.buildSearchableContent(importedRecipe)
                                    )
                                    importedCount++
                                }
                                // else: keep existing (it is newer)
                            }
                        }
                        ImportResult.Success(importedCount)
                    }
                }
            } catch (e: SerializationException) {
                ImportResult.Error("Invalid JSON: ${e.message}")
            } catch (e: IllegalArgumentException) {
                ImportResult.Error(e.message ?: "Unsupported backup format")
            } catch (e: Exception) {
                ImportResult.Error("Import failed: ${e.message}")
            }
        }
}
