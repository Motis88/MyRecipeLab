package com.example.recipemanager.presentation.recipedetail

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.recipemanager.R
import com.example.recipemanager.core.model.Recipe
import com.example.recipemanager.core.parser.CategoryDetector
import com.example.recipemanager.core.util.IngredientParser
import com.example.recipemanager.core.util.IngredientScaler
import com.example.recipemanager.data.sharing.RecipeFormatter
import com.example.recipemanager.presentation.common.CookingTimer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToCookingMode: (String) -> Unit = {},
    viewModel: RecipeDetailViewModel = viewModel(factory = RecipeDetailViewModel.factory())
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val showDeleteConfirm by viewModel.showDeleteConfirm.collectAsStateWithLifecycle()

    when (val state = uiState) {
        RecipeDetailUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        RecipeDetailUiState.NotFound -> {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {},
                        navigationIcon = {
                            IconButton(onClick = onNavigateBack) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.back)
                                )
                            }
                        }
                    )
                }
            ) { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.recipe_not_found),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        is RecipeDetailUiState.Error -> {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {},
                        navigationIcon = {
                            IconButton(onClick = onNavigateBack) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.back)
                                )
                            }
                        }
                    )
                }
            ) { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        is RecipeDetailUiState.Success -> {
            RecipeDetailContent(
                recipe = state.recipe,
                showDeleteConfirm = showDeleteConfirm,
                onNavigateBack = onNavigateBack,
                onEdit = { onNavigateToEdit(state.recipe.id) },
                onCookingMode = { onNavigateToCookingMode(state.recipe.id) },
                onToggleFavorite = viewModel::toggleFavorite,
                onRequestDelete = viewModel::requestDelete,
                onCancelDelete = viewModel::cancelDelete,
                onConfirmDelete = { viewModel.confirmDelete(onNavigateBack) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecipeDetailContent(
    recipe: Recipe,
    showDeleteConfirm: Boolean,
    onNavigateBack: () -> Unit,
    onEdit: () -> Unit,
    onCookingMode: () -> Unit,
    onToggleFavorite: () -> Unit,
    onRequestDelete: () -> Unit,
    onCancelDelete: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    val context = LocalContext.current
    var showMoreMenu by remember { mutableStateOf(false) }
    var keepScreenOn by remember { mutableStateOf(false) }
    val view = LocalView.current
    
    DisposableEffect(keepScreenOn) {
        view.keepScreenOn = keepScreenOn
        onDispose { view.keepScreenOn = false }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = onCancelDelete,
            title = { Text(stringResource(R.string.delete_recipe)) },
            text = { Text(stringResource(R.string.delete_confirmation)) },
            confirmButton = {
                TextButton(onClick = onConfirmDelete) {
                    Text(
                        stringResource(R.string.delete),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = onCancelDelete) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = recipe.title,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    // Cooking mode (primary action)
                    if (recipe.steps.isNotEmpty()) {
                        IconButton(onClick = onCookingMode) {
                            Icon(
                                Icons.AutoMirrored.Filled.MenuBook,
                                contentDescription = stringResource(R.string.cooking_mode),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    // Favorite toggle
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = if (recipe.isFavorite) Icons.Default.Favorite
                            else Icons.Default.FavoriteBorder,
                            contentDescription = stringResource(
                                if (recipe.isFavorite) R.string.remove_from_favorites
                                else R.string.add_to_favorites
                            )
                        )
                    }
                    // More actions menu
                    Box {
                        IconButton(onClick = { showMoreMenu = true }) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = stringResource(R.string.more_options)
                            )
                        }
                        DropdownMenu(
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.share_as_text)) },
                                leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                                onClick = {
                                    showMoreMenu = false
                                    val text = RecipeFormatter.formatAsText(recipe)
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, text)
                                        putExtra(Intent.EXTRA_SUBJECT, recipe.title)
                                    }
                                    context.startActivity(Intent.createChooser(intent, null))
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.share_as_json)) },
                                leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                                onClick = {
                                    showMoreMenu = false
                                    val json = RecipeFormatter.formatAsJson(recipe)
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/json"
                                        putExtra(Intent.EXTRA_TEXT, json)
                                        putExtra(Intent.EXTRA_SUBJECT, "${recipe.title}.json")
                                    }
                                    context.startActivity(Intent.createChooser(intent, null))
                                }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.edit_recipe)) },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                                onClick = { showMoreMenu = false; onEdit() }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        stringResource(
                                            if (keepScreenOn) R.string.screen_on_disable
                                            else R.string.keep_screen_on
                                        )
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.LightMode,
                                        contentDescription = null,
                                        tint = if (keepScreenOn) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                onClick = { showMoreMenu = false; keepScreenOn = !keepScreenOn }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        stringResource(R.string.delete_recipe),
                                        color = MaterialTheme.colorScheme.error
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                },
                                onClick = { showMoreMenu = false; onRequestDelete() }
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp)
        ) {
            // Category
            if (recipe.category.isNotBlank()) {
                val displayCategory = CategoryDetector.CATEGORY_DISPLAY_NAMES[recipe.category]
                    ?.let { stringResource(it) }
                    ?: recipe.category
                Text(
                    text = displayCategory,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Time & servings info
            val timeChips = buildList {
                recipe.prepTimeMinutes?.let { add(stringResource(R.string.prep_time_format, it)) }
                recipe.cookTimeMinutes?.let { add(stringResource(R.string.cook_time_format, it)) }
                val total = (recipe.prepTimeMinutes ?: 0) + (recipe.cookTimeMinutes ?: 0)
                if (total > 0) add(stringResource(R.string.total_time, total))
                if (recipe.servings > 1) add(stringResource(R.string.servings_format, recipe.servings))
            }
            if (timeChips.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    timeChips.forEach { chip ->
                        SuggestionChip(onClick = {}, label = { Text(chip) })
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Cooking Timer
            CookingTimer()

            // Servings adjuster for scaling
            var adjustedServings by remember { mutableIntStateOf(recipe.servings.coerceAtLeast(1)) }
            val scaleFactor = adjustedServings.toFloat() / recipe.servings.coerceAtLeast(1).toFloat()

            if (recipe.ingredients.isNotEmpty() && recipe.servings >= 1) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.servings),
                        style = MaterialTheme.typography.titleSmall
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (adjustedServings > 1) adjustedServings-- },
                            enabled = adjustedServings > 1
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = null)
                        }
                        Text(
                            text = adjustedServings.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        IconButton(onClick = { adjustedServings++ }) {
                            Icon(Icons.Default.Add, contentDescription = null)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Ingredients
            if (recipe.ingredients.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.AutoMirrored.Filled.FormatListBulleted,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = stringResource(R.string.ingredients),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                recipe.ingredients.forEach { ingredient ->
                    val scaledText = IngredientScaler.scaleText(ingredient.text, scaleFactor)
                    Row(modifier = Modifier.padding(vertical = 2.dp)) {
                        Text("•", modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = scaledText,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            ingredient.notes.forEach { note ->
                                Text(
                                    text = note,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Steps
            if (recipe.steps.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.AutoMirrored.Filled.Notes,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = stringResource(R.string.steps),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Pre-parse all ingredients once for matching
                val parsedIngredients = remember(recipe.id) {
                    recipe.ingredients.map { ing ->
                        Pair(ing, IngredientParser.parse(ing.text))
                    }
                }

                recipe.steps.forEachIndexed { index, step ->
                    @OptIn(ExperimentalLayoutApi::class)
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Row {
                            Text(
                                text = "${index + 1}.",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(28.dp)
                            )
                            Column {
                                Text(
                                    text = step.text,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                step.notes.forEach { note ->
                                    Text(
                                        text = note,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                                    )
                                }
                            }
                        }
                        // Inline ingredient hints for this step
                        val stepLower = step.text.lowercase()
                        val referencedIngredients = parsedIngredients.filter { (_, parsed) ->
                            val name = parsed.name.lowercase().trim()
                            name.length >= 3 && stepLower.contains(name)
                        }
                        if (referencedIngredients.isNotEmpty()) {
                            @Suppress("OPT_IN_USAGE")
                            FlowRow(
                                modifier = Modifier.padding(start = 28.dp, top = 4.dp, bottom = 2.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                referencedIngredients.forEach { (ing, _) ->
                                    val scaledText = IngredientScaler.scaleText(ing.text, scaleFactor)
                                    AssistChip(
                                        onClick = {},
                                        label = {
                                            Text(
                                                text = scaledText,
                                                style = MaterialTheme.typography.labelSmall,
                                                maxLines = 1
                                            )
                                        },
                                        colors = AssistChipDefaults.assistChipColors(
                                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // General notes
            if (recipe.generalNotes.isNotEmpty()) {
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.general_notes),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                recipe.generalNotes.forEach { note ->
                    Row(modifier = Modifier.padding(vertical = 2.dp)) {
                        Text("•", modifier = Modifier.width(16.dp))
                        Text(
                            text = note,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
