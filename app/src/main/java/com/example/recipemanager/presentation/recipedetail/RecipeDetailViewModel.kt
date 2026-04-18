package com.example.recipemanager.presentation.recipedetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.recipemanager.app.RecipeManagerApp
import com.example.recipemanager.core.dispatchers.AppDispatchers
import com.example.recipemanager.domain.usecase.DeleteRecipeUseCase
import com.example.recipemanager.domain.usecase.GetRecipeByIdUseCase
import com.example.recipemanager.domain.usecase.ToggleFavoriteUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecipeDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val getRecipeById: GetRecipeByIdUseCase,
    private val toggleFavorite: ToggleFavoriteUseCase,
    private val deleteRecipe: DeleteRecipeUseCase,
    private val dispatchers: AppDispatchers
) : ViewModel() {

    val recipeId: String = checkNotNull(savedStateHandle["recipeId"])

    val uiState = getRecipeById(recipeId)
        .map<_, RecipeDetailUiState> { recipe ->
            if (recipe != null) RecipeDetailUiState.Success(recipe)
            else RecipeDetailUiState.NotFound
        }
        .catch { emit(RecipeDetailUiState.Error(it.message ?: "Unknown error")) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            RecipeDetailUiState.Loading
        )

    fun toggleFavorite() {
        val current = uiState.value as? RecipeDetailUiState.Success ?: return
        viewModelScope.launch(dispatchers.io) {
            toggleFavorite.invoke(recipeId, !current.recipe.isFavorite)
        }
    }

    fun requestDelete() {
        val current = uiState.value as? RecipeDetailUiState.Success ?: return
        // Reflect pending delete in the state (UI shows confirm dialog)
        // We use a separate backing flow to avoid overwriting the recipe state
        _showDeleteConfirm.value = true
    }

    private val _showDeleteConfirm = kotlinx.coroutines.flow.MutableStateFlow(false)
    val showDeleteConfirm = _showDeleteConfirm

    fun cancelDelete() {
        _showDeleteConfirm.value = false
    }

    fun confirmDelete(onDeleted: () -> Unit) {
        viewModelScope.launch(dispatchers.io) {
            deleteRecipe(recipeId)
            withContext(dispatchers.main) { onDeleted() }
        }
    }

    companion object {
        fun factory(): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as RecipeManagerApp
                RecipeDetailViewModel(
                    savedStateHandle = createSavedStateHandle(),
                    getRecipeById = app.container.getRecipeByIdUseCase,
                    toggleFavorite = app.container.toggleFavoriteUseCase,
                    deleteRecipe = app.container.deleteRecipeUseCase,
                    dispatchers = app.container.appDispatchers
                )
            }
        }
    }
}
