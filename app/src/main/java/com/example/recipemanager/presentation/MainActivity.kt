package com.example.recipemanager.presentation

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.recipemanager.app.RecipeManagerApp
import com.example.recipemanager.core.model.AppLanguage
import com.example.recipemanager.core.model.AppTheme
import com.example.recipemanager.presentation.navigation.MainScreen
import com.example.recipemanager.presentation.theme.RecipeManagerTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = (application as RecipeManagerApp).container

        setContent {
            val prefs by container.settingsRepository
                .userPreferences
                .collectAsStateWithLifecycle(
                    initialValue = com.example.recipemanager.core.model.UserPreferences()
                )

            val darkTheme = when (prefs.appTheme) {
                AppTheme.LIGHT -> false
                AppTheme.DARK -> true
                AppTheme.SYSTEM -> isSystemInDarkTheme()
            }

            RecipeManagerTheme(darkTheme = darkTheme) {
                MainScreen()
            }
        }
    }

    companion object {
        fun applyLocale(language: AppLanguage) {
            val localeList = when (language) {
                AppLanguage.SYSTEM -> LocaleListCompat.getEmptyLocaleList()
                AppLanguage.ENGLISH -> LocaleListCompat.forLanguageTags("en")
                AppLanguage.HEBREW -> LocaleListCompat.forLanguageTags("he")
            }
            AppCompatDelegate.setApplicationLocales(localeList)
        }
    }
}
