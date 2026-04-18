package com.example.recipemanager.domain.repository

import com.example.recipemanager.core.model.AppLanguage
import com.example.recipemanager.core.model.AppTheme
import com.example.recipemanager.core.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val userPreferences: Flow<UserPreferences>
    suspend fun setAppLanguage(language: AppLanguage)
    suspend fun setAppTheme(theme: AppTheme)
}
