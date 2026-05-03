package com.example.recipemanager.data.local.mapper

import com.example.recipemanager.core.model.Ingredient
import com.example.recipemanager.core.model.Language
import com.example.recipemanager.core.model.Recipe
import com.example.recipemanager.core.model.Step
import com.example.recipemanager.data.local.entity.IngredientEntity
import com.example.recipemanager.data.local.entity.IngredientNoteEntity
import com.example.recipemanager.data.local.entity.RecipeEntity
import com.example.recipemanager.data.local.entity.RecipeNoteEntity
import com.example.recipemanager.data.local.entity.RecipeWithDetails
import com.example.recipemanager.data.local.entity.StepEntity
import com.example.recipemanager.data.local.entity.StepNoteEntity

object RecipeMapper {

    fun toDomain(entity: RecipeWithDetails): Recipe {
        return Recipe(
            id = entity.recipe.id,
            title = entity.recipe.title,
            category = entity.recipe.category,
            language = try {
                Language.valueOf(entity.recipe.language)
            } catch (_: IllegalArgumentException) {
                Language.UNKNOWN
            },
            ingredients = entity.ingredients
                .sortedBy { it.ingredient.orderIndex }
                .map { ingredientWithNotes ->
                    Ingredient(
                        id = ingredientWithNotes.ingredient.id,
                        text = ingredientWithNotes.ingredient.text,
                        notes = ingredientWithNotes.notes
                            .sortedBy { it.orderIndex }
                            .map { it.text }
                    )
                },
            steps = entity.steps
                .sortedBy { it.step.orderIndex }
                .map { stepWithNotes ->
                    Step(
                        id = stepWithNotes.step.id,
                        text = stepWithNotes.step.text,
                        notes = stepWithNotes.notes
                            .sortedBy { it.orderIndex }
                            .map { it.text }
                    )
                },
            generalNotes = entity.generalNotes
                .sortedBy { it.orderIndex }
                .map { it.text },
            isFavorite = entity.recipe.isFavorite,
            prepTimeMinutes = entity.recipe.prepTimeMinutes,
            cookTimeMinutes = entity.recipe.cookTimeMinutes,
            servings = entity.recipe.servings,
            createdAt = entity.recipe.createdAt,
            updatedAt = entity.recipe.updatedAt,
            imagePath = entity.recipe.imagePath
        )
    }

    fun toEntity(recipe: Recipe): RecipeEntity {
        return RecipeEntity(
            id = recipe.id,
            title = recipe.title,
            category = recipe.category,
            language = recipe.language.name,
            isFavorite = recipe.isFavorite,
            prepTimeMinutes = recipe.prepTimeMinutes,
            cookTimeMinutes = recipe.cookTimeMinutes,
            servings = recipe.servings,
            createdAt = recipe.createdAt,
            updatedAt = recipe.updatedAt,
            imagePath = recipe.imagePath
        )
    }

    fun toIngredientEntities(recipe: Recipe): List<IngredientEntity> {
        return recipe.ingredients.mapIndexed { index, ingredient ->
            IngredientEntity(
                id = ingredient.id,
                recipeId = recipe.id,
                text = ingredient.text,
                orderIndex = index
            )
        }
    }

    fun toStepEntities(recipe: Recipe): List<StepEntity> {
        return recipe.steps.mapIndexed { index, step ->
            StepEntity(
                id = step.id,
                recipeId = recipe.id,
                text = step.text,
                orderIndex = index
            )
        }
    }

    fun toRecipeNoteEntities(recipe: Recipe): List<RecipeNoteEntity> {
        return recipe.generalNotes.mapIndexed { index, note ->
            RecipeNoteEntity(
                id = "${recipe.id}_note_$index",
                recipeId = recipe.id,
                text = note,
                orderIndex = index
            )
        }
    }

    fun toIngredientNoteEntities(recipe: Recipe): List<IngredientNoteEntity> {
        return recipe.ingredients.flatMap { ingredient ->
            ingredient.notes.mapIndexed { index, note ->
                IngredientNoteEntity(
                    id = "${ingredient.id}_note_$index",
                    ingredientId = ingredient.id,
                    text = note,
                    orderIndex = index
                )
            }
        }
    }

    fun toStepNoteEntities(recipe: Recipe): List<StepNoteEntity> {
        return recipe.steps.flatMap { step ->
            step.notes.mapIndexed { index, note ->
                StepNoteEntity(
                    id = "${step.id}_note_$index",
                    stepId = step.id,
                    text = note,
                    orderIndex = index
                )
            }
        }
    }

    fun buildSearchableContent(recipe: Recipe): String {
        val parts = mutableListOf<String>()
        parts.add(recipe.title)
        recipe.ingredients.forEach { parts.add(it.text) }
        recipe.steps.forEach { parts.add(it.text) }
        return parts.joinToString(" ")
    }
}
