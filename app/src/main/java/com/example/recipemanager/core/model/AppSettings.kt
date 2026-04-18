package com.example.recipemanager.core.model

enum class AppLanguage {
    SYSTEM,
    ENGLISH,
    HEBREW
}

enum class AppTheme {
    SYSTEM,
    LIGHT,
    DARK
}

data class UserPreferences(
    val appLanguage: AppLanguage = AppLanguage.SYSTEM,
    val appTheme: AppTheme = AppTheme.SYSTEM
)
