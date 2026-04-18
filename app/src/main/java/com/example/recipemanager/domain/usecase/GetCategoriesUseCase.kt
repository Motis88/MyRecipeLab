package com.example.recipemanager.domain.usecase

import com.example.recipemanager.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf

class GetCategoriesUseCase(private val repository: CategoryRepository) {
    operator fun invoke(): Flow<List<String>> = combine(
        flowOf(repository.getDefaultCategories()),
        repository.getCustomCategories()
    ) { defaults, customs ->
        (defaults + customs).distinctBy { it.trim().lowercase() }
    }
}
