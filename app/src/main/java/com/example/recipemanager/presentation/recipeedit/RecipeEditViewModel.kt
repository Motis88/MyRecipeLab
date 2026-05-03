package com.example.recipemanager.presentation.recipeedit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.recipemanager.app.RecipeManagerApp
import com.example.recipemanager.core.dispatchers.AppDispatchers
import com.example.recipemanager.core.model.Ingredient
import com.example.recipemanager.core.model.Recipe
import com.example.recipemanager.core.model.Step
import com.example.recipemanager.core.util.IdGenerator
import com.example.recipemanager.domain.usecase.GetCategoriesUseCase
import com.example.recipemanager.domain.usecase.GetRecipeByIdUseCase
import com.example.recipemanager.domain.usecase.ParseRecipeUseCase
import com.example.recipemanager.domain.usecase.SaveRecipeUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecipeEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val getRecipeById: GetRecipeByIdUseCase,
    private val parseRecipe: ParseRecipeUseCase,
    private val saveRecipe: SaveRecipeUseCase,
    private val getCategories: GetCategoriesUseCase,
    private val dispatchers: AppDispatchers,
    /**
     * Text shared into the app via ACTION_SEND, captured and cleared by the factory at ViewModel
     * creation time. Non-null only when this is a new-recipe screen opened via share intent.
     */
    private val initialSharedText: String? = null
) : ViewModel() {

    private val recipeId: String? = savedStateHandle["recipeId"]
    private val isNew: Boolean = recipeId == null

    private val _uiState = MutableStateFlow(
        RecipeEditUiState(
            mode = if (isNew) EditMode.ADD else EditMode.EDIT,
            isLoadingRecipe = !isNew
        )
    )
    val uiState = _uiState.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        _uiState.value
    )

    val availableCategories = getCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        // Pre-fill paste field when text was shared into the app via ACTION_SEND
        if (isNew && initialSharedText != null) {
            _uiState.update { it.copy(rawText = initialSharedText, showPasteField = true) }
        }
        if (recipeId != null) {
            viewModelScope.launch(dispatchers.io) {
                val recipe = getRecipeById(recipeId).first()
                withContext(dispatchers.main) {
                    if (recipe != null) {
                        _uiState.update { loadRecipeForEditing(recipe) }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoadingRecipe = false,
                                validationError = "Recipe not found"
                            )
                        }
                    }
                }
            }
        }
    }

    // ---- Raw text / parse ----

    fun updateRawText(text: String) {
        _uiState.update { it.copy(rawText = text, validationError = null) }
    }

    fun parseInput() {
        val raw = _uiState.value.rawText
        if (raw.isBlank()) {
            _uiState.update { it.copy(validationError = "Paste a recipe to parse") }
            return
        }
        _uiState.update { it.copy(isParsing = true, validationError = null) }
        viewModelScope.launch(dispatchers.default) {
            val result = parseRecipe(raw)
            withContext(dispatchers.main) {
                val hasContent = result.ingredients.isNotEmpty() || result.steps.isNotEmpty()
                val forceManual = result.confidence < 0.4 && !hasContent
                val unclassifiedNotes = result.diagnostics.unclassifiedLines
                    .filter { it.isNotBlank() }
                _uiState.update {
                    it.copy(
                        isParsing = false,
                        title = result.title,
                        category = result.suggestedCategory,
                        language = result.language,
                        ingredients = result.ingredients.map { ing ->
                            EditableIngredient(id = IdGenerator.generate(), text = ing.text)
                        },
                        steps = result.steps.map { step ->
                            EditableStep(id = IdGenerator.generate(), text = step.text)
                        },
                        generalNotes = unclassifiedNotes,
                        confidence = result.confidence,
                        parserDiagnostics = result.diagnostics,
                        isManualMode = forceManual
                    )
                }
            }
        }
    }

    fun enterManualMode() {
        _uiState.update { it.copy(isManualMode = true, showPasteField = false) }
    }

    fun enterPasteMode() {
        _uiState.update { it.copy(showPasteField = true, isManualMode = false) }
    }

    // ---- Header field edits ----

    fun updateTitle(title: String) = _uiState.update { it.copy(title = title, validationError = null) }
    fun updateCategory(category: String) = _uiState.update { it.copy(category = category) }
    fun updatePrepTime(minutes: Int?) = _uiState.update { it.copy(prepTimeMinutes = minutes) }
    fun updateCookTime(minutes: Int?) = _uiState.update { it.copy(cookTimeMinutes = minutes) }
    fun updateServings(servings: Int) = _uiState.update { it.copy(servings = servings.coerceAtLeast(1)) }

    // ---- Ingredient operations ----

    fun addIngredient() {
        _uiState.update {
            it.copy(
                ingredients = it.ingredients + EditableIngredient(id = IdGenerator.generate(), text = "")
            )
        }
    }

    fun updateIngredient(index: Int, text: String) {
        _uiState.update {
            val updated = it.ingredients.toMutableList()
            if (index in updated.indices) updated[index] = updated[index].copy(text = text)
            it.copy(ingredients = updated)
        }
    }

    fun removeIngredient(index: Int) {
        _uiState.update {
            val updated = it.ingredients.toMutableList()
            if (index in updated.indices) updated.removeAt(index)
            it.copy(ingredients = updated)
        }
    }

    fun moveIngredient(fromIndex: Int, toIndex: Int) {
        _uiState.update {
            val updated = it.ingredients.toMutableList()
            if (fromIndex in updated.indices && toIndex in updated.indices) {
                val item = updated.removeAt(fromIndex)
                updated.add(toIndex, item)
            }
            it.copy(ingredients = updated)
        }
    }

    fun addIngredientNote(ingredientIndex: Int, note: String) {
        _uiState.update {
            val updated = it.ingredients.toMutableList()
            if (ingredientIndex in updated.indices) {
                val ing = updated[ingredientIndex]
                updated[ingredientIndex] = ing.copy(notes = ing.notes + note)
            }
            it.copy(ingredients = updated)
        }
    }

    fun removeIngredientNote(ingredientIndex: Int, noteIndex: Int) {
        _uiState.update {
            val updated = it.ingredients.toMutableList()
            if (ingredientIndex in updated.indices) {
                val ing = updated[ingredientIndex]
                val notes = ing.notes.toMutableList()
                if (noteIndex in notes.indices) notes.removeAt(noteIndex)
                updated[ingredientIndex] = ing.copy(notes = notes)
            }
            it.copy(ingredients = updated)
        }
    }

    // ---- Step operations ----

    fun addStep() {
        _uiState.update {
            it.copy(steps = it.steps + EditableStep(id = IdGenerator.generate(), text = ""))
        }
    }

    fun updateStep(index: Int, text: String) {
        _uiState.update {
            val updated = it.steps.toMutableList()
            if (index in updated.indices) updated[index] = updated[index].copy(text = text)
            it.copy(steps = updated)
        }
    }

    fun removeStep(index: Int) {
        _uiState.update {
            val updated = it.steps.toMutableList()
            if (index in updated.indices) updated.removeAt(index)
            it.copy(steps = updated)
        }
    }

    fun moveStep(fromIndex: Int, toIndex: Int) {
        _uiState.update {
            val updated = it.steps.toMutableList()
            if (fromIndex in updated.indices && toIndex in updated.indices) {
                val item = updated.removeAt(fromIndex)
                updated.add(toIndex, item)
            }
            it.copy(steps = updated)
        }
    }

    fun addStepNote(stepIndex: Int, note: String) {
        _uiState.update {
            val updated = it.steps.toMutableList()
            if (stepIndex in updated.indices) {
                val step = updated[stepIndex]
                updated[stepIndex] = step.copy(notes = step.notes + note)
            }
            it.copy(steps = updated)
        }
    }

    fun removeStepNote(stepIndex: Int, noteIndex: Int) {
        _uiState.update {
            val updated = it.steps.toMutableList()
            if (stepIndex in updated.indices) {
                val step = updated[stepIndex]
                val notes = step.notes.toMutableList()
                if (noteIndex in notes.indices) notes.removeAt(noteIndex)
                updated[stepIndex] = step.copy(notes = notes)
            }
            it.copy(steps = updated)
        }
    }

    // ---- General notes ----

    fun addGeneralNote(note: String) {
        _uiState.update { it.copy(generalNotes = it.generalNotes + note) }
    }

    fun removeGeneralNote(index: Int) {
        _uiState.update {
            val updated = it.generalNotes.toMutableList()
            if (index in updated.indices) updated.removeAt(index)
            it.copy(generalNotes = updated)
        }
    }

    fun updateGeneralNote(index: Int, text: String) {
        _uiState.update {
            val updated = it.generalNotes.toMutableList()
            if (index in updated.indices) updated[index] = text
            it.copy(generalNotes = updated)
        }
    }

    // ---- Save ----

    fun save() {
        val state = _uiState.value
        if (state.title.isBlank()) {
            _uiState.update { it.copy(validationError = "Title is required") }
            return
        }
        _uiState.update { it.copy(isSaving = true, validationError = null) }
        viewModelScope.launch(dispatchers.io) {
            try {
                val recipeToSave = buildRecipe(state)
                saveRecipe(recipeToSave, isNew)
                withContext(dispatchers.main) {
                    _uiState.update {
                        it.copy(isSaving = false, saveResult = SaveResult.Success(recipeToSave.id))
                    }
                }
            } catch (e: Exception) {
                withContext(dispatchers.main) {
                    _uiState.update {
                        it.copy(isSaving = false, saveResult = SaveResult.Error(e.message ?: "Save failed"))
                    }
                }
            }
        }
    }

    fun clearSaveResult() {
        _uiState.update { it.copy(saveResult = null) }
    }

    fun setOcrProcessing(processing: Boolean) {
        _uiState.update { it.copy(isOcrProcessing = processing) }
    }

    // ---- Private helpers ----

    private fun buildRecipe(state: RecipeEditUiState): Recipe {
        val now = System.currentTimeMillis()
        return Recipe(
            id = recipeId ?: IdGenerator.generate(),
            title = state.title.trim(),
            category = state.category.trim(),
            language = state.language,
            ingredients = state.ingredients.map { ing ->
                Ingredient(id = ing.id, text = ing.text.trim(), notes = ing.notes)
            },
            steps = state.steps.map { step ->
                Step(id = step.id, text = step.text.trim(), notes = step.notes)
            },
            generalNotes = state.generalNotes,
            isFavorite = false,
            prepTimeMinutes = state.prepTimeMinutes,
            cookTimeMinutes = state.cookTimeMinutes,
            servings = state.servings,
            createdAt = now,
            updatedAt = now
        )
    }

    private fun loadRecipeForEditing(recipe: Recipe): RecipeEditUiState = RecipeEditUiState(
        mode = EditMode.EDIT,
        title = recipe.title,
        category = recipe.category,
        language = recipe.language,
        ingredients = recipe.ingredients.map {
            EditableIngredient(id = it.id, text = it.text, notes = it.notes)
        },
        steps = recipe.steps.map {
            EditableStep(id = it.id, text = it.text, notes = it.notes)
        },
        generalNotes = recipe.generalNotes,
        isManualMode = true,
        isLoadingRecipe = false,
        confidence = 1.0,
        prepTimeMinutes = recipe.prepTimeMinutes,
        cookTimeMinutes = recipe.cookTimeMinutes,
        servings = recipe.servings
    )

    companion object {
        fun factory(): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as RecipeManagerApp
                // Capture and atomically clear the pending share text. This prevents a
                // navigation loop in MainScreen: once null, the LaunchedEffect won't re-navigate.
                val sharedText = app.container.pendingShareText.value
                if (sharedText != null) app.container.pendingShareText.value = null
                RecipeEditViewModel(
                    savedStateHandle = createSavedStateHandle(),
                    getRecipeById = app.container.getRecipeByIdUseCase,
                    parseRecipe = app.container.parseRecipeUseCase,
                    saveRecipe = app.container.saveRecipeUseCase,
                    getCategories = app.container.getCategoriesUseCase,
                    dispatchers = app.container.appDispatchers,
                    initialSharedText = sharedText
                )
            }
        }
    }
}
