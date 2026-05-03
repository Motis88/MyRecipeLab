package com.example.recipemanager.presentation.recipeedit

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.recipemanager.R
import com.example.recipemanager.core.parser.CategoryDetector
import com.example.recipemanager.presentation.common.ConfidenceBanner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeEditScreen(
    onNavigateBack: () -> Unit,
    viewModel: RecipeEditViewModel = viewModel(factory = RecipeEditViewModel.factory())
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val categories by viewModel.availableCategories.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val savedMsg = stringResource(R.string.recipe_saved)
    val ocrErrorMsg = stringResource(R.string.ocr_error)

    // Camera temp file holder
    val cameraImageUri = remember { mutableStateOf<Uri?>(null) }

    // Gallery launcher → OCR
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.setOcrProcessing(true)
            coroutineScope.launch {
                try {
                    val text = extractTextFromImage(context, it)
                    viewModel.updateRawText(text)
                    viewModel.enterPasteMode()
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar(ocrErrorMsg)
                } finally {
                    viewModel.setOcrProcessing(false)
                }
            }
        }
    }

    // Take-picture launcher → OCR
    val takePictureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraImageUri.value?.let { uri ->
                viewModel.setOcrProcessing(true)
                coroutineScope.launch {
                    try {
                        val text = extractTextFromImage(context, uri)
                        viewModel.updateRawText(text)
                        viewModel.enterPasteMode()
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar(ocrErrorMsg)
                    } finally {
                        viewModel.setOcrProcessing(false)
                    }
                }
            }
        }
    }

    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val cacheDir = File(context.cacheDir, "camera_images").also { it.mkdirs() }
            val file = File(cacheDir, "recipe_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            cameraImageUri.value = uri
            takePictureLauncher.launch(uri)
        }
    }

    fun onScanFromCamera() {
        cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
    }

    fun onScanFromGallery() {
        galleryLauncher.launch("image/*")
    }

    LaunchedEffect(uiState.saveResult) {
        when (uiState.saveResult) {
            is SaveResult.Success -> {
                snackbarHostState.showSnackbar(savedMsg)
                onNavigateBack()
            }
            is SaveResult.Error -> {
                snackbarHostState.showSnackbar(
                    (uiState.saveResult as SaveResult.Error).message
                )
                viewModel.clearSaveResult()
            }
            null -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (uiState.mode == EditMode.ADD) R.string.add_recipe
                            else R.string.edit_recipe
                        )
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
                    val showSave = uiState.isManualMode || uiState.title.isNotBlank()
                    if (showSave) {
                        TextButton(
                            onClick = { viewModel.save() },
                            enabled = !uiState.isSaving
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp))
                            } else {
                                Text(stringResource(R.string.save))
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoadingRecipe) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        if (uiState.isOcrProcessing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(stringResource(R.string.ocr_processing))
                }
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp)
        ) {
            val showChoiceMode =
                !uiState.isManualMode && !uiState.showPasteField && uiState.mode == EditMode.ADD && uiState.title.isBlank()
            val showPasteMode =
                !uiState.isManualMode && uiState.showPasteField && uiState.mode == EditMode.ADD && uiState.title.isBlank()

            if (showChoiceMode) {
                AddModeChooser(
                    onPasteMode = viewModel::enterPasteMode,
                    onManualMode = viewModel::enterManualMode,
                    onScanFromGallery = ::onScanFromGallery,
                    onScanFromCamera = ::onScanFromCamera
                )
            } else if (showPasteMode) {
                PasteSection(
                    rawText = uiState.rawText,
                    onRawTextChange = viewModel::updateRawText,
                    isParsing = uiState.isParsing,
                    onParse = viewModel::parseInput,
                    onSwitchToManual = viewModel::enterManualMode,
                    validationError = uiState.validationError
                )
            } else {
                // Confidence banner for freshly parsed recipes
                if (uiState.mode == EditMode.ADD && uiState.parserDiagnostics != null) {
                    ConfidenceBanner(confidence = uiState.confidence)
                    Spacer(modifier = Modifier.height(12.dp))
                }

                EditForm(
                    uiState = uiState,
                    categories = categories,
                    onTitleChange = viewModel::updateTitle,
                    onCategoryChange = viewModel::updateCategory,
                    onAddIngredient = viewModel::addIngredient,
                    onUpdateIngredient = viewModel::updateIngredient,
                    onRemoveIngredient = viewModel::removeIngredient,
                    onMoveIngredient = viewModel::moveIngredient,
                    onAddIngredientNote = viewModel::addIngredientNote,
                    onRemoveIngredientNote = viewModel::removeIngredientNote,
                    onAddStep = viewModel::addStep,
                    onUpdateStep = viewModel::updateStep,
                    onRemoveStep = viewModel::removeStep,
                    onMoveStep = viewModel::moveStep,
                    onAddStepNote = viewModel::addStepNote,
                    onRemoveStepNote = viewModel::removeStepNote,
                    onAddGeneralNote = viewModel::addGeneralNote,
                    onUpdateGeneralNote = viewModel::updateGeneralNote,
                    onRemoveGeneralNote = viewModel::removeGeneralNote,
                    onPrepTimeChange = viewModel::updatePrepTime,
                    onCookTimeChange = viewModel::updateCookTime,
                    onServingsChange = viewModel::updateServings
                )
            }
        }
    }
}

@Composable
private fun AddModeChooser(
    onPasteMode: () -> Unit,
    onManualMode: () -> Unit,
    onScanFromGallery: () -> Unit,
    onScanFromCamera: () -> Unit
) {
    Text(
        text = stringResource(R.string.how_to_add),
        style = MaterialTheme.typography.titleMedium
    )
    Spacer(modifier = Modifier.height(16.dp))

    Card(
        onClick = onPasteMode,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.ContentPaste,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.paste_and_parse),
                    style = MaterialTheme.typography.titleSmall
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.paste_and_parse_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 40.dp)
            )
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Image scan card
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.PhotoCamera,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.scan_image),
                    style = MaterialTheme.typography.titleSmall
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.scan_image_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 40.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 40.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onScanFromCamera,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.PhotoCamera,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.take_photo),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
                OutlinedButton(
                    onClick = onScanFromGallery,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(R.string.choose_from_gallery),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    Card(
        onClick = onManualMode,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.enter_manually),
                    style = MaterialTheme.typography.titleSmall
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.enter_manually_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 40.dp)
            )
        }
    }
}

@Composable
private fun PasteSection(
    rawText: String,
    onRawTextChange: (String) -> Unit,
    isParsing: Boolean,
    onParse: () -> Unit,
    onSwitchToManual: () -> Unit,
    validationError: String?
) {
    OutlinedTextField(
        value = rawText,
        onValueChange = onRawTextChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
        placeholder = { Text(stringResource(R.string.paste_recipe_text)) },
        isError = validationError != null,
        supportingText = validationError?.let { { Text(it) } }
    )

    Spacer(modifier = Modifier.height(12.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onParse,
            enabled = !isParsing && rawText.isNotBlank()
        ) {
            if (isParsing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.parsing_text))
            } else {
                Text(stringResource(R.string.parse))
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        TextButton(onClick = onSwitchToManual) {
            Text(stringResource(R.string.manual_mode))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditForm(
    uiState: RecipeEditUiState,
    categories: List<String>,
    onTitleChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onAddIngredient: () -> Unit,
    onUpdateIngredient: (Int, String) -> Unit,
    onRemoveIngredient: (Int) -> Unit,
    onMoveIngredient: (Int, Int) -> Unit,
    onAddIngredientNote: (Int, String) -> Unit,
    onRemoveIngredientNote: (Int, Int) -> Unit,
    onAddStep: () -> Unit,
    onUpdateStep: (Int, String) -> Unit,
    onRemoveStep: (Int) -> Unit,
    onMoveStep: (Int, Int) -> Unit,
    onAddStepNote: (Int, String) -> Unit,
    onRemoveStepNote: (Int, Int) -> Unit,
    onAddGeneralNote: (String) -> Unit,
    onUpdateGeneralNote: (Int, String) -> Unit,
    onRemoveGeneralNote: (Int) -> Unit,
    onPrepTimeChange: (Int?) -> Unit,
    onCookTimeChange: (Int?) -> Unit,
    onServingsChange: (Int) -> Unit
) {
    // Title
    OutlinedTextField(
        value = uiState.title,
        onValueChange = onTitleChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.title_label)) },
        isError = uiState.validationError != null,
        supportingText = uiState.validationError?.let { { Text(it) } },
        singleLine = true
    )

    Spacer(modifier = Modifier.height(12.dp))

    // Category dropdown
    var categoryExpanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = categoryExpanded,
        onExpandedChange = { categoryExpanded = it }
    ) {
        val displayCategory = CategoryDetector.CATEGORY_DISPLAY_NAMES[uiState.category]
            ?.let { stringResource(it) }
            ?: uiState.category
        OutlinedTextField(
            value = displayCategory,
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
            label = { Text(stringResource(R.string.category)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
            singleLine = true,
            readOnly = true
        )
        ExposedDropdownMenu(
            expanded = categoryExpanded,
            onDismissRequest = { categoryExpanded = false }
        ) {
            categories.forEach { category ->
                val displayName = CategoryDetector.CATEGORY_DISPLAY_NAMES[category]
                    ?.let { stringResource(it) }
                    ?: category
                DropdownMenuItem(
                    text = { Text(displayName) },
                    onClick = {
                        onCategoryChange(category)
                        categoryExpanded = false
                    }
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Prep time, Cook time, Servings
    Row(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = uiState.prepTimeMinutes?.toString() ?: "",
            onValueChange = { onPrepTimeChange(it.toIntOrNull()) },
            modifier = Modifier.weight(1f),
            label = { Text(stringResource(R.string.prep_time)) },
            suffix = { Text(stringResource(R.string.minutes_short)) },
            singleLine = true
        )
        Spacer(modifier = Modifier.width(8.dp))
        OutlinedTextField(
            value = uiState.cookTimeMinutes?.toString() ?: "",
            onValueChange = { onCookTimeChange(it.toIntOrNull()) },
            modifier = Modifier.weight(1f),
            label = { Text(stringResource(R.string.cook_time)) },
            suffix = { Text(stringResource(R.string.minutes_short)) },
            singleLine = true
        )
        Spacer(modifier = Modifier.width(8.dp))
        OutlinedTextField(
            value = uiState.servings.toString(),
            onValueChange = { onServingsChange(it.toIntOrNull() ?: 1) },
            modifier = Modifier.weight(1f),
            label = { Text(stringResource(R.string.servings)) },
            singleLine = true
        )
    }

    Spacer(modifier = Modifier.height(20.dp))
    HorizontalDivider()
    Spacer(modifier = Modifier.height(16.dp))

    // ── Ingredients ──
    SectionHeader(
        title = stringResource(R.string.ingredients),
        onAdd = onAddIngredient,
        addLabel = stringResource(R.string.add_ingredient)
    )
    Spacer(modifier = Modifier.height(8.dp))

    uiState.ingredients.forEachIndexed { index, ingredient ->
        EditableItemRow(
            text = ingredient.text,
            hint = stringResource(R.string.ingredient_hint),
            onTextChange = { onUpdateIngredient(index, it) },
            onRemove = { onRemoveIngredient(index) },
            onMoveUp = if (index > 0) {
                { onMoveIngredient(index, index - 1) }
            } else null,
            onMoveDown = if (index < uiState.ingredients.size - 1) {
                { onMoveIngredient(index, index + 1) }
            } else null,
            notes = ingredient.notes,
            onAddNote = { onAddIngredientNote(index, "") },
            onRemoveNote = { noteIdx -> onRemoveIngredientNote(index, noteIdx) }
        )
    }

    Spacer(modifier = Modifier.height(20.dp))
    HorizontalDivider()
    Spacer(modifier = Modifier.height(16.dp))

    // ── Steps ──
    SectionHeader(
        title = stringResource(R.string.steps),
        onAdd = onAddStep,
        addLabel = stringResource(R.string.add_step)
    )
    Spacer(modifier = Modifier.height(8.dp))

    uiState.steps.forEachIndexed { index, step ->
        EditableItemRow(
            text = step.text,
            hint = "${stringResource(R.string.step_hint)} ${index + 1}",
            onTextChange = { onUpdateStep(index, it) },
            onRemove = { onRemoveStep(index) },
            onMoveUp = if (index > 0) {
                { onMoveStep(index, index - 1) }
            } else null,
            onMoveDown = if (index < uiState.steps.size - 1) {
                { onMoveStep(index, index + 1) }
            } else null,
            notes = step.notes,
            onAddNote = { onAddStepNote(index, "") },
            onRemoveNote = { noteIdx -> onRemoveStepNote(index, noteIdx) }
        )
    }

    Spacer(modifier = Modifier.height(20.dp))
    HorizontalDivider()
    Spacer(modifier = Modifier.height(16.dp))

    // ── General Notes ──
    var newNote by remember { mutableStateOf("") }
    SectionHeader(
        title = stringResource(R.string.general_notes),
        onAdd = {
            onAddGeneralNote("")
        },
        addLabel = stringResource(R.string.add_note)
    )
    Spacer(modifier = Modifier.height(8.dp))

    uiState.generalNotes.forEachIndexed { index, note ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = note,
                onValueChange = { onUpdateGeneralNote(index, it) },
                modifier = Modifier.weight(1f),
                placeholder = { Text(stringResource(R.string.note_hint)) },
                singleLine = false,
                maxLines = 3
            )
            IconButton(onClick = { onRemoveGeneralNote(index) }) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = stringResource(R.string.remove),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(24.dp))
}

@Composable
private fun SectionHeader(
    title: String,
    onAdd: () -> Unit,
    addLabel: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f)
        )
        OutlinedButton(onClick = onAdd) {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(addLabel)
        }
    }
}

@Composable
private fun EditableItemRow(
    text: String,
    hint: String,
    onTextChange: (String) -> Unit,
    onRemove: () -> Unit,
    onMoveUp: (() -> Unit)?,
    onMoveDown: (() -> Unit)?,
    notes: List<String>,
    onAddNote: () -> Unit,
    onRemoveNote: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text(hint) },
                singleLine = false,
                maxLines = 3
            )
            Column {
                if (onMoveUp != null) {
                    IconButton(onClick = onMoveUp, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.ArrowUpward,
                            contentDescription = stringResource(R.string.move_up),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                if (onMoveDown != null) {
                    IconButton(onClick = onMoveDown, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.ArrowDownward,
                            contentDescription = stringResource(R.string.move_down),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = stringResource(R.string.remove),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }

        // Notes for this item
        notes.forEachIndexed { noteIndex, note ->
            Row(
                modifier = Modifier.padding(start = 24.dp, top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = note.ifBlank { stringResource(R.string.note_hint) },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { onRemoveNote(noteIndex) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = stringResource(R.string.remove),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        TextButton(
            onClick = onAddNote,
            modifier = Modifier.padding(start = 16.dp)
        ) {
            Text(
                stringResource(R.string.add_note),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

/**
 * Extracts text from an image URI using ML Kit Text Recognition.
 * Runs on a coroutine — call from a coroutine scope.
 */
private suspend fun extractTextFromImage(
    context: android.content.Context,
    uri: Uri
): String = suspendCancellableCoroutine { cont ->
    val image = try {
        InputImage.fromFilePath(context, uri)
    } catch (e: Exception) {
        cont.resumeWithException(e)
        return@suspendCancellableCoroutine
    }
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    recognizer.process(image)
        .addOnSuccessListener { result ->
            recognizer.close()
            cont.resume(result.text)
        }
        .addOnFailureListener { e ->
            recognizer.close()
            cont.resumeWithException(e)
        }
    cont.invokeOnCancellation { recognizer.close() }
}
