package com.example.recipemanager.presentation.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument

object Routes {
    const val TABS = "tabs"
    const val RECIPE_LIST = "recipe_list"
    const val FAVORITES = "favorites"
    const val SHOPPING_LIST = "shopping_list"
    const val SETTINGS = "settings"

    const val RECIPE_DETAIL = "recipe_detail/{recipeId}"
    const val RECIPE_EDIT = "recipe_edit?recipeId={recipeId}"
    const val COOKING_MODE = "cooking_mode/{recipeId}"

    val recipeDetailArgs = listOf(
        navArgument("recipeId") { type = NavType.StringType }
    )

    val recipeEditArgs = listOf(
        navArgument("recipeId") {
            type = NavType.StringType
            nullable = true
            defaultValue = null
        }
    )

    val cookingModeArgs = listOf(
        navArgument("recipeId") { type = NavType.StringType }
    )

    fun recipeDetail(recipeId: String): String = "recipe_detail/$recipeId"

    fun recipeEdit(recipeId: String? = null): String =
        if (recipeId != null) "recipe_edit?recipeId=$recipeId" else "recipe_edit"

    fun cookingMode(recipeId: String): String = "cooking_mode/$recipeId"
}
