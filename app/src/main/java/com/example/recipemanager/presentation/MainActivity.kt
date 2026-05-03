package com.example.recipemanager.presentation

import android.content.Intent
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

        // Handle share intent on cold start
        handleShareIntent(intent)

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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleShareIntent(intent)
    }

    /**
     * Extracts shared text (from ACTION_SEND) and stores it in the container so the
     * RecipeEditViewModel can consume it on next navigation to the edit screen.
     */
    private fun handleShareIntent(intent: Intent) {
        if (intent.action == Intent.ACTION_SEND &&
            intent.type?.startsWith("text") == true
        ) {
            val text = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (!text.isNullOrBlank()) {
                (application as RecipeManagerApp).container.pendingShareText.value = text
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
