package com.example.recipemanager.presentation.settings

import com.example.recipemanager.core.model.AppLanguage
import com.example.recipemanager.core.model.AppTheme
import com.example.recipemanager.domain.repository.ImportResult

data class SettingsUiState(
    val appLanguage: AppLanguage = AppLanguage.SYSTEM,
    val appTheme: AppTheme = AppTheme.SYSTEM,
    val isExporting: Boolean = false,
    val exportResult: ExportUiResult? = null,
    val isImporting: Boolean = false,
    val importResult: ImportUiResult? = null,
    val isRepairing: Boolean = false,
    val repairResult: RepairUiResult? = null
)

sealed class RepairUiResult {
    data class Success(val fixedCount: Int) : RepairUiResult()
    data class Error(val message: String) : RepairUiResult()
}

sealed class ExportUiResult {
    data class Success(val json: String) : ExportUiResult()
    data class Error(val message: String) : ExportUiResult()
}

sealed class ImportUiResult {
    data class Success(val importedCount: Int) : ImportUiResult()
    data class Error(val message: String) : ImportUiResult()
}

fun ImportResult.toUiResult(): ImportUiResult = when (this) {
    is ImportResult.Success -> ImportUiResult.Success(importedCount)
    is ImportResult.Error -> ImportUiResult.Error(message)
}
