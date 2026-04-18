package com.example.recipemanager.core.parser

import com.example.recipemanager.core.model.Language
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class RecipeParserTest {

    private lateinit var parser: RecipeParser

    @Before
    fun setup() {
        parser = RecipeParser()
    }

    // ── Empty / blank input ─────────────────────────────────────────

    @Test
    fun `parse empty string returns zero confidence`() {
        val result = parser.parse("")
        assertThat(result.confidence).isEqualTo(0.0)
        assertThat(result.title).isEmpty()
        assertThat(result.ingredients).isEmpty()
        assertThat(result.steps).isEmpty()
        assertThat(result.language).isEqualTo(Language.UNKNOWN)
    }

    @Test
    fun `parse blank whitespace returns zero confidence`() {
        val result = parser.parse("   \n  \n  ")
        assertThat(result.confidence).isEqualTo(0.0)
    }

    // ── English recipe with section headers ─────────────────────────

    @Test
    fun `parse English recipe with headers returns high confidence`() {
        val input = """
            Chocolate Chip Cookies

            Ingredients:
            2 cups flour
            1 cup butter
            1 cup chocolate chips
            2 eggs

            Instructions:
            1. Preheat oven to 350°F
            2. Mix flour and butter together
            3. Add eggs and chocolate chips
            4. Bake for 12 minutes
        """.trimIndent()

        val result = parser.parse(input)

        assertThat(result.title).isEqualTo("Chocolate Chip Cookies")
        assertThat(result.language).isEqualTo(Language.EN)
        assertThat(result.ingredients).hasSize(4)
        assertThat(result.steps).hasSize(4)
        assertThat(result.confidence).isGreaterThan(0.7)
        assertThat(result.suggestedCategory).isEqualTo("Dessert")
    }

    @Test
    fun `parse English recipe extracts ingredient text correctly`() {
        val input = """
            Test Recipe

            Ingredients:
            2 cups flour
            1 tsp salt

            Steps:
            Mix everything together
        """.trimIndent()

        val result = parser.parse(input)
        assertThat(result.ingredients.map { it.text }).containsExactly("2 cups flour", "1 tsp salt")
    }

    @Test
    fun `parse English recipe extracts step text correctly`() {
        val input = """
            Test Recipe

            Ingredients:
            2 cups flour

            Directions:
            1. Preheat the oven
            2. Mix the batter
            3. Bake for 20 minutes
        """.trimIndent()

        val result = parser.parse(input)
        assertThat(result.steps).hasSize(3)
        assertThat(result.steps[0].text).isEqualTo("1. Preheat the oven")
    }

    // ── Hebrew recipe with section headers ──────────────────────────

    @Test
    fun `parse Hebrew recipe with headers returns high confidence`() {
        val input = """
            עוגת שוקולד

            מרכיבים:
            2 כוסות קמח
            100 גרם שוקולד
            3 ביצים
            כוס סוכר

            אופן הכנה:
            לחמם תנור ל-180 מעלות
            לערבב את הקמח עם הסוכר
            להוסיף ביצים ושוקולד
            לאפות 30 דקות
        """.trimIndent()

        val result = parser.parse(input)

        assertThat(result.title).isEqualTo("עוגת שוקולד")
        assertThat(result.language).isEqualTo(Language.HE)
        assertThat(result.ingredients).hasSize(4)
        assertThat(result.steps).hasSize(4)
        assertThat(result.confidence).isGreaterThan(0.7)
        assertThat(result.suggestedCategory).isEqualTo("Dessert")
    }

    // ── Recipe without headers (line-by-line fallback) ──────────────

    @Test
    fun `parse recipe without headers uses line-by-line classification`() {
        val input = """
            Simple Pasta

            2 cups pasta
            1 tbsp olive oil
            salt

            Boil water in a large pot
            Cook pasta for 10 minutes
            Drain and serve with olive oil
        """.trimIndent()

        val result = parser.parse(input)

        assertThat(result.title).isEqualTo("Simple Pasta")
        assertThat(result.language).isEqualTo(Language.EN)
        // Without headers, confidence should be lower
        assertThat(result.diagnostics.fallbackReasons).isNotEmpty()
    }

    @Test
    fun `parse recipe with numbered steps without headers detects steps`() {
        val input = """
            Quick Rice

            1 cup rice
            2 cups water

            1. Boil water
            2. Add rice
            3. Simmer for 15 minutes
        """.trimIndent()

        val result = parser.parse(input)
        assertThat(result.title).isEqualTo("Quick Rice")
        // Numbered lines should be detected as steps even without headers
        assertThat(result.steps.size).isGreaterThan(0)
    }

    // ── Confidence scoring ──────────────────────────────────────────

    @Test
    fun `recipe with both ingredients and steps has higher confidence than ingredients only`() {
        val fullRecipe = """
            Full Recipe

            Ingredients:
            1 cup flour

            Steps:
            Mix and bake
        """.trimIndent()

        val partialRecipe = """
            Partial Recipe

            Ingredients:
            1 cup flour
        """.trimIndent()

        val fullResult = parser.parse(fullRecipe)
        val partialResult = parser.parse(partialRecipe)

        assertThat(fullResult.confidence).isGreaterThan(partialResult.confidence)
    }

    @Test
    fun `section-based parsing has higher confidence than line-by-line`() {
        val withHeaders = """
            Recipe A

            Ingredients:
            2 cups flour
            1 cup sugar

            Instructions:
            Mix and bake
        """.trimIndent()

        val withoutHeaders = """
            Recipe B

            2 cups flour
            1 cup sugar

            Mix and bake
        """.trimIndent()

        val headerResult = parser.parse(withHeaders)
        val noHeaderResult = parser.parse(withoutHeaders)

        assertThat(headerResult.confidence).isAtLeast(noHeaderResult.confidence)
    }

    // ── Category detection through parser ───────────────────────────

    @Test
    fun `parser detects Dessert category for cake recipe`() {
        val input = """
            Vanilla Cake

            Ingredients:
            2 cups sugar
            1 cup vanilla cream

            Steps:
            Mix and bake
        """.trimIndent()

        assertThat(parser.parse(input).suggestedCategory).isEqualTo("Dessert")
    }

    @Test
    fun `parser detects Main category for chicken recipe`() {
        val input = """
            Grilled Chicken

            Ingredients:
            500g chicken breast
            1 tbsp olive oil

            Instructions:
            Grill chicken for 20 minutes
        """.trimIndent()

        assertThat(parser.parse(input).suggestedCategory).isEqualTo("Main Course")
    }

    // ── Diagnostics ─────────────────────────────────────────────────

    @Test
    fun `diagnostics reflect parsed counts`() {
        val input = """
            Test

            Ingredients:
            item1
            item2

            Steps:
            do this
        """.trimIndent()

        val diag = parser.parse(input).diagnostics
        assertThat(diag.titleDetected).isTrue()
        assertThat(diag.ingredientCount).isEqualTo(2)
        assertThat(diag.stepCount).isEqualTo(1)
    }

    @Test
    fun `rawInput is preserved in result`() {
        val input = "My Recipe\nIngredients:\n1 cup flour"
        assertThat(parser.parse(input).rawInput).isEqualTo(input)
    }
}
