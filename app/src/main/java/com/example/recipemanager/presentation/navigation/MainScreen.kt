package com.example.recipemanager.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.recipemanager.R
import com.example.recipemanager.presentation.favorites.FavoritesScreen
import com.example.recipemanager.presentation.recipedetail.RecipeDetailScreen
import com.example.recipemanager.presentation.recipeedit.RecipeEditScreen
import com.example.recipemanager.presentation.recipelist.RecipeListScreen
import com.example.recipemanager.presentation.settings.SettingsScreen
import com.example.recipemanager.presentation.shoppinglist.ShoppingListScreen

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val topLevelRoutes = listOf(Routes.RECIPE_LIST, Routes.FAVORITES, Routes.SHOPPING_LIST, Routes.SETTINGS)
    val showBottomBar = currentRoute in topLevelRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == Routes.RECIPE_LIST,
                        onClick = {
                            navController.navigate(Routes.RECIPE_LIST) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                Icons.AutoMirrored.Filled.MenuBook,
                                contentDescription = null
                            )
                        },
                        label = { Text(stringResource(R.string.recipes)) }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Routes.FAVORITES,
                        onClick = {
                            navController.navigate(Routes.FAVORITES) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                Icons.Default.Favorite,
                                contentDescription = null
                            )
                        },
                        label = { Text(stringResource(R.string.favorites)) }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Routes.SHOPPING_LIST,
                        onClick = {
                            navController.navigate(Routes.SHOPPING_LIST) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                Icons.Default.ShoppingCart,
                                contentDescription = null
                            )
                        },
                        label = { Text(stringResource(R.string.shopping_list)) }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Routes.SETTINGS,
                        onClick = {
                            navController.navigate(Routes.SETTINGS) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = null
                            )
                        },
                        label = { Text(stringResource(R.string.settings)) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.RECIPE_LIST
        ) {
            composable(Routes.RECIPE_LIST) {
                RecipeListScreen(
                    onNavigateToDetail = { recipeId ->
                        navController.navigate(Routes.recipeDetail(recipeId))
                    },
                    onNavigateToAdd = {
                        navController.navigate(Routes.recipeEdit())
                    }
                )
            }

            composable(
                route = Routes.RECIPE_DETAIL,
                arguments = Routes.recipeDetailArgs
            ) {
                RecipeDetailScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEdit = { recipeId ->
                        navController.navigate(Routes.recipeEdit(recipeId))
                    }
                )
            }

            composable(
                route = Routes.RECIPE_EDIT,
                arguments = Routes.recipeEditArgs
            ) {
                RecipeEditScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Routes.FAVORITES) {
                FavoritesScreen(
                    onNavigateToDetail = { recipeId ->
                        navController.navigate(Routes.recipeDetail(recipeId))
                    }
                )
            }

            composable(Routes.SHOPPING_LIST) {
                ShoppingListScreen()
            }

            composable(Routes.SETTINGS) {
                SettingsScreen()
            }
        }
    }
}
