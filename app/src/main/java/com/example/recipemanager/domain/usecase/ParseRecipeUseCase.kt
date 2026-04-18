package com.example.recipemanager.domain.usecase

import com.example.recipemanager.core.parser.ParserResult
import com.example.recipemanager.core.parser.RecipeParser

class ParseRecipeUseCase(private val parser: RecipeParser) {
    operator fun invoke(input: String): ParserResult = parser.parse(input)
}
