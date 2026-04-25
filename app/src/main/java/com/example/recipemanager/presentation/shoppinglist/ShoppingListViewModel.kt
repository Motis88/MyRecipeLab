package com.example.recipemanager.presentation.shoppinglist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.recipemanager.app.RecipeManagerApp
import com.example.recipemanager.core.dispatchers.AppDispatchers
import com.example.recipemanager.core.util.GroceryAggregator
import com.example.recipemanager.domain.usecase.GetAllRecipesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class ShoppingListViewModel(
    getAllRecipes: GetAllRecipesUseCase,
    @Suppress("UNUSED_PARAMETER") private val dispatchers: AppDispatchers
) : ViewModel() {

    private val _selectedIds = MutableStateFlow<Set<String>>(emptySet())

    val uiState = combine(
        getAllRecipes(),
        _selectedIds
    ) { recipes, selected ->
        val groceryItems = if (selected.isEmpty()) {
            emptyList()
        } else {
            val texts = recipes
                .filter { it.id in selected }
                .flatMap { it.ingredients }
                .map { it.text.trim() }
                .filter { it.isNotBlank() }
            GroceryAggregator.aggregate(texts)
        }
        val byCategory = GroceryAggregator.groupByCategory(groceryItems)
        ShoppingListUiState(
            allRecipes = recipes,
            selectedRecipeIds = selected,
            groceryItems = groceryItems,
            groceryByCategory = byCategory,
            isLoading = false
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        ShoppingListUiState()
    )

    fun toggleRecipe(recipeId: String) {
        _selectedIds.update { current ->
            if (recipeId in current) current - recipeId else current + recipeId
        }
    }

    fun clearSelection() {
        _selectedIds.value = emptySet()
    }

    companion object {
        fun factory(): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as RecipeManagerApp
                ShoppingListViewModel(
                    getAllRecipes = app.container.getAllRecipesUseCase,
                    dispatchers = app.container.appDispatchers
                )
            }
        }
    }
}

