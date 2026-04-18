package com.example.recipemanager.presentation.recipelist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.recipemanager.R
import com.example.recipemanager.core.parser.CategoryDetector
import com.example.recipemanager.presentation.common.EmptyState
import com.example.recipemanager.presentation.common.RecipeCard
import com.example.recipemanager.presentation.common.SortOrder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeListScreen(
    onNavigateToDetail: (String) -> Unit,
    onNavigateToAdd: () -> Unit,
    viewModel: RecipeListViewModel = viewModel(factory = RecipeListViewModel.factory())
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val categories by viewModel.availableCategories.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.recipes)) })
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToAdd,
                icon = {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null
                    )
                },
                text = { Text(stringResource(R.string.add_recipe)) }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Search bar
            OutlinedTextField(
                value = filter.searchQuery,
                onValueChange = viewModel::updateSearchQuery,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text(stringResource(R.string.search_recipes)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (filter.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = null)
                        }
                    }
                },
                singleLine = true
            )

            // Filter chips row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) {
                // Sort chips
                item {
                    FilterChip(
                        selected = filter.sortOrder == SortOrder.RECENT,
                        onClick = { viewModel.updateSortOrder(SortOrder.RECENT) },
                        label = { Text(stringResource(R.string.sort_recent)) },
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
                item {
                    FilterChip(
                        selected = filter.sortOrder == SortOrder.ALPHABETICAL,
                        onClick = { viewModel.updateSortOrder(SortOrder.ALPHABETICAL) },
                        label = { Text(stringResource(R.string.sort_alphabetical)) },
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
                // Divider space
                item { Spacer(modifier = Modifier.width(8.dp)) }
                // Category chips
                item {
                    FilterChip(
                        selected = filter.category == null,
                        onClick = { viewModel.updateCategory(null) },
                        label = { Text(stringResource(R.string.all_categories)) },
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
                items(categories) { category ->
                    val displayName = CategoryDetector.CATEGORY_DISPLAY_NAMES[category]
                        ?.let { stringResource(it) }
                        ?: category
                    FilterChip(
                        selected = filter.category == category,
                        onClick = { viewModel.updateCategory(category) },
                        label = { Text(displayName) },
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Content
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = uiState.error ?: stringResource(R.string.error_occurred),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                uiState.recipes.isEmpty() -> {
                    EmptyState(
                        message = stringResource(R.string.no_recipes_found),
                        action = {
                            Button(onClick = onNavigateToAdd) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.add_recipe))
                            }
                        }
                    )
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 64.dp)
                    ) {
                        items(uiState.recipes, key = { it.id }) { recipe ->
                            RecipeCard(
                                recipe = recipe,
                                onClick = { onNavigateToDetail(recipe.id) },
                                onFavoriteClick = {
                                    viewModel.toggleFavorite(recipe.id, !recipe.isFavorite)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
