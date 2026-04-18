package com.example.recipemanager.presentation.settings

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.recipemanager.R
import com.example.recipemanager.core.model.AppLanguage
import com.example.recipemanager.core.model.AppTheme
import com.example.recipemanager.domain.repository.ImportStrategy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.factory())
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showImportDialog by remember { mutableStateOf(false) }
    var pendingImportJson by remember { mutableStateOf<String?>(null) }

    // File picker for import
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            val json = context.contentResolver.openInputStream(it)?.bufferedReader()?.readText()
            if (json != null) {
                pendingImportJson = json
                showImportDialog = true
            }
        }
    }

    // Export to file via SAF
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            val exportResult = uiState.exportResult
            if (exportResult is ExportUiResult.Success) {
                context.contentResolver.openOutputStream(it)?.use { out ->
                    out.write(exportResult.json.toByteArray())
                }
                viewModel.clearExportResult()
                Toast.makeText(
                    context,
                    context.getString(R.string.export_success),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // When export produces JSON, open file saver
    LaunchedEffect(uiState.exportResult) {
        val result = uiState.exportResult
        if (result is ExportUiResult.Success) {
            exportLauncher.launch("recipes_backup.json")
        }
    }

    // Import strategy dialog
    if (showImportDialog && pendingImportJson != null) {
        AlertDialog(
            onDismissRequest = {
                showImportDialog = false
                pendingImportJson = null
            },
            title = { Text(stringResource(R.string.choose_import_strategy)) },
            text = {
                Column {
                    TextButton(onClick = {
                        showImportDialog = false
                        viewModel.importRecipes(pendingImportJson!!, ImportStrategy.MERGE)
                        pendingImportJson = null
                    }) {
                        Text(stringResource(R.string.import_merge))
                    }
                    TextButton(onClick = {
                        showImportDialog = false
                        viewModel.importRecipes(pendingImportJson!!, ImportStrategy.OVERWRITE)
                        pendingImportJson = null
                    }) {
                        Text(stringResource(R.string.import_overwrite))
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = {
                    showImportDialog = false
                    pendingImportJson = null
                }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Import result toast
    LaunchedEffect(uiState.importResult) {
        when (val r = uiState.importResult) {
            is ImportUiResult.Success -> {
                Toast.makeText(
                    context,
                    context.getString(R.string.import_success, r.importedCount),
                    Toast.LENGTH_SHORT
                ).show()
                viewModel.clearImportResult()
            }
            is ImportUiResult.Error -> {
                Toast.makeText(context, r.message, Toast.LENGTH_LONG).show()
                viewModel.clearImportResult()
            }
            null -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.settings)) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp)
        ) {
            // ── Appearance ──
            Text(
                text = stringResource(R.string.appearance),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Theme selector
            Text(
                text = stringResource(R.string.theme),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            val themeOptions = listOf(
                AppTheme.SYSTEM to stringResource(R.string.system_default),
                AppTheme.LIGHT to stringResource(R.string.light),
                AppTheme.DARK to stringResource(R.string.dark)
            )
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                themeOptions.forEachIndexed { index, (theme, label) ->
                    SegmentedButton(
                        selected = uiState.appTheme == theme,
                        onClick = { viewModel.setAppTheme(theme) },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = themeOptions.size
                        )
                    ) {
                        Text(label)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Language selector
            Text(
                text = stringResource(R.string.language),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            val langOptions = listOf(
                AppLanguage.SYSTEM to stringResource(R.string.system_default),
                AppLanguage.ENGLISH to stringResource(R.string.english),
                AppLanguage.HEBREW to stringResource(R.string.hebrew)
            )
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                langOptions.forEachIndexed { index, (lang, label) ->
                    SegmentedButton(
                        selected = uiState.appLanguage == lang,
                        onClick = { viewModel.setAppLanguage(lang) },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = langOptions.size
                        )
                    ) {
                        Text(label)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))

            // ── Data Management ──
            Text(
                text = stringResource(R.string.data_management),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { viewModel.exportAllRecipes() },
                    enabled = !uiState.isExporting,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.FileUpload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.export_recipes))
                }
                Spacer(modifier = Modifier.width(12.dp))
                OutlinedButton(
                    onClick = { importLauncher.launch(arrayOf("application/json")) },
                    enabled = !uiState.isImporting,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.import_recipes))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.import_export_help),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
