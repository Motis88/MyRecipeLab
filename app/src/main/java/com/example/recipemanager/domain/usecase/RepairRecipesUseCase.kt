package com.example.recipemanager.domain.usecase

import com.example.recipemanager.core.model.Ingredient
import com.example.recipemanager.core.model.Step
import com.example.recipemanager.core.parser.RecipeParser
import com.example.recipemanager.core.util.IdGenerator
import com.example.recipemanager.domain.repository.RecipeRepository

/**
 * Scans all recipes that have no steps (everything was entered in ingredients),
 * re-parses the combined text with RecipeParser, and updates those where the parser
 * successfully separates ingredients from steps.
 *
 * Returns the number of recipes actually repaired.
 */
class RepairRecipesUseCase(
    private val repository: RecipeRepository,
    private val parser: RecipeParser = RecipeParser()
) {
    suspend operator fun invoke(): Int {
        val recipes = repository.getAllRecipesSync()
        var fixedCount = 0

        for (recipe in recipes) {
            // Only try to fix recipes that have ingredients but zero steps
            if (recipe.steps.isNotEmpty() || recipe.ingredients.isEmpty()) continue

            // Reconstruct raw text from title + ingredients
            val rawText = buildString {
                appendLine(recipe.title)
                appendLine()
                recipe.ingredients.forEach { appendLine(it.text) }
            }

            val result = parser.parse(rawText)

            // Only update if parser found both parts
            if (result.ingredients.isNotEmpty() && result.steps.isNotEmpty()) {
                val repairedRecipe = recipe.copy(
                    ingredients = result.ingredients.map { parsed ->
                        Ingredient(
                            id = IdGenerator.generate(),
                            text = parsed.text,
                            notes = emptyList()
                        )
                    },
                    steps = result.steps.map { parsed ->
                        Step(
                            id = IdGenerator.generate(),
                            text = parsed.text,
                            notes = emptyList()
                        )
                    },
                    updatedAt = System.currentTimeMillis()
                )
                repository.updateRecipe(repairedRecipe)
                fixedCount++
            }
        }

        return fixedCount
    }
}
