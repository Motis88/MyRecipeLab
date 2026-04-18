package com.example.recipemanager.domain.usecase

import com.example.recipemanager.domain.repository.BackupRepository

class ExportRecipesUseCase(private val repository: BackupRepository) {
    suspend operator fun invoke(): String = repository.exportAllRecipes()
}
