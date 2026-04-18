package com.example.recipemanager.domain.repository

import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getDefaultCategories(): List<String>
    fun getCustomCategories(): Flow<List<String>>
    suspend fun addCustomCategory(name: String)
    suspend fun deleteCustomCategory(name: String)
    suspend fun categoryExists(name: String): Boolean
}
