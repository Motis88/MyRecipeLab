package com.example.recipemanager.data.local.fts

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import com.example.recipemanager.data.local.entity.RecipeSearchIndexEntity

@Fts4(contentEntity = RecipeSearchIndexEntity::class)
@Entity(tableName = "recipe_search_fts")
data class RecipeSearchFtsEntity(
    @ColumnInfo(name = "searchableContent")
    val searchableContent: String
)
