package com.example.recipemanager.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "recipes",
    indices = [
        Index("category"),
        Index("isFavorite"),
        Index("createdAt"),
        Index("title")
    ]
)
data class RecipeEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "category")
    val category: String,
    @ColumnInfo(name = "language")
    val language: String,
    @ColumnInfo(name = "isFavorite")
    val isFavorite: Boolean,
    @ColumnInfo(name = "prepTimeMinutes")
    val prepTimeMinutes: Int? = null,
    @ColumnInfo(name = "cookTimeMinutes")
    val cookTimeMinutes: Int? = null,
    @ColumnInfo(name = "servings")
    val servings: Int = 1,
    @ColumnInfo(name = "createdAt")
    val createdAt: Long,
    @ColumnInfo(name = "updatedAt")
    val updatedAt: Long,
    @ColumnInfo(name = "imagePath")
    val imagePath: String? = null
)
