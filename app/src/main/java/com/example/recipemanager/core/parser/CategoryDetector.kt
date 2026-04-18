package com.example.recipemanager.core.parser

import com.example.recipemanager.R
import com.example.recipemanager.core.model.Language

class CategoryDetector {

    companion object {
        const val CATEGORY_MAIN_COURSE = "Main Course"
        const val CATEGORY_DESSERT = "Dessert"
        const val CATEGORY_SALAD = "Salad"
        const val CATEGORY_SOUP = "Soup"
        const val CATEGORY_APPETIZER = "Appetizer"
        const val CATEGORY_SIDE_DISH = "Side Dish"
        const val CATEGORY_BREAKFAST = "Breakfast"
        const val CATEGORY_BREAD = "Bread & Pastry"
        const val CATEGORY_DRINK = "Drink"
        const val CATEGORY_SNACK = "Snack"
        const val CATEGORY_SAUCE = "Sauce & Dip"
        const val CATEGORY_OTHER = "Other"
        const val CATEGORY_UNCATEGORIZED = ""

        val DEFAULT_CATEGORIES = listOf(
            CATEGORY_MAIN_COURSE,
            CATEGORY_DESSERT,
            CATEGORY_APPETIZER,
            CATEGORY_SIDE_DISH,
            CATEGORY_SOUP,
            CATEGORY_SALAD,
            CATEGORY_BREAKFAST,
            CATEGORY_BREAD,
            CATEGORY_DRINK,
            CATEGORY_SNACK,
            CATEGORY_SAUCE,
            CATEGORY_OTHER
        )

        /** Maps internal category key to a string resource ID for localized display. */
        val CATEGORY_DISPLAY_NAMES: Map<String, Int> = mapOf(
            CATEGORY_MAIN_COURSE to R.string.category_main_course,
            CATEGORY_DESSERT to R.string.category_dessert,
            CATEGORY_SALAD to R.string.category_salad,
            CATEGORY_SOUP to R.string.category_soup,
            CATEGORY_APPETIZER to R.string.category_appetizer,
            CATEGORY_SIDE_DISH to R.string.category_side_dish,
            CATEGORY_BREAKFAST to R.string.category_breakfast,
            CATEGORY_BREAD to R.string.category_bread,
            CATEGORY_DRINK to R.string.category_drink,
            CATEGORY_SNACK to R.string.category_snack,
            CATEGORY_SAUCE to R.string.category_sauce,
            CATEGORY_OTHER to R.string.category_other
        )

        private val DESSERT_KEYWORDS = setOf(
            "cake", "cookie", "cookies", "chocolate", "sugar", "sweet",
            "pie", "brownie", "brownies", "pastry", "dessert", "frosting",
            "icing", "vanilla", "cream", "custard", "pudding", "muffin",
            "muffins", "cupcake", "cupcakes", "tart", "cheesecake",
            "עוגה", "עוגיות", "שוקולד", "סוכר", "מתוק", "קרם",
            "פאי", "בראוני", "מאפה", "קינוח", "וניל", "קצפת",
            "פודינג", "מאפין", "קאפקייק", "טארט"
        )

        private val MAIN_COURSE_KEYWORDS = setOf(
            "chicken", "beef", "rice", "pasta", "meat", "fish", "pork",
            "lamb", "turkey", "steak", "roast", "casserole", "stew",
            "curry", "noodle", "noodles", "tofu", "shrimp",
            "עוף", "בקר", "אורז", "פסטה", "בשר", "דג", "דגים",
            "טלה", "הודו", "סטייק", "צלי", "תבשיל",
            "קארי", "נודלס", "טופו"
        )

        private val SALAD_KEYWORDS = setOf(
            "salad", "lettuce", "cucumber", "dressing", "vinaigrette",
            "coleslaw", "greens", "arugula",
            "סלט", "חסה", "מלפפון", "רוטב", "ויניגרט", "ירקות", "תרד"
        )

        private val SOUP_KEYWORDS = setOf(
            "soup", "broth", "bisque", "chowder", "consomme",
            "stock", "gazpacho", "minestrone",
            "מרק", "ציר", "מרקית"
        )

        private val APPETIZER_KEYWORDS = setOf(
            "appetizer", "starter", "bruschetta", "tapas", "crostini", "dip",
            "מנה ראשונה", "ברוסקטה", "טפאס", "מתאבן"
        )

        private val SIDE_DISH_KEYWORDS = setOf(
            "side dish", "side", "mashed", "fries", "couscous", "quinoa",
            "puree", "gratin",
            "תוספת", "פירה", "צ'יפס", "קוסקוס", "קינואה", "גרטן"
        )

        private val BREAKFAST_KEYWORDS = setOf(
            "breakfast", "omelette", "omelet", "pancake", "pancakes",
            "waffle", "waffles", "cereal", "granola", "shakshuka",
            "ארוחת בוקר", "חביתה", "פנקייק", "וופל", "גרנולה", "שקשוקה"
        )

        private val BREAD_KEYWORDS = setOf(
            "bread", "dough", "challah", "focaccia", "pita", "bagel",
            "roll", "baguette", "ciabatta",
            "לחם", "בצק", "חלה", "פוקצ'ה", "פיתה", "בייגל", "באגט"
        )

        private val DRINK_KEYWORDS = setOf(
            "smoothie", "juice", "cocktail", "shake", "lemonade",
            "tea", "coffee", "drink",
            "שייק", "מיץ", "קוקטייל", "לימונדה", "תה", "קפה", "משקה"
        )

        private val SNACK_KEYWORDS = setOf(
            "snack", "popcorn", "nachos", "pretzel", "chips",
            "energy bar", "trail mix",
            "חטיף", "פופקורן", "נצ'וס", "בייגלה", "צ'יפס"
        )

        private val SAUCE_KEYWORDS = setOf(
            "sauce", "dip", "salsa", "pesto", "hummus", "tahini",
            "marinade", "gravy", "aioli",
            "רוטב", "טבילה", "סלסה", "פסטו", "חומוס", "טחינה", "מרינדה"
        )
    }

    fun detect(texts: List<String>, @Suppress("UNUSED_PARAMETER") language: Language): String {
        val combined = texts.joinToString(" ").lowercase()

        val keywordSets = mapOf(
            CATEGORY_DESSERT to DESSERT_KEYWORDS,
            CATEGORY_MAIN_COURSE to MAIN_COURSE_KEYWORDS,
            CATEGORY_SALAD to SALAD_KEYWORDS,
            CATEGORY_SOUP to SOUP_KEYWORDS,
            CATEGORY_APPETIZER to APPETIZER_KEYWORDS,
            CATEGORY_SIDE_DISH to SIDE_DISH_KEYWORDS,
            CATEGORY_BREAKFAST to BREAKFAST_KEYWORDS,
            CATEGORY_BREAD to BREAD_KEYWORDS,
            CATEGORY_DRINK to DRINK_KEYWORDS,
            CATEGORY_SNACK to SNACK_KEYWORDS,
            CATEGORY_SAUCE to SAUCE_KEYWORDS
        )

        val scores = keywordSets.mapValues { (_, keywords) ->
            keywords.count { combined.contains(it) }
        }

        val best = scores.maxByOrNull { it.value }
        return if (best != null && best.value > 0) best.key else CATEGORY_UNCATEGORIZED
    }
}
