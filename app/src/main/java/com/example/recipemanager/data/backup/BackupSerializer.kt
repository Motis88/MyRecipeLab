package com.example.recipemanager.data.backup

import com.example.recipemanager.core.model.Ingredient
import com.example.recipemanager.core.model.Language
import com.example.recipemanager.core.model.Recipe
import com.example.recipemanager.core.model.Step
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object BackupSerializer {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    fun serialize(recipes: List<Recipe>): String {
        val root = BackupRoot(
            version = 1,
            recipes = recipes.map { it.toDto() }
        )
        return json.encodeToString(root)
    }

    /**
     * @throws SerializationException on malformed JSON
     * @throws IllegalArgumentException on unsupported schema version
     */
    fun deserialize(jsonString: String): BackupRoot {
        val root: BackupRoot = json.decodeFromString(jsonString)
        require(root.version == 1) {
            "Unsupported backup version: ${root.version}. Expected version 1."
        }
        return root
    }

    fun BackupRoot.toRecipeList(): List<Recipe> = recipes.map { it.toDomain() }

    private fun Recipe.toDto(): RecipeBackupDto = RecipeBackupDto(
        id = id,
        title = title,
        category = category,
        language = language.name,
        ingredients = ingredients.map {
            IngredientBackupDto(id = it.id, text = it.text, notes = it.notes)
        },
        steps = steps.map {
            StepBackupDto(id = it.id, text = it.text, notes = it.notes)
        },
        generalNotes = generalNotes,
        isFavorite = isFavorite,
        prepTimeMinutes = prepTimeMinutes,
        cookTimeMinutes = cookTimeMinutes,
        servings = servings,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun RecipeBackupDto.toDomain(): Recipe = Recipe(
        id = id,
        title = title,
        category = category,
        language = try { Language.valueOf(language) } catch (_: IllegalArgumentException) { Language.UNKNOWN },
        ingredients = ingredients.map { Ingredient(id = it.id, text = it.text, notes = it.notes) },
        steps = steps.map { Step(id = it.id, text = it.text, notes = it.notes) },
        generalNotes = generalNotes,
        isFavorite = isFavorite,
        prepTimeMinutes = prepTimeMinutes,
        cookTimeMinutes = cookTimeMinutes,
        servings = servings,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
