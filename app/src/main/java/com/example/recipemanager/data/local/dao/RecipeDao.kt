package com.example.recipemanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.recipemanager.data.local.entity.IngredientEntity
import com.example.recipemanager.data.local.entity.IngredientNoteEntity
import com.example.recipemanager.data.local.entity.RecipeEntity
import com.example.recipemanager.data.local.entity.RecipeNoteEntity
import com.example.recipemanager.data.local.entity.RecipeWithDetails
import com.example.recipemanager.data.local.entity.StepEntity
import com.example.recipemanager.data.local.entity.StepNoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class RecipeDao {

    @Transaction
    @Query("SELECT * FROM recipes ORDER BY createdAt DESC")
    abstract fun getAllRecipes(): Flow<List<RecipeWithDetails>>

    @Transaction
    @Query("SELECT * FROM recipes WHERE id = :id")
    abstract fun getRecipeById(id: String): Flow<RecipeWithDetails?>

    @Transaction
    @Query("SELECT * FROM recipes WHERE isFavorite = 1 ORDER BY createdAt DESC")
    abstract fun getFavoriteRecipes(): Flow<List<RecipeWithDetails>>

    @Transaction
    @Query("SELECT * FROM recipes WHERE category = :category ORDER BY createdAt DESC")
    abstract fun getRecipesByCategory(category: String): Flow<List<RecipeWithDetails>>

    @Transaction
    @Query("SELECT * FROM recipes ORDER BY title COLLATE NOCASE ASC")
    abstract fun getAllRecipesSortedByTitle(): Flow<List<RecipeWithDetails>>

    @Transaction
    @Query("SELECT * FROM recipes WHERE id IN (:ids)")
    abstract fun getRecipesByIds(ids: List<String>): Flow<List<RecipeWithDetails>>

    @Transaction
    @Query("SELECT * FROM recipes")
    abstract suspend fun getAllRecipesWithDetailsSync(): List<RecipeWithDetails>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertRecipe(recipe: RecipeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertIngredients(ingredients: List<IngredientEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertSteps(steps: List<StepEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertRecipeNotes(notes: List<RecipeNoteEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertIngredientNotes(notes: List<IngredientNoteEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertStepNotes(notes: List<StepNoteEntity>)

    @Query("UPDATE recipes SET isFavorite = :isFavorite WHERE id = :recipeId")
    abstract suspend fun updateFavoriteStatus(recipeId: String, isFavorite: Boolean)

    @Query("DELETE FROM recipes WHERE id = :recipeId")
    abstract suspend fun deleteRecipe(recipeId: String)

    @Query("DELETE FROM ingredients WHERE recipeId = :recipeId")
    abstract suspend fun deleteIngredientsForRecipe(recipeId: String)

    @Query("DELETE FROM steps WHERE recipeId = :recipeId")
    abstract suspend fun deleteStepsForRecipe(recipeId: String)

    @Query("DELETE FROM recipe_notes WHERE recipeId = :recipeId")
    abstract suspend fun deleteRecipeNotesForRecipe(recipeId: String)

    @Query("DELETE FROM recipes")
    abstract suspend fun deleteAllRecipes()

    @Query("SELECT id FROM recipes WHERE id = :id")
    abstract suspend fun recipeExists(id: String): String?

    @Query("SELECT updatedAt FROM recipes WHERE id = :id")
    abstract suspend fun getRecipeUpdatedAt(id: String): Long?

    @Transaction
    open suspend fun insertRecipeWithDetails(
        recipe: RecipeEntity,
        ingredients: List<IngredientEntity>,
        steps: List<StepEntity>,
        recipeNotes: List<RecipeNoteEntity>,
        ingredientNotes: List<IngredientNoteEntity>,
        stepNotes: List<StepNoteEntity>
    ) {
        insertRecipe(recipe)
        insertIngredients(ingredients)
        insertSteps(steps)
        insertRecipeNotes(recipeNotes)
        insertIngredientNotes(ingredientNotes)
        insertStepNotes(stepNotes)
    }

    @Transaction
    open suspend fun updateRecipeWithDetails(
        recipe: RecipeEntity,
        ingredients: List<IngredientEntity>,
        steps: List<StepEntity>,
        recipeNotes: List<RecipeNoteEntity>,
        ingredientNotes: List<IngredientNoteEntity>,
        stepNotes: List<StepNoteEntity>
    ) {
        deleteIngredientsForRecipe(recipe.id)
        deleteStepsForRecipe(recipe.id)
        deleteRecipeNotesForRecipe(recipe.id)
        insertRecipe(recipe)
        insertIngredients(ingredients)
        insertSteps(steps)
        insertRecipeNotes(recipeNotes)
        insertIngredientNotes(ingredientNotes)
        insertStepNotes(stepNotes)
    }
}
