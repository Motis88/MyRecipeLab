package com.example.recipemanager.core.parser

import com.example.recipemanager.core.model.Language

class RecipeParser(
    private val languageDetector: LanguageDetector = LanguageDetector(),
    private val heuristics: ParserHeuristics = ParserHeuristics(),
    private val categoryDetector: CategoryDetector = CategoryDetector()
) {

    fun parse(input: String): ParserResult {
        if (input.isBlank()) {
            return createEmptyResult(input)
        }

        val language = languageDetector.detect(input)
        val lines = input.lines()

        if (language == Language.UNKNOWN) {
            return createUnknownLanguageResult(lines, input)
        }

        val nonEmptyLines = lines.filter { it.isNotBlank() }
        if (nonEmptyLines.isEmpty()) {
            return createEmptyResult(input)
        }

        val titleLineIndex = lines.indexOfFirst { it.isNotBlank() }
        val title = lines[titleLineIndex].trim()
        val remainingLines = lines.subList(titleLineIndex + 1, lines.size)

        val sectionResult = trySectionBasedParsing(remainingLines, language)
        val classifications = sectionResult ?: classifyLinesIndividually(remainingLines, language)

        val ingredients = classifications
            .filter { it.type == LineType.INGREDIENT }
            .map { ParsedIngredient(it.text.trim()) }

        val steps = classifications
            .filter { it.type == LineType.STEP }
            .map { ParsedStep(it.text.trim()) }

        val unclassifiedLines = classifications
            .filter { it.type == LineType.UNCLASSIFIED }
            .map { it.text }

        val allTexts = listOf(title) + ingredients.map { it.text } + steps.map { it.text }
        val suggestedCategory = categoryDetector.detect(allTexts, language)

        val totalContentLines = remainingLines.count { it.isNotBlank() }
        val confidence = calculateConfidence(
            titleDetected = true,
            ingredientCount = ingredients.size,
            stepCount = steps.size,
            unclassifiedCount = unclassifiedLines.size,
            totalContentLines = totalContentLines,
            usedSectionHeaders = sectionResult != null
        )

        val fallbackReasons = mutableListOf<String>()
        if (ingredients.isEmpty()) fallbackReasons.add("No ingredients detected")
        if (steps.isEmpty()) fallbackReasons.add("No steps detected")
        if (sectionResult == null && totalContentLines > 0) {
            fallbackReasons.add("No section headers found; used line-by-line classification")
        }

        val diagnostics = ParserDiagnostics(
            titleDetected = true,
            ingredientCount = ingredients.size,
            stepCount = steps.size,
            unclassifiedLines = unclassifiedLines,
            fallbackReasons = fallbackReasons,
            lineClassifications = classifications
        )

        return ParserResult(
            title = title,
            ingredients = ingredients,
            steps = steps,
            language = language,
            suggestedCategory = suggestedCategory,
            confidence = confidence,
            diagnostics = diagnostics,
            rawInput = input
        )
    }

    private fun trySectionBasedParsing(
        lines: List<String>,
        language: Language
    ): List<LineClassification>? {
        val classifications = mutableListOf<LineClassification>()
        var currentSection: LineType? = null
        var foundAnyHeader = false

        for ((index, line) in lines.withIndex()) {
            if (line.isBlank()) {
                classifications.add(LineClassification(index, line, LineType.EMPTY, 1.0))
                continue
            }

            val headerType = heuristics.detectSectionHeader(line, language)
            if (headerType != null) {
                currentSection = headerType
                foundAnyHeader = true
                classifications.add(LineClassification(index, line, LineType.SECTION_HEADER, 1.0))
                continue
            }

            if (currentSection != null) {
                classifications.add(LineClassification(index, line, currentSection, 0.9))
            } else {
                classifications.add(LineClassification(index, line, LineType.UNCLASSIFIED, 0.0))
            }
        }

        return if (foundAnyHeader) classifications else null
    }

    private fun classifyLinesIndividually(
        lines: List<String>,
        language: Language
    ): List<LineClassification> {
        // First pass: classify each line independently
        val initial = lines.mapIndexed { index, line ->
            if (line.isBlank()) {
                return@mapIndexed LineClassification(index, line, LineType.EMPTY, 1.0)
            }

            val ingredientConf = heuristics.ingredientConfidence(line, language)
            val stepConf = heuristics.stepConfidence(line, language)

            when {
                ingredientConf > stepConf && ingredientConf > 0.3 ->
                    LineClassification(index, line, LineType.INGREDIENT, ingredientConf)
                stepConf > ingredientConf && stepConf > 0.3 ->
                    LineClassification(index, line, LineType.STEP, stepConf)
                ingredientConf == stepConf && ingredientConf > 0.3 ->
                    LineClassification(index, line, LineType.INGREDIENT, ingredientConf)
                else ->
                    LineClassification(index, line, LineType.UNCLASSIFIED, 0.0)
            }
        }

        // Second pass: propagate context — if a line is UNCLASSIFIED but surrounded by
        // lines of the same type, classify it with reduced confidence
        return initial.mapIndexed { index, classification ->
            if (classification.type != LineType.UNCLASSIFIED) return@mapIndexed classification

            val prevType = initial.getOrNull(index - 1)?.type
            val nextType = initial.getOrNull(index + 1)?.type

            // If both neighbors are the same type, adopt that type
            if (prevType != null && prevType == nextType &&
                prevType != LineType.EMPTY && prevType != LineType.UNCLASSIFIED
            ) {
                return@mapIndexed classification.copy(type = prevType, confidence = 0.35)
            }

            // If previous neighbor is classified and line is short, likely same section
            if (prevType == LineType.INGREDIENT && classification.text.trim().length < 50) {
                return@mapIndexed classification.copy(type = LineType.INGREDIENT, confidence = 0.35)
            }

            classification
        }
    }

    private fun calculateConfidence(
        titleDetected: Boolean,
        ingredientCount: Int,
        stepCount: Int,
        @Suppress("UNUSED_PARAMETER") unclassifiedCount: Int,
        totalContentLines: Int,
        usedSectionHeaders: Boolean
    ): Double {
        var score = 0.0

        if (titleDetected) score += 0.15
        if (ingredientCount > 0) score += 0.2
        if (stepCount > 0) score += 0.2
        if (ingredientCount > 0 && stepCount > 0) score += 0.15

        if (totalContentLines > 0) {
            val classifiedCount = ingredientCount + stepCount
            val ratio = classifiedCount.toDouble() / totalContentLines
            score += ratio * 0.2
        }

        if (usedSectionHeaders) score += 0.1

        return score.coerceIn(0.0, 1.0)
    }

    private fun createEmptyResult(rawInput: String): ParserResult {
        return ParserResult(
            title = "",
            ingredients = emptyList(),
            steps = emptyList(),
            language = Language.UNKNOWN,
            suggestedCategory = "",
            confidence = 0.0,
            diagnostics = ParserDiagnostics(
                titleDetected = false,
                ingredientCount = 0,
                stepCount = 0,
                unclassifiedLines = emptyList(),
                fallbackReasons = listOf("Empty input"),
                lineClassifications = emptyList()
            ),
            rawInput = rawInput
        )
    }

    private fun createUnknownLanguageResult(
        lines: List<String>,
        rawInput: String
    ): ParserResult {
        val title = lines.firstOrNull { it.isNotBlank() }?.trim() ?: ""
        return ParserResult(
            title = title,
            ingredients = emptyList(),
            steps = emptyList(),
            language = Language.UNKNOWN,
            suggestedCategory = "",
            confidence = 0.0,
            diagnostics = ParserDiagnostics(
                titleDetected = title.isNotBlank(),
                ingredientCount = 0,
                stepCount = 0,
                unclassifiedLines = lines.filter { it.isNotBlank() }.drop(if (title.isNotBlank()) 1 else 0),
                fallbackReasons = listOf("Language could not be determined"),
                lineClassifications = emptyList()
            ),
            rawInput = rawInput
        )
    }
}
