package com.example.recipemanager.data.backup

import kotlinx.serialization.Serializable

@Serializable
data class BackupRoot(
    val version: Int,
    val recipes: List<RecipeBackupDto>
)

@Serializable
data class RecipeBackupDto(
    val id: String,
    val title: String,
    val category: String,
    val language: String,
    val ingredients: List<IngredientBackupDto>,
    val steps: List<StepBackupDto>,
    val generalNotes: List<String>,
    val isFavorite: Boolean,
    val prepTimeMinutes: Int? = null,
    val cookTimeMinutes: Int? = null,
    val servings: Int = 1,
    val createdAt: Long,
    val updatedAt: Long
)

@Serializable
data class IngredientBackupDto(
    val id: String,
    val text: String,
    val notes: List<String>
)

@Serializable
data class StepBackupDto(
    val id: String,
    val text: String,
    val notes: List<String>
)
