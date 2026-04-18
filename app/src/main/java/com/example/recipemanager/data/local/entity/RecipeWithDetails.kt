package com.example.recipemanager.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class IngredientWithNotes(
    @Embedded val ingredient: IngredientEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "ingredientId"
    )
    val notes: List<IngredientNoteEntity>
)

data class StepWithNotes(
    @Embedded val step: StepEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "stepId"
    )
    val notes: List<StepNoteEntity>
)

data class RecipeWithDetails(
    @Embedded val recipe: RecipeEntity,
    @Relation(
        entity = IngredientEntity::class,
        parentColumn = "id",
        entityColumn = "recipeId"
    )
    val ingredients: List<IngredientWithNotes>,
    @Relation(
        entity = StepEntity::class,
        parentColumn = "id",
        entityColumn = "recipeId"
    )
    val steps: List<StepWithNotes>,
    @Relation(
        parentColumn = "id",
        entityColumn = "recipeId"
    )
    val generalNotes: List<RecipeNoteEntity>
)
