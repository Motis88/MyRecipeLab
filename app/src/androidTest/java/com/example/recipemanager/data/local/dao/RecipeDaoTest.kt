package com.example.recipemanager.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.recipemanager.data.local.db.AppDatabase
import com.example.recipemanager.data.local.entity.IngredientEntity
import com.example.recipemanager.data.local.entity.RecipeEntity
import com.example.recipemanager.data.local.entity.RecipeNoteEntity
import com.example.recipemanager.data.local.entity.StepEntity
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecipeDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var recipeDao: RecipeDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        recipeDao = db.recipeDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun testRecipeEntity(
        id: String = "r1",
        title: String = "Test Recipe",
        category: String = "Dessert",
        isFavorite: Boolean = false
    ) = RecipeEntity(
        id = id,
        title = title,
        category = category,
        language = "EN",
        isFavorite = isFavorite,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )

    private fun testIngredient(recipeId: String, index: Int) = IngredientEntity(
        id = "${recipeId}_ing_$index",
        recipeId = recipeId,
        text = "Ingredient $index",
        orderIndex = index
    )

    private fun testStep(recipeId: String, index: Int) = StepEntity(
        id = "${recipeId}_step_$index",
        recipeId = recipeId,
        text = "Step $index",
        orderIndex = index
    )

    // ── Insert & Query ──────────────────────────────────────────────

    @Test
    fun insertAndRetrieveRecipe() = runTest {
        val entity = testRecipeEntity()
        recipeDao.insertRecipeWithDetails(
            entity,
            listOf(testIngredient("r1", 0)),
            listOf(testStep("r1", 0)),
            emptyList(), emptyList(), emptyList()
        )

        val recipes = recipeDao.getAllRecipes().first()
        assertThat(recipes).hasSize(1)
        assertThat(recipes[0].recipe.title).isEqualTo("Test Recipe")
        assertThat(recipes[0].ingredients).hasSize(1)
        assertThat(recipes[0].steps).hasSize(1)
    }

    @Test
    fun getRecipeByIdReturnsCorrectRecipe() = runTest {
        recipeDao.insertRecipe(testRecipeEntity("r1", "Recipe A"))
        recipeDao.insertRecipe(testRecipeEntity("r2", "Recipe B"))

        val result = recipeDao.getRecipeById("r2").first()
        assertThat(result).isNotNull()
        assertThat(result!!.recipe.title).isEqualTo("Recipe B")
    }

    @Test
    fun getRecipeByIdReturnsNullForMissing() = runTest {
        val result = recipeDao.getRecipeById("nonexistent").first()
        assertThat(result).isNull()
    }

    // ── Favorites ───────────────────────────────────────────────────

    @Test
    fun updateFavoriteStatus() = runTest {
        recipeDao.insertRecipe(testRecipeEntity("r1", isFavorite = false))

        recipeDao.updateFavoriteStatus("r1", true)

        val recipe = recipeDao.getRecipeById("r1").first()
        assertThat(recipe!!.recipe.isFavorite).isTrue()
    }

    @Test
    fun getFavoriteRecipesFiltersCorrectly() = runTest {
        recipeDao.insertRecipe(testRecipeEntity("r1", "Fav", isFavorite = true))
        recipeDao.insertRecipe(testRecipeEntity("r2", "NotFav", isFavorite = false))

        val favorites = recipeDao.getFavoriteRecipes().first()
        assertThat(favorites).hasSize(1)
        assertThat(favorites[0].recipe.title).isEqualTo("Fav")
    }

    // ── Delete ──────────────────────────────────────────────────────

    @Test
    fun deleteRecipeRemovesIt() = runTest {
        recipeDao.insertRecipe(testRecipeEntity("r1"))
        recipeDao.deleteRecipe("r1")

        val recipes = recipeDao.getAllRecipes().first()
        assertThat(recipes).isEmpty()
    }

    @Test
    fun deleteAllRecipesClearsTable() = runTest {
        recipeDao.insertRecipe(testRecipeEntity("r1"))
        recipeDao.insertRecipe(testRecipeEntity("r2"))
        recipeDao.deleteAllRecipes()

        val recipes = recipeDao.getAllRecipes().first()
        assertThat(recipes).isEmpty()
    }

    // ── Exists & updatedAt ──────────────────────────────────────────

    @Test
    fun recipeExistsReturnsIdWhenPresent() = runTest {
        recipeDao.insertRecipe(testRecipeEntity("r1"))
        assertThat(recipeDao.recipeExists("r1")).isEqualTo("r1")
    }

    @Test
    fun recipeExistsReturnsNullWhenMissing() = runTest {
        assertThat(recipeDao.recipeExists("r1")).isNull()
    }

    @Test
    fun getRecipeUpdatedAtReturnsTimestamp() = runTest {
        val entity = testRecipeEntity("r1")
        recipeDao.insertRecipe(entity)
        assertThat(recipeDao.getRecipeUpdatedAt("r1")).isEqualTo(entity.updatedAt)
    }

    // ── Update ──────────────────────────────────────────────────────

    @Test
    fun updateRecipeWithDetailsReplacesChildren() = runTest {
        recipeDao.insertRecipeWithDetails(
            testRecipeEntity("r1"),
            listOf(testIngredient("r1", 0), testIngredient("r1", 1)),
            listOf(testStep("r1", 0)),
            emptyList(), emptyList(), emptyList()
        )

        // Update with fewer ingredients
        recipeDao.updateRecipeWithDetails(
            testRecipeEntity("r1", "Updated"),
            listOf(testIngredient("r1", 0)),
            listOf(testStep("r1", 0), testStep("r1", 1)),
            emptyList(), emptyList(), emptyList()
        )

        val recipe = recipeDao.getRecipeById("r1").first()!!
        assertThat(recipe.recipe.title).isEqualTo("Updated")
        assertThat(recipe.ingredients).hasSize(1)
        assertThat(recipe.steps).hasSize(2)
    }

    // ── Batch query ─────────────────────────────────────────────────

    @Test
    fun getRecipesByIdsReturnsMatchingRecipes() = runTest {
        recipeDao.insertRecipe(testRecipeEntity("r1", "A"))
        recipeDao.insertRecipe(testRecipeEntity("r2", "B"))
        recipeDao.insertRecipe(testRecipeEntity("r3", "C"))

        val result = recipeDao.getRecipesByIds(listOf("r1", "r3")).first()
        assertThat(result.map { it.recipe.id }).containsExactly("r1", "r3")
    }

    @Test
    fun getAllRecipesWithDetailsSyncReturnsAll() = runTest {
        recipeDao.insertRecipe(testRecipeEntity("r1"))
        recipeDao.insertRecipe(testRecipeEntity("r2"))

        val result = recipeDao.getAllRecipesWithDetailsSync()
        assertThat(result).hasSize(2)
    }

    // ── General notes ───────────────────────────────────────────────

    @Test
    fun recipeNotesArePreserved() = runTest {
        val notes = listOf(
            RecipeNoteEntity("n1", "r1", "First note", 0),
            RecipeNoteEntity("n2", "r1", "Second note", 1)
        )
        recipeDao.insertRecipeWithDetails(
            testRecipeEntity("r1"), emptyList(), emptyList(),
            notes, emptyList(), emptyList()
        )

        val recipe = recipeDao.getRecipeById("r1").first()!!
        assertThat(recipe.generalNotes).hasSize(2)
        assertThat(recipe.generalNotes.map { it.text }).containsExactly("First note", "Second note")
    }
}
