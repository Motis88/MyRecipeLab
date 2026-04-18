package com.example.recipemanager.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.recipemanager.core.model.AppLanguage
import com.example.recipemanager.core.model.AppTheme
import com.example.recipemanager.core.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class PreferencesDataStore(context: Context) {

    private val dataStore = context.dataStore

    companion object {
        private val KEY_APP_LANGUAGE = stringPreferencesKey("app_language")
        private val KEY_APP_THEME = stringPreferencesKey("app_theme")
    }

    val userPreferences: Flow<UserPreferences> = dataStore.data.map { preferences ->
        UserPreferences(
            appLanguage = preferences[KEY_APP_LANGUAGE]?.let { name ->
                try {
                    AppLanguage.valueOf(name)
                } catch (_: IllegalArgumentException) {
                    AppLanguage.SYSTEM
                }
            } ?: AppLanguage.SYSTEM,
            appTheme = preferences[KEY_APP_THEME]?.let { name ->
                try {
                    AppTheme.valueOf(name)
                } catch (_: IllegalArgumentException) {
                    AppTheme.SYSTEM
                }
            } ?: AppTheme.SYSTEM
        )
    }

    suspend fun setAppLanguage(language: AppLanguage) {
        dataStore.edit { preferences ->
            preferences[KEY_APP_LANGUAGE] = language.name
        }
    }

    suspend fun setAppTheme(theme: AppTheme) {
        dataStore.edit { preferences ->
            preferences[KEY_APP_THEME] = theme.name
        }
    }
}
