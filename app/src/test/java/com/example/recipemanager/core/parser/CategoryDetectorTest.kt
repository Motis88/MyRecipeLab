package com.example.recipemanager.core.parser

import com.example.recipemanager.core.model.Language
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class CategoryDetectorTest {

    private lateinit var detector: CategoryDetector

    @Before
    fun setup() {
        detector = CategoryDetector()
    }

    @Test
    fun `detects Dessert for chocolate cake text`() {
        val texts = listOf("Chocolate Cake", "2 cups sugar", "1 cup chocolate")
        assertThat(detector.detect(texts, Language.EN)).isEqualTo("Dessert")
    }

    @Test
    fun `detects Main Course for chicken recipe text`() {
        val texts = listOf("Grilled Chicken", "500g chicken breast", "olive oil")
        assertThat(detector.detect(texts, Language.EN)).isEqualTo("Main Course")
    }

    @Test
    fun `detects Salad for salad recipe text`() {
        val texts = listOf("Caesar Salad", "lettuce", "cucumber", "dressing")
        assertThat(detector.detect(texts, Language.EN)).isEqualTo("Salad")
    }

    @Test
    fun `detects Soup for soup recipe text`() {
        val texts = listOf("Tomato Soup", "chicken broth", "tomatoes")
        assertThat(detector.detect(texts, Language.EN)).isEqualTo("Soup")
    }

    @Test
    fun `returns empty string for no keyword matches`() {
        val texts = listOf("Mystery Dish", "ingredient1", "ingredient2")
        assertThat(detector.detect(texts, Language.EN)).isEmpty()
    }

    @Test
    fun `detects Dessert for Hebrew chocolate recipe`() {
        val texts = listOf("עוגת שוקולד", "סוכר", "קרם")
        assertThat(detector.detect(texts, Language.HE)).isEqualTo("Dessert")
    }

    @Test
    fun `detects Main Course for Hebrew chicken recipe`() {
        val texts = listOf("עוף בתנור", "500 גרם עוף", "שמן זית")
        assertThat(detector.detect(texts, Language.HE)).isEqualTo("Main Course")
    }

    @Test
    fun `detects Soup for Hebrew soup recipe`() {
        val texts = listOf("מרק עוף", "ציר עוף", "ירקות")
        assertThat(detector.detect(texts, Language.HE)).isEqualTo("Soup")
    }

    @Test
    fun `best category wins when multiple match`() {
        // More dessert keywords → Dessert wins
        val texts = listOf("Sugar cookies", "cream cheese frosting", "vanilla", "chocolate")
        assertThat(detector.detect(texts, Language.EN)).isEqualTo("Dessert")
    }

    @Test
    fun `DEFAULT_CATEGORIES contains all expected categories`() {
        assertThat(CategoryDetector.DEFAULT_CATEGORIES).containsExactly(
            "Main Course", "Dessert", "Appetizer", "Side Dish",
            "Soup", "Salad", "Breakfast", "Bread & Pastry",
            "Drink", "Snack", "Sauce & Dip", "Other"
        )
    }
}
