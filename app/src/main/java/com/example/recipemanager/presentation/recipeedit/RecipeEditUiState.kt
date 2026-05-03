package com.example.recipemanager.presentation.recipeedit

import com.example.recipemanager.core.model.Language
import com.example.recipemanager.core.parser.ParserDiagnostics

enum class EditMode { ADD, EDIT }

sealed class SaveResult {
    data class Success(val recipeId: String) : SaveResult()
    data class Error(val message: String) : SaveResult()
}

data class EditableIngredient(
    val id: String,
    val text: String,
    val notes: List<String> = emptyList()
)

data class EditableStep(
    val id: String,
    val text: String,
    val notes: List<String> = emptyList()
)

data class RecipeEditUiState(
    val mode: EditMode = EditMode.ADD,
    val rawText: String = "",
    val isParsing: Boolean = false,
    val title: String = "",
    val category: String = "",
    val language: Language = Language.UNKNOWN,
    val ingredients: List<EditableIngredient> = emptyList(),
    val steps: List<EditableStep> = emptyList(),
    val generalNotes: List<String> = emptyList(),
    val isManualMode: Boolean = false,
    val showPasteField: Boolean = false,
    val confidence: Double = 1.0,
    val parserDiagnostics: ParserDiagnostics? = null,
    val isSaving: Boolean = false,
    val saveResult: SaveResult? = null,
    val availableCategories: List<String> = emptyList(),
    val validationError: String? = null,
    val isLoadingRecipe: Boolean = false,
    val prepTimeMinutes: Int? = null,
    val cookTimeMinutes: Int? = null,
    val servings: Int = 1,
    val isOcrProcessing: Boolean = false
)
