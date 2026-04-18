package com.example.recipemanager.domain.usecase

import com.example.recipemanager.domain.repository.BackupRepository
import com.example.recipemanager.domain.repository.ImportResult
import com.example.recipemanager.domain.repository.ImportStrategy

class ImportRecipesUseCase(private val repository: BackupRepository) {
    suspend operator fun invoke(json: String, strategy: ImportStrategy): ImportResult =
        repository.importRecipes(json, strategy)
}
