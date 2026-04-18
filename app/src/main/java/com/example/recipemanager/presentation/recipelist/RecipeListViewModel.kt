package com.example.recipemanager.presentation.recipelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.recipemanager.app.RecipeManagerApp
import com.example.recipemanager.core.dispatchers.AppDispatchers
import com.example.recipemanager.domain.usecase.GetAllRecipesUseCase
import com.example.recipemanager.domain.usecase.GetCategoriesUseCase
import com.example.recipemanager.domain.usecase.SearchRecipesUseCase
import com.example.recipemanager.domain.usecase.ToggleFavoriteUseCase
import com.example.recipemanager.presentation.common.RecipeFilter
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
class RecipeListViewModel(
    private val getAllRecipes: GetAllRecipesUseCase,
    private val searchRecipes: SearchRecipesUseCase,
    private val toggleFavorite: ToggleFavoriteUseCase,
    private val getCategories: GetCategoriesUseCase,
    private val dispatchers: AppDispatchers
) : ViewModel() {

    private val _filter = MutableStateFlow(RecipeFilter())
    val filter = _filter

    // Debounce only the search query part; other filter fields react immediately
    private val debouncedFilter = combine(
        _filter.map { it.copy(searchQuery = "") }.distinctUntilChanged(),
        _filter.map { it.searchQuery }.distinctUntilChanged().debounce(300L)
    ) { filterWithoutQuery, query ->
        filterWithoutQuery.copy(searchQuery = query)
    }

    private val searchResults = debouncedFilter.flatMapLatest { filter ->
        if (filter.searchQuery.isBlank()) getAllRecipes()
        else searchRecipes(filter.searchQuery)
    }

    val availableCategories = getCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val uiState = combine(searchResults, _filter) { recipes, filter ->
        val filtered = recipes
            .filter { recipe ->
                (filter.category == null || recipe.category == filter.category) &&
                    (!filter.favoritesOnly || recipe.isFavorite)
            }
            .let { list ->
                when (filter.sortOrder) {
                    SortOrder.RECENT -> list.sortedByDescending { it.createdAt }
                    SortOrder.ALPHABETICAL -> list.sortedBy { it.title.lowercase() }
                }
            }
        RecipeListUiState(
            recipes = filtered,
            isLoading = false,
            filter = filter,
            availableCategories = availableCategories.value
        )
    }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            RecipeListUiState(isLoading = true)
        )

    fun updateSearchQuery(query: String) {
        _filter.value = _filter.value.copy(searchQuery = query)
    }

    fun updateCategory(category: String?) {
        _filter.value = _filter.value.copy(category = category)
    }

    fun updateFavoritesOnly(favoritesOnly: Boolean) {
        _filter.value = _filter.value.copy(favoritesOnly = favoritesOnly)
    }

    fun updateSortOrder(sortOrder: SortOrder) {
        _filter.value = _filter.value.copy(sortOrder = sortOrder)
    }

    fun clearFilters() {
        _filter.value = RecipeFilter(searchQuery = _filter.value.searchQuery)
    }

    fun toggleFavorite(recipeId: String, isFavorite: Boolean) {
        viewModelScope.launch(dispatchers.io) {
            toggleFavorite.invoke(recipeId, isFavorite)
        }
    }

    companion object {
        fun factory(): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as RecipeManagerApp
                RecipeListViewModel(
                    getAllRecipes = app.container.getAllRecipesUseCase,
                    searchRecipes = app.container.searchRecipesUseCase,
                    toggleFavorite = app.container.toggleFavoriteUseCase,
                    getCategories = app.container.getCategoriesUseCase,
                    dispatchers = app.container.appDispatchers
                )
            }
        }
    }
}
