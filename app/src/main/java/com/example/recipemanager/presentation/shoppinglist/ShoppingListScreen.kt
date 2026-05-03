package com.example.recipemanager.presentation.shoppinglist

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.recipemanager.R
import com.example.recipemanager.core.model.GroceryCategory
import com.example.recipemanager.core.model.GroceryItem
import com.example.recipemanager.core.util.GroceryAggregator
import com.example.recipemanager.presentation.common.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    viewModel: ShoppingListViewModel = viewModel(factory = ShoppingListViewModel.factory())
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var checkedItems by remember { mutableStateOf(setOf<String>()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.shopping_list)) },
                actions = {
                    if (uiState.groceryItems.isNotEmpty()) {
                        IconButton(onClick = {
                            val text = buildShareText(uiState.groceryByCategory)
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Shopping List", text))
                            Toast.makeText(context, context.getString(R.string.copy_list), Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = stringResource(R.string.copy_list))
                        }
                        IconButton(onClick = {
                            val text = buildShareText(uiState.groceryByCategory)
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, text)
                            }
                            context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_list)))
                        }) {
                            Icon(Icons.Default.Share, contentDescription = stringResource(R.string.share_list))
                        }
                        IconButton(onClick = {
                            viewModel.clearSelection()
                            checkedItems = emptySet()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.clear_list))
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 64.dp)
        ) {
            // Recipe selector
            item {
                Text(
                    text = stringResource(R.string.select_recipes),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            item {
                Column {
                    uiState.allRecipes.chunked(2).forEach { rowRecipes ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowRecipes.forEach { recipe ->
                                FilterChip(
                                    selected = recipe.id in uiState.selectedRecipeIds,
                                    onClick = { viewModel.toggleRecipe(recipe.id) },
                                    label = { Text(recipe.title, maxLines = 1) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (rowRecipes.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            if (uiState.groceryItems.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.shopping_list),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // Render category sections
                uiState.groceryByCategory.forEach { (category, items) ->
                    item(key = "header_${category.name}") {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = groceryCategoryLabel(category, context),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                    items(items, key = { "${category.name}_${it.name}_${it.unit}" }) { item ->
                        GroceryRow(
                            item = item,
                            isChecked = itemKey(item) in checkedItems,
                            onCheckedChange = { checked ->
                                checkedItems = if (checked)
                                    checkedItems + itemKey(item)
                                else
                                    checkedItems - itemKey(item)
                            }
                        )
                    }
                }
            } else if (uiState.selectedRecipeIds.isEmpty() && !uiState.isLoading) {
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                    EmptyState(message = stringResource(R.string.shopping_list_empty))
                }
            }
        }
    }
}

@Composable
private fun GroceryRow(
    item: GroceryItem,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = onCheckedChange
        )
        Text(
            text = item.name,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
            color = if (isChecked) MaterialTheme.colorScheme.onSurfaceVariant
            else MaterialTheme.colorScheme.onSurface
        )
        val qty = item.displayQty()
        if (qty.isNotBlank()) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = qty,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isChecked) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.primary
            )
        }
    }
}

private fun itemKey(item: GroceryItem): String = "${item.name}|${item.unit}"

private fun groceryCategoryLabel(category: GroceryCategory, context: Context): String {
    val resId = when (category) {
        GroceryCategory.PRODUCE -> R.string.grocery_produce
        GroceryCategory.DAIRY -> R.string.grocery_dairy
        GroceryCategory.MEAT -> R.string.grocery_meat
        GroceryCategory.SEAFOOD -> R.string.grocery_seafood
        GroceryCategory.DRY_GOODS -> R.string.grocery_dry_goods
        GroceryCategory.SPICES -> R.string.grocery_spices
        GroceryCategory.BAKERY -> R.string.grocery_bakery
        GroceryCategory.FROZEN -> R.string.grocery_frozen
        GroceryCategory.BEVERAGES -> R.string.grocery_beverages
        GroceryCategory.OTHER -> R.string.grocery_other
    }
    return context.getString(resId)
}

private fun buildShareText(
    byCategory: Map<GroceryCategory, List<GroceryItem>>
): String = buildString {
    byCategory.forEach { (_, items) ->
        items.forEach { item ->
            append("☐ ")
            append(GroceryAggregator.formatItem(item))
            append("\n")
        }
    }
}.trimEnd()

