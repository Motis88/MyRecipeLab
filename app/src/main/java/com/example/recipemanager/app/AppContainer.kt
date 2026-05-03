package com.example.recipemanager.app

import android.content.Context
import androidx.room.Room
import com.example.recipemanager.core.dispatchers.AppDispatchers
import com.example.recipemanager.core.parser.RecipeParser
import com.example.recipemanager.data.datastore.PreferencesDataStore
import com.example.recipemanager.data.local.db.AppDatabase
import com.example.recipemanager.data.repository.BackupRepositoryImpl
import com.example.recipemanager.data.repository.CategoryRepositoryImpl
import com.example.recipemanager.data.repository.RecipeRepositoryImpl
import com.example.recipemanager.data.repository.SettingsRepositoryImpl
import com.example.recipemanager.domain.repository.BackupRepository
import com.example.recipemanager.domain.repository.CategoryRepository
import com.example.recipemanager.domain.repository.RecipeRepository
import com.example.recipemanager.domain.repository.SettingsRepository
import com.example.recipemanager.domain.usecase.DeleteRecipeUseCase
import com.example.recipemanager.domain.usecase.ExportRecipesUseCase
import com.example.recipemanager.domain.usecase.GetAllRecipesUseCase
import com.example.recipemanager.domain.usecase.GetCategoriesUseCase
import com.example.recipemanager.domain.usecase.GetFavoriteRecipesUseCase
import com.example.recipemanager.domain.usecase.GetRecipeByIdUseCase
import com.example.recipemanager.domain.usecase.ImportRecipesUseCase
import com.example.recipemanager.domain.usecase.ParseRecipeUseCase
import com.example.recipemanager.domain.usecase.SaveRecipeUseCase
import com.example.recipemanager.domain.usecase.SearchRecipesUseCase
import com.example.recipemanager.domain.usecase.RepairRecipesUseCase
import com.example.recipemanager.domain.usecase.ToggleFavoriteUseCase
import kotlinx.coroutines.flow.MutableStateFlow

class AppContainer(context: Context) {

    val appDispatchers: AppDispatchers = AppDispatchers()

    /** Holds text shared into the app via ACTION_SEND. Consumed by RecipeEditViewModel on init. */
    val pendingShareText = MutableStateFlow<String?>(null)

    val database: AppDatabase = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "recipe_manager.db"
    )
        .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3)
        .build()

    val preferencesDataStore: PreferencesDataStore = PreferencesDataStore(context.applicationContext)

    val recipeRepository: RecipeRepository = RecipeRepositoryImpl(
        recipeDao = database.recipeDao(),
        searchDao = database.searchDao(),
        dispatchers = appDispatchers
    )

    val categoryRepository: CategoryRepository = CategoryRepositoryImpl(
        customCategoryDao = database.customCategoryDao(),
        dispatchers = appDispatchers
    )

    val settingsRepository: SettingsRepository = SettingsRepositoryImpl(
        preferencesDataStore = preferencesDataStore
    )

    val backupRepository: BackupRepository = BackupRepositoryImpl(
        recipeDao = database.recipeDao(),
        searchDao = database.searchDao(),
        dispatchers = appDispatchers
    )

    // Use cases
    val getAllRecipesUseCase = GetAllRecipesUseCase(recipeRepository)
    val getFavoriteRecipesUseCase = GetFavoriteRecipesUseCase(recipeRepository)
    val getRecipeByIdUseCase = GetRecipeByIdUseCase(recipeRepository)
    val searchRecipesUseCase = SearchRecipesUseCase(recipeRepository)
    val saveRecipeUseCase = SaveRecipeUseCase(recipeRepository)
    val deleteRecipeUseCase = DeleteRecipeUseCase(recipeRepository)
    val toggleFavoriteUseCase = ToggleFavoriteUseCase(recipeRepository)
    val getCategoriesUseCase = GetCategoriesUseCase(categoryRepository)
    val parseRecipeUseCase = ParseRecipeUseCase(RecipeParser())
    val exportRecipesUseCase = ExportRecipesUseCase(backupRepository)
    val importRecipesUseCase = ImportRecipesUseCase(backupRepository)
    val repairRecipesUseCase = RepairRecipesUseCase(recipeRepository)
}
