package com.example.recipemanager.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "recipe_search_index",
    indices = [Index(value = ["recipeId"], unique = true)]
)
data class RecipeSearchIndexEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    @ColumnInfo(name = "recipeId")
    val recipeId: String,
    @ColumnInfo(name = "searchableContent")
    val searchableContent: String
)
