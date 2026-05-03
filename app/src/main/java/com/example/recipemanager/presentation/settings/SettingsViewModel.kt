package com.example.recipemanager.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.recipemanager.app.RecipeManagerApp
import com.example.recipemanager.core.dispatchers.AppDispatchers
import com.example.recipemanager.core.model.AppLanguage
import com.example.recipemanager.core.model.AppTheme
import com.example.recipemanager.domain.repository.ImportStrategy
import com.example.recipemanager.domain.repository.SettingsRepository
import com.example.recipemanager.domain.usecase.ExportRecipesUseCase
import com.example.recipemanager.domain.usecase.ImportRecipesUseCase
import com.example.recipemanager.domain.usecase.RepairRecipesUseCase
import com.example.recipemanager.presentation.MainActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val exportRecipes: ExportRecipesUseCase,
    private val importRecipes: ImportRecipesUseCase,
    private val repairRecipes: RepairRecipesUseCase,
    private val dispatchers: AppDispatchers
) : ViewModel() {

    private val _operationState = MutableStateFlow(
        SettingsUiState()
    )

    val uiState = combine(
        settingsRepository.userPreferences,
        _operationState
    ) { prefs, ops ->
        ops.copy(
            appLanguage = prefs.appLanguage,
            appTheme = prefs.appTheme
        )
    }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            SettingsUiState()
        )

    fun setAppLanguage(language: AppLanguage) {
        viewModelScope.launch(dispatchers.io) {
            settingsRepository.setAppLanguage(language)
            withContext(dispatchers.main) {
                MainActivity.applyLocale(language)
            }
        }
    }

    fun setAppTheme(theme: AppTheme) {
        viewModelScope.launch(dispatchers.io) {
            settingsRepository.setAppTheme(theme)
        }
    }

    fun exportAllRecipes() {
        _operationState.value = _operationState.value.copy(isExporting = true, exportResult = null)
        viewModelScope.launch(dispatchers.io) {
            val result = try {
                ExportUiResult.Success(exportRecipes())
            } catch (e: Exception) {
                ExportUiResult.Error(e.message ?: "Export failed")
            }
            withContext(dispatchers.main) {
                _operationState.value = _operationState.value.copy(
                    isExporting = false,
                    exportResult = result
                )
            }
        }
    }

    fun importRecipes(json: String, strategy: ImportStrategy) {
        _operationState.value = _operationState.value.copy(isImporting = true, importResult = null)
        viewModelScope.launch(dispatchers.io) {
            val result = importRecipes.invoke(json, strategy).toUiResult()
            withContext(dispatchers.main) {
                _operationState.value = _operationState.value.copy(
                    isImporting = false,
                    importResult = result
                )
            }
        }
    }

    fun clearExportResult() {
        _operationState.value = _operationState.value.copy(exportResult = null)
    }

    fun clearImportResult() {
        _operationState.value = _operationState.value.copy(importResult = null)
    }

    fun repairRecipes() {
        _operationState.value = _operationState.value.copy(isRepairing = true, repairResult = null)
        viewModelScope.launch(dispatchers.io) {
            val result = try {
                val fixedCount = repairRecipes.invoke()
                RepairUiResult.Success(fixedCount)
            } catch (e: Exception) {
                RepairUiResult.Error(e.message ?: "Repair failed")
            }
            withContext(dispatchers.main) {
                _operationState.value = _operationState.value.copy(
                    isRepairing = false,
                    repairResult = result
                )
            }
        }
    }

    fun clearRepairResult() {
        _operationState.value = _operationState.value.copy(repairResult = null)
    }

    companion object {
        fun factory(): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as RecipeManagerApp
                SettingsViewModel(
                    settingsRepository = app.container.settingsRepository,
                    exportRecipes = app.container.exportRecipesUseCase,
                    importRecipes = app.container.importRecipesUseCase,
                    repairRecipes = app.container.repairRecipesUseCase,
                    dispatchers = app.container.appDispatchers
                )
            }
        }
    }
}
