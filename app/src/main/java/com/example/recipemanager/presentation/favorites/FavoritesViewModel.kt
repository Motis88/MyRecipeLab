package com.example.recipemanager.presentation.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.recipemanager.app.RecipeManagerApp
import com.example.recipemanager.core.dispatchers.AppDispatchers
import com.example.recipemanager.domain.usecase.GetFavoriteRecipesUseCase
import com.example.recipemanager.domain.usecase.SearchRecipesUseCase
import com.example.recipemanager.domain.usecase.ToggleFavoriteUseCase
import com.example.recipemanager.presentation.common.SortOrder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(kotlinx.coroutines.FlowPreview::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class FavoritesViewModel(
    private val getFavorites: GetFavoriteRecipesUseCase,
    private val searchRecipes: SearchRecipesUseCase,
    private val toggleFavorite: ToggleFavoriteUseCase,
    private val dispatchers: AppDispatchers
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _sortOrder = MutableStateFlow(SortOrder.RECENT)

    private val debouncedQuery = _searchQuery
        .debounce(300L)
        .distinctUntilChanged()

    private val baseRecipes = debouncedQuery.flatMapLatest { query ->
        if (query.isBlank()) getFavorites()
        else searchRecipes(query).map { list -> list.filter { it.isFavorite } }
    }

    val uiState = combine(baseRecipes, _sortOrder, _searchQuery) { recipes, sortOrder, query ->
        val sorted = when (sortOrder) {
            SortOrder.RECENT -> recipes.sortedByDescending { it.createdAt }
            SortOrder.ALPHABETICAL -> recipes.sortedBy { it.title.lowercase() }
        }
        FavoritesUiState(
            recipes = sorted,
            isLoading = false,
            searchQuery = query,
            sortOrder = sortOrder
        )
    }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            FavoritesUiState(isLoading = true)
        )

    fun updateSearchQuery(query: String) { _searchQuery.value = query }

    fun updateSortOrder(sortOrder: SortOrder) { _sortOrder.value = sortOrder }

    fun toggleFavorite(recipeId: String, isFavorite: Boolean) {
        viewModelScope.launch(dispatchers.io) {
            toggleFavorite.invoke(recipeId, isFavorite)
        }
    }

    companion object {
        fun factory(): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as RecipeManagerApp
                FavoritesViewModel(
                    getFavorites = app.container.getFavoriteRecipesUseCase,
                    searchRecipes = app.container.searchRecipesUseCase,
                    toggleFavorite = app.container.toggleFavoriteUseCase,
                    dispatchers = app.container.appDispatchers
                )
            }
        }
    }
}
