package com.example.recipemanager.core.model

data class Recipe(
    val id: String,
    val title: String,
    val category: String,
    val language: Language,
    val ingredients: List<Ingredient>,
    val steps: List<Step>,
    val generalNotes: List<String>,
    val isFavorite: Boolean,
    val prepTimeMinutes: Int? = null,
    val cookTimeMinutes: Int? = null,
    val servings: Int = 1,
    val createdAt: Long,
    val updatedAt: Long,
    val imagePath: String? = null
)

data class Ingredient(
    val id: String,
    val text: String,
    val notes: List<String>
)

data class Step(
    val id: String,
    val text: String,
    val notes: List<String>
)
