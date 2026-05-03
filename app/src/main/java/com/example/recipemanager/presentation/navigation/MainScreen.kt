package com.example.recipemanager.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.recipemanager.R
import com.example.recipemanager.app.RecipeManagerApp
import com.example.recipemanager.presentation.cookingmode.CookingModeScreen
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

    val showBottomBar = currentRoute == Routes.TABS

    // Track which tab is selected (survives recomposition)
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val pagerState = rememberPagerState(initialPage = selectedTab) { 4 }

    // Sync: pager swipe → selectedTab state
    LaunchedEffect(pagerState.settledPage) {
        selectedTab = pagerState.settledPage
    }

    // Sync: bottom nav tap → pager scroll
    LaunchedEffect(selectedTab) {
        if (pagerState.currentPage != selectedTab) {
            pagerState.animateScrollToPage(selectedTab)
        }
    }

    // Observe pending shared text from share intents
    val app = LocalContext.current.applicationContext as RecipeManagerApp
    val pendingShareText by app.container.pendingShareText.collectAsStateWithLifecycle(null)

    LaunchedEffect(pendingShareText) {
        if (pendingShareText != null) {
            navController.navigate(Routes.recipeEdit()) {
                launchSingleTop = true
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = {
                            Icon(
                                Icons.AutoMirrored.Filled.MenuBook,
                                contentDescription = stringResource(R.string.recipes)
                            )
                        },
                        label = { Text(stringResource(R.string.recipes)) }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = {
                            Icon(
                                Icons.Default.Favorite,
                                contentDescription = stringResource(R.string.favorites)
                            )
                        },
                        label = { Text(stringResource(R.string.favorites)) }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = {
                            Icon(
                                Icons.Default.ShoppingCart,
                                contentDescription = stringResource(R.string.shopping_list)
                            )
                        },
                        label = { Text(stringResource(R.string.shopping_list)) }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        icon = {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = stringResource(R.string.settings)
                            )
                        },
                        label = { Text(stringResource(R.string.settings)) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            modifier = Modifier.padding(innerPadding),
            navController = navController,
            startDestination = Routes.TABS,
            enterTransition = {
                fadeIn(animationSpec = tween(300)) +
                    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(300))
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(300)) +
                    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300))
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(300))
            }
        ) {
            // ── All 4 tabs inside a swipeable pager ──
            composable(
                Routes.TABS,
                enterTransition = { fadeIn(tween(200)) },
                exitTransition = { fadeOut(tween(200)) }
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    when (page) {
                        0 -> RecipeListScreen(
                            onNavigateToDetail = { recipeId ->
                                navController.navigate(Routes.recipeDetail(recipeId))
                            },
                            onNavigateToAdd = {
                                navController.navigate(Routes.recipeEdit())
                            }
                        )
                        1 -> FavoritesScreen(
                            onNavigateToDetail = { recipeId ->
                                navController.navigate(Routes.recipeDetail(recipeId))
                            },
                            onNavigateToAdd = {
                                navController.navigate(Routes.recipeEdit())
                            }
                        )
                        2 -> ShoppingListScreen()
                        else -> SettingsScreen()
                    }
                }
            }

            // ── Nested screens ──
            composable(
                route = Routes.RECIPE_DETAIL,
                arguments = Routes.recipeDetailArgs
            ) {
                RecipeDetailScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEdit = { recipeId ->
                        navController.navigate(Routes.recipeEdit(recipeId))
                    },
                    onNavigateToCookingMode = { recipeId ->
                        navController.navigate(Routes.cookingMode(recipeId))
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

            composable(
                route = Routes.COOKING_MODE,
                arguments = Routes.cookingModeArgs,
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }
            ) {
                CookingModeScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

