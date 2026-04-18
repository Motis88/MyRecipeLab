package com.example.recipemanager.presentation.shoppinglist

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.recipemanager.R
import com.example.recipemanager.presentation.common.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    viewModel: ShoppingListViewModel = viewModel(factory = ShoppingListViewModel.factory())
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var checkedItems by remember { mutableStateOf(setOf<Int>()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.shopping_list)) },
                actions = {
                    if (uiState.shoppingItems.isNotEmpty()) {
                        IconButton(onClick = {
                            val text = uiState.shoppingItems.joinToString("\n") { "☐ $it" }
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Shopping List", text))
                            Toast.makeText(context, context.getString(R.string.copy_list), Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = stringResource(R.string.copy_list))
                        }
                        IconButton(onClick = {
                            val text = uiState.shoppingItems.joinToString("\n") { "☐ $it" }
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
                .padding(horizontal = 16.dp)
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

            if (uiState.shoppingItems.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.shopping_list),
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                items(uiState.shoppingItems.size) { index ->
                    val item = uiState.shoppingItems[index]
                    val isChecked = index in checkedItems
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = {
                                checkedItems = if (isChecked) checkedItems - index else checkedItems + index
                            }
                        )
                        Text(
                            text = item,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (isChecked) MaterialTheme.colorScheme.onSurfaceVariant
                            else MaterialTheme.colorScheme.onSurface
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
