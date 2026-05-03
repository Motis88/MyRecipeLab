package com.example.recipemanager.presentation.cookingmode

import android.speech.tts.TextToSpeech
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.VoiceOverOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.recipemanager.R
import com.example.recipemanager.core.model.Recipe
import com.example.recipemanager.presentation.recipedetail.RecipeDetailUiState
import com.example.recipemanager.presentation.recipedetail.RecipeDetailViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookingModeScreen(
    onNavigateBack: () -> Unit,
    viewModel: RecipeDetailViewModel = viewModel(factory = RecipeDetailViewModel.factory())
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val view = LocalView.current

    // Keep screen awake for the entire duration of cooking mode
    DisposableEffect(Unit) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }

    when (val state = uiState) {
        RecipeDetailUiState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is RecipeDetailUiState.NotFound,
        is RecipeDetailUiState.Error -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.recipe_not_found))
            }
        }

        is RecipeDetailUiState.Success -> {
            CookingModeContent(
                recipe = state.recipe,
                onNavigateBack = onNavigateBack
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CookingModeContent(
    recipe: Recipe,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val steps = recipe.steps
    var currentStep by remember { mutableIntStateOf(0) }
    var ttsEnabled by remember { mutableStateOf(false) }
    var ttsReady by remember { mutableStateOf(false) }
    val tts = remember { mutableStateOf<TextToSpeech?>(null) }

    // Initialize TTS engine once
    DisposableEffect(Unit) {
        val engine = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsReady = true
            }
        }
        tts.value = engine
        onDispose {
            engine.stop()
            engine.shutdown()
            tts.value = null
        }
    }

    // Speak current step whenever it changes and TTS is on
    LaunchedEffect(currentStep, ttsEnabled, ttsReady) {
        if (ttsEnabled && ttsReady && steps.isNotEmpty()) {
            val engine = tts.value ?: return@LaunchedEffect
            // Set language based on first RTL char presence (simple heuristic)
            val stepText = steps[currentStep].text
            val locale = if (stepText.any { it.code in 0x0590..0x05FF }) {
                Locale("he", "IL")
            } else {
                Locale.getDefault().takeIf { it.language.isNotBlank() } ?: Locale.ENGLISH
            }
            engine.language = locale
            engine.stop()
            engine.speak(stepText, TextToSpeech.QUEUE_FLUSH, null, "step_$currentStep")
        } else {
            tts.value?.stop()
        }
    }

    val isLastStep = steps.isEmpty() || currentStep >= steps.lastIndex
    val progress = if (steps.isEmpty()) 1f
    else (currentStep + 1).toFloat() / steps.size.toFloat()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = recipe.title,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1
                        )
                        if (steps.isNotEmpty()) {
                            Text(
                                text = stringResource(
                                    R.string.cooking_step_of,
                                    currentStep + 1,
                                    steps.size
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
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
                    IconButton(
                        onClick = { ttsEnabled = !ttsEnabled },
                        enabled = ttsReady
                    ) {
                        Icon(
                            imageVector = if (ttsEnabled) Icons.Default.VoiceOverOff
                            else Icons.Default.RecordVoiceOver,
                            contentDescription = stringResource(
                                if (ttsEnabled) R.string.tts_disable else R.string.tts_enable
                            ),
                            tint = if (ttsEnabled) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Progress bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth()
            )

            if (steps.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_steps),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Step content — slides left/right on navigation
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        if (targetState > initialState) {
                            slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                        } else {
                            slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
                        }
                    },
                    label = "step_transition",
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) { step ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 32.dp, vertical = 24.dp),
                        contentAlignment = Alignment.TopStart
                    ) {
                        Text(
                            text = steps[step].text,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Normal,
                            lineHeight = MaterialTheme.typography.headlineSmall.lineHeight,
                            textAlign = TextAlign.Start
                        )
                    }
                }

                // Navigation row
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous button
                    FilledIconButton(
                        onClick = { if (currentStep > 0) currentStep-- },
                        enabled = currentStep > 0,
                        modifier = Modifier.size(64.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.prev_step),
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Step counter pill
                    Box(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = MaterialTheme.shapes.medium
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "${currentStep + 1} / ${steps.size}",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Next / Done button
                    FilledIconButton(
                        onClick = {
                            if (!isLastStep) currentStep++
                            else onNavigateBack()
                        },
                        modifier = Modifier.size(64.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = if (isLastStep)
                                MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.primary,
                            contentColor = if (isLastStep)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(
                            imageVector = if (isLastStep) Icons.Default.CheckCircle
                            else Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = stringResource(
                                if (isLastStep) R.string.cooking_done else R.string.next_step
                            ),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}
