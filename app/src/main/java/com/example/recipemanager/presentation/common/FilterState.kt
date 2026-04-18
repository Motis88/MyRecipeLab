package com.example.recipemanager.presentation.common

enum class SortOrder { RECENT, ALPHABETICAL }

data class RecipeFilter(
    val searchQuery: String = "",
    val category: String? = null,
    val favoritesOnly: Boolean = false,
    val sortOrder: SortOrder = SortOrder.RECENT
)
