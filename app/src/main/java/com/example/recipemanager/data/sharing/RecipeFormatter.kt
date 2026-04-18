package com.example.recipemanager.data.sharing

import com.example.recipemanager.core.model.Recipe
import com.example.recipemanager.data.backup.BackupSerializer

object RecipeFormatter {

    fun formatAsText(recipe: Recipe): String {
        val sb = StringBuilder()
        sb.appendLine(recipe.title)

        if (recipe.category.isNotBlank()) {
            sb.appendLine()
            sb.appendLine("Category: ${recipe.category}")
        }

        val timeInfo = buildList {
            recipe.prepTimeMinutes?.let { add("Prep: ${it}min") }
            recipe.cookTimeMinutes?.let { add("Cook: ${it}min") }
        }
        if (timeInfo.isNotEmpty()) {
            sb.appendLine(timeInfo.joinToString(" | "))
        }
        if (recipe.servings > 1) {
            sb.appendLine("Servings: ${recipe.servings}")
        }

        if (recipe.ingredients.isNotEmpty()) {
            sb.appendLine()
            sb.appendLine("Ingredients:")
            recipe.ingredients.forEach { ingredient ->
                sb.appendLine("• ${ingredient.text}")
                ingredient.notes.forEach { note ->
                    sb.appendLine("  ↳ $note")
                }
            }
        }

        if (recipe.steps.isNotEmpty()) {
            sb.appendLine()
            sb.appendLine("Instructions:")
            recipe.steps.forEachIndexed { index, step ->
                sb.appendLine("${index + 1}. ${step.text}")
                step.notes.forEach { note ->
                    sb.appendLine("   ↳ $note")
                }
            }
        }

        if (recipe.generalNotes.isNotEmpty()) {
            sb.appendLine()
            sb.appendLine("Notes:")
            recipe.generalNotes.forEach { note ->
                sb.appendLine("• $note")
            }
        }

        return sb.toString().trimEnd()
    }

    fun formatAsJson(recipe: Recipe): String = BackupSerializer.serialize(listOf(recipe))
}
