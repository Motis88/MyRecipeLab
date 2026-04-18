package com.example.recipemanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.recipemanager.data.local.entity.RecipeSearchIndexEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class SearchDao {

    @Query("""
        SELECT recipeId FROM recipe_search_index 
        WHERE id IN (
            SELECT docid FROM recipe_search_fts WHERE recipe_search_fts MATCH :query
        )
    """)
    abstract fun search(query: String): Flow<List<String>>

    @Insert
    abstract suspend fun insertSearchIndex(entity: RecipeSearchIndexEntity)

    @Query("DELETE FROM recipe_search_index WHERE recipeId = :recipeId")
    abstract suspend fun deleteSearchIndex(recipeId: String)

    @Query("DELETE FROM recipe_search_index")
    abstract suspend fun clearSearchIndex()

    @Transaction
    open suspend fun upsertSearchIndex(recipeId: String, searchableContent: String) {
        deleteSearchIndex(recipeId)
        insertSearchIndex(
            RecipeSearchIndexEntity(
                recipeId = recipeId,
                searchableContent = searchableContent
            )
        )
    }
}
