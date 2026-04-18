package com.example.recipemanager.data.sharing

import android.content.Context
import android.content.Intent
import com.example.recipemanager.core.model.Recipe

object RecipeSharingHelper {

    fun shareAsText(context: Context, recipe: Recipe) {
        val text = RecipeFormatter.formatAsText(recipe)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_SUBJECT, recipe.title)
        }
        context.startActivity(Intent.createChooser(intent, recipe.title))
    }

    fun shareAsJson(context: Context, recipe: Recipe) {
        val json = RecipeFormatter.formatAsJson(recipe)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, json)
            putExtra(Intent.EXTRA_SUBJECT, "${recipe.title}.json")
        }
        context.startActivity(Intent.createChooser(intent, recipe.title))
    }
}
