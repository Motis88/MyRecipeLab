package com.example.recipemanager.data.repository

import com.example.recipemanager.core.dispatchers.AppDispatchers
import com.example.recipemanager.core.parser.CategoryDetector
import com.example.recipemanager.core.util.IdGenerator
import com.example.recipemanager.data.local.dao.CustomCategoryDao
import com.example.recipemanager.data.local.entity.CustomCategoryEntity
import com.example.recipemanager.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class CategoryRepositoryImpl(
    private val customCategoryDao: CustomCategoryDao,
    private val dispatchers: AppDispatchers
) : CategoryRepository {

    override fun getDefaultCategories(): List<String> = CategoryDetector.DEFAULT_CATEGORIES

    override fun getCustomCategories(): Flow<List<String>> =
        customCategoryDao.getAllCategories()
            .map { list -> list.map { it.name } }
            .flowOn(dispatchers.io)

    override suspend fun addCustomCategory(name: String) = withContext(dispatchers.io) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return@withContext
        val normalized = normalize(trimmed)
        val existing = customCategoryDao.findByNormalizedName(normalized)
        if (existing == null) {
            customCategoryDao.insertCategory(
                CustomCategoryEntity(
                    id = IdGenerator.generate(),
                    name = trimmed,
                    normalizedName = normalized
                )
            )
        }
    }

    override suspend fun deleteCustomCategory(name: String) = withContext(dispatchers.io) {
        val normalized = normalize(name)
        val entity = customCategoryDao.findByNormalizedName(normalized) ?: return@withContext
        customCategoryDao.deleteCategory(entity)
    }

    override suspend fun categoryExists(name: String): Boolean = withContext(dispatchers.io) {
        customCategoryDao.findByNormalizedName(normalize(name)) != null
            || getDefaultCategories().any { normalize(it) == normalize(name) }
    }

    private fun normalize(name: String): String = name.trim().lowercase()
}
