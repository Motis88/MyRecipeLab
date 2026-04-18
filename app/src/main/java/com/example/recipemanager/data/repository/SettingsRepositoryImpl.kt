package com.example.recipemanager.data.repository

import com.example.recipemanager.core.model.AppLanguage
import com.example.recipemanager.core.model.AppTheme
import com.example.recipemanager.core.model.UserPreferences
import com.example.recipemanager.data.datastore.PreferencesDataStore
import com.example.recipemanager.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

class SettingsRepositoryImpl(
    private val preferencesDataStore: PreferencesDataStore
) : SettingsRepository {

    override val userPreferences: Flow<UserPreferences> = preferencesDataStore.userPreferences

    override suspend fun setAppLanguage(language: AppLanguage) =
        preferencesDataStore.setAppLanguage(language)

    override suspend fun setAppTheme(theme: AppTheme) =
        preferencesDataStore.setAppTheme(theme)
}
