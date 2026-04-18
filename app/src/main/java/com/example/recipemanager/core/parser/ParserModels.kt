package com.example.recipemanager.core.parser

import com.example.recipemanager.core.model.Language

data class ParserResult(
    val title: String,
    val ingredients: List<ParsedIngredient>,
    val steps: List<ParsedStep>,
    val language: Language,
    val suggestedCategory: String,
    val confidence: Double,
    val diagnostics: ParserDiagnostics,
    val rawInput: String
)

data class ParsedIngredient(
    val text: String
)

data class ParsedStep(
    val text: String
)

data class ParserDiagnostics(
    val titleDetected: Boolean,
    val ingredientCount: Int,
    val stepCount: Int,
    val unclassifiedLines: List<String>,
    val fallbackReasons: List<String>,
    val lineClassifications: List<LineClassification>
)

data class LineClassification(
    val lineNumber: Int,
    val text: String,
    val type: LineType,
    val confidence: Double
)

enum class LineType {
    TITLE,
    INGREDIENT,
    STEP,
    EMPTY,
    UNCLASSIFIED,
    SECTION_HEADER
}
