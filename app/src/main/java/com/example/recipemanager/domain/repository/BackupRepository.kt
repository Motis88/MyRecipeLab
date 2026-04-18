package com.example.recipemanager.domain.repository

interface BackupRepository {
    suspend fun exportAllRecipes(): String
    suspend fun importRecipes(json: String, strategy: ImportStrategy): ImportResult
}

enum class ImportStrategy { MERGE, OVERWRITE }

sealed class ImportResult {
    data class Success(val importedCount: Int) : ImportResult()
    data class Error(val message: String) : ImportResult()
}
