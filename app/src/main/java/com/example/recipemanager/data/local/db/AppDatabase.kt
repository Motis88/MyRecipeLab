package com.example.recipemanager.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.recipemanager.data.local.dao.CustomCategoryDao
import com.example.recipemanager.data.local.dao.RecipeDao
import com.example.recipemanager.data.local.dao.SearchDao
import com.example.recipemanager.data.local.entity.CustomCategoryEntity
import com.example.recipemanager.data.local.entity.IngredientEntity
import com.example.recipemanager.data.local.entity.IngredientNoteEntity
import com.example.recipemanager.data.local.entity.RecipeEntity
import com.example.recipemanager.data.local.entity.RecipeNoteEntity
import com.example.recipemanager.data.local.entity.RecipeSearchIndexEntity
import com.example.recipemanager.data.local.entity.StepEntity
import com.example.recipemanager.data.local.entity.StepNoteEntity
import com.example.recipemanager.data.local.fts.RecipeSearchFtsEntity

@Database(
    entities = [
        RecipeEntity::class,
        IngredientEntity::class,
        StepEntity::class,
        RecipeNoteEntity::class,
        IngredientNoteEntity::class,
        StepNoteEntity::class,
        CustomCategoryEntity::class,
        RecipeSearchIndexEntity::class,
        RecipeSearchFtsEntity::class
    ],
    version = 3,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun searchDao(): SearchDao
    abstract fun customCategoryDao(): CustomCategoryDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE recipes ADD COLUMN prepTimeMinutes INTEGER DEFAULT NULL")
                db.execSQL("ALTER TABLE recipes ADD COLUMN cookTimeMinutes INTEGER DEFAULT NULL")
                db.execSQL("ALTER TABLE recipes ADD COLUMN servings INTEGER NOT NULL DEFAULT 1")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE recipes ADD COLUMN imagePath TEXT DEFAULT NULL")
            }
        }
    }
}
