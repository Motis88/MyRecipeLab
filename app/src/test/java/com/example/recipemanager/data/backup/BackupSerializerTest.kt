package com.example.recipemanager.data.backup

import com.example.recipemanager.core.model.Ingredient
import com.example.recipemanager.core.model.Language
import com.example.recipemanager.core.model.Recipe
import com.example.recipemanager.core.model.Step
import com.example.recipemanager.data.backup.BackupSerializer.toRecipeList
import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.SerializationException
import org.junit.Test

class BackupSerializerTest {

    private fun sampleRecipe(
        id: String = "r1",
        title: String = "Test Recipe"
    ): Recipe = Recipe(
        id = id,
        title = title,
        category = "Dessert",
        language = Language.EN,
        ingredients = listOf(
            Ingredient("i1", "2 cups flour", listOf("sift first")),
            Ingredient("i2", "1 cup sugar", emptyList())
        ),
        steps = listOf(
            Step("s1", "Mix ingredients", listOf("mix well")),
            Step("s2", "Bake for 30 min", emptyList())
        ),
        generalNotes = listOf("great for parties"),
        isFavorite = true,
        createdAt = 1000L,
        updatedAt = 2000L
    )

    @Test
    fun `serialize then deserialize roundtrip preserves data`() {
        val original = sampleRecipe()
        val json = BackupSerializer.serialize(listOf(original))
        val root = BackupSerializer.deserialize(json)
        val restored = root.toRecipeList()

        assertThat(restored).hasSize(1)
        val recipe = restored[0]
        assertThat(recipe.id).isEqualTo(original.id)
        assertThat(recipe.title).isEqualTo(original.title)
        assertThat(recipe.category).isEqualTo(original.category)
        assertThat(recipe.language).isEqualTo(original.language)
        assertThat(recipe.isFavorite).isEqualTo(original.isFavorite)
        assertThat(recipe.createdAt).isEqualTo(original.createdAt)
        assertThat(recipe.updatedAt).isEqualTo(original.updatedAt)
        assertThat(recipe.generalNotes).isEqualTo(original.generalNotes)
    }

    @Test
    fun `roundtrip preserves ingredients with notes`() {
        val original = sampleRecipe()
        val json = BackupSerializer.serialize(listOf(original))
        val restored = BackupSerializer.deserialize(json).toRecipeList()[0]

        assertThat(restored.ingredients).hasSize(2)
        assertThat(restored.ingredients[0].text).isEqualTo("2 cups flour")
        assertThat(restored.ingredients[0].notes).containsExactly("sift first")
        assertThat(restored.ingredients[1].notes).isEmpty()
    }

    @Test
    fun `roundtrip preserves steps with notes`() {
        val original = sampleRecipe()
        val json = BackupSerializer.serialize(listOf(original))
        val restored = BackupSerializer.deserialize(json).toRecipeList()[0]

        assertThat(restored.steps).hasSize(2)
        assertThat(restored.steps[0].notes).containsExactly("mix well")
    }

    @Test
    fun `serialize multiple recipes roundtrips correctly`() {
        val recipes = listOf(sampleRecipe("r1", "A"), sampleRecipe("r2", "B"))
        val json = BackupSerializer.serialize(recipes)
        val restored = BackupSerializer.deserialize(json).toRecipeList()

        assertThat(restored).hasSize(2)
        assertThat(restored.map { it.title }).containsExactly("A", "B")
    }

    @Test
    fun `serialize empty list roundtrips`() {
        val json = BackupSerializer.serialize(emptyList())
        val restored = BackupSerializer.deserialize(json).toRecipeList()
        assertThat(restored).isEmpty()
    }

    @Test
    fun `deserialized version is 1`() {
        val json = BackupSerializer.serialize(listOf(sampleRecipe()))
        val root = BackupSerializer.deserialize(json)
        assertThat(root.version).isEqualTo(1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `deserialize unsupported version throws`() {
        val json = """{"version":99,"recipes":[]}"""
        BackupSerializer.deserialize(json)
    }

    @Test(expected = SerializationException::class)
    fun `deserialize invalid JSON throws SerializationException`() {
        BackupSerializer.deserialize("not-json-at-all")
    }

    @Test
    fun `unknown language falls back to UNKNOWN`() {
        val json = """
        {
            "version": 1,
            "recipes": [{
                "id": "r1", "title": "T", "category": "", "language": "KLINGON",
                "ingredients": [], "steps": [], "generalNotes": [],
                "isFavorite": false, "createdAt": 0, "updatedAt": 0
            }]
        }
        """.trimIndent()
        val restored = BackupSerializer.deserialize(json).toRecipeList()
        assertThat(restored[0].language).isEqualTo(Language.UNKNOWN)
    }

    @Test
    fun `serialized JSON contains expected fields`() {
        val json = BackupSerializer.serialize(listOf(sampleRecipe()))
        assertThat(json).contains("\"version\"")
        assertThat(json).contains("\"recipes\"")
        assertThat(json).contains("\"title\"")
        assertThat(json).contains("Test Recipe")
    }
}
