package com.example.recipemanager.domain.usecase

import com.example.recipemanager.core.model.Ingredient
import com.example.recipemanager.core.model.Step
import com.example.recipemanager.core.parser.RecipeParser
import com.example.recipemanager.core.util.IdGenerator
import com.example.recipemanager.domain.repository.RecipeRepository

/**
 * Scans ALL recipes and:
 * 1. Re-parses recipes with no steps (everything in ingredients) to separate them.
 * 2. Cleans up every recipe: removes empty items, strips orphaned numbers,
 *    normalises step numbering, trims whitespace — for visual symmetry.
 *
 * Returns the number of recipes that were actually changed.
 */
class RepairRecipesUseCase(
    private val repository: RecipeRepository,
    private val parser: RecipeParser = RecipeParser()
) {
    suspend operator fun invoke(): Int {
        val recipes = repository.getAllRecipesSync()
        var fixedCount = 0

        for (recipe in recipes) {
            var current = recipe

            // ── Pass 1: re-parse if there are no steps at all ──────────────────
            if (current.steps.isEmpty() && current.ingredients.isNotEmpty()) {
                val rawText = buildString {
                    appendLine(current.title)
                    appendLine()
                    current.ingredients.forEach { appendLine(it.text) }
                }
                val result = parser.parse(rawText)
                if (result.ingredients.isNotEmpty() && result.steps.isNotEmpty()) {
                    current = current.copy(
                        ingredients = result.ingredients.map { parsed ->
                            Ingredient(id = IdGenerator.generate(), text = parsed.text, notes = emptyList())
                        },
                        steps = result.steps.map { parsed ->
                            Step(id = IdGenerator.generate(), text = parsed.text, notes = emptyList())
                        }
                    )
                }
            }

            // ── Pass 2: clean up every recipe ───────────────────────────────────
            val cleanIngredients = current.ingredients
                .map { ing -> ing.copy(text = ing.text.trim()) }
                .filter { it.text.isNotBlank() && !isStandaloneNumber(it.text) }

            val cleanSteps = current.steps
                .map { step -> step.copy(text = stripLeadingNumber(step.text).trim()) }
                .filter { it.text.isNotBlank() && !isStandaloneNumber(it.text) }

            val changed = cleanIngredients != current.ingredients || cleanSteps != current.steps

            if (changed) {
                repository.updateRecipe(
                    current.copy(
                        ingredients = cleanIngredients,
                        steps = cleanSteps,
                        updatedAt = System.currentTimeMillis()
                    )
                )
                fixedCount++
            }
        }

        return fixedCount
    }

    /** Returns true if the text is just a number or number + punctuation, e.g. "1", "2.", "3:" */
    private fun isStandaloneNumber(text: String): Boolean =
        text.trim().matches(Regex("""^\d{1,3}[.:)]?$"""))

    /**
     * Removes a leading step number from text if present.
     * Handles patterns like "1.", "1:", "1)", "שלב 1:", "Step 1:", etc.
     */
    private fun stripLeadingNumber(text: String): String {
        val stripped = text
            // "1. text" / "1: text" / "1) text"
            .replace(Regex("""^\d{1,3}[.):\s]\s*"""), "")
            // "שלב 1:" / "Step 1:" (Hebrew/English)
            .replace(Regex("""^(?:שלב|step)\s+\d{1,3}[.:)]\s*""", RegexOption.IGNORE_CASE), "")
        return if (stripped.isBlank()) text else stripped
    }
}
