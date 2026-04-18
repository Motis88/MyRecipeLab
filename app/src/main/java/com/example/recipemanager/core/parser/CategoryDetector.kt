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
            "mousse", "tiramisu", "macaron", "sorbet", "ice cream", "gelato",
            "truffle", "fudge", "caramel", "marshmallow", "donut", "eclair",
            "עוגה", "עוגיות", "שוקולד", "סוכר", "מתוק", "קרם",
            "פאי", "בראוני", "מאפה", "קינוח", "וניל", "קצפת",
            "פודינג", "מאפין", "קאפקייק", "טארט", "מוס", "טירמיסו",
            "מקרון", "סורבה", "גלידה", "טראפל", "פאדג'", "קרמל",
            "מרשמלו", "סופגנייה", "אקלר", "עוגת גבינה"
        )

        private val MAIN_COURSE_KEYWORDS = setOf(
            "chicken", "beef", "rice", "pasta", "meat", "fish", "pork",
            "lamb", "turkey", "steak", "roast", "casserole", "stew",
            "curry", "noodle", "noodles", "tofu", "shrimp", "salmon",
            "tuna", "duck", "veal", "lasagna", "risotto", "paella",
            "schnitzel", "kebab", "meatball", "burger", "wellington",
            "עוף", "בקר", "אורז", "פסטה", "בשר", "דג", "דגים",
            "טלה", "הודו", "סטייק", "צלי", "תבשיל",
            "קארי", "נודלס", "טופו", "סלמון", "טונה", "ברווז",
            "עגל", "לזניה", "ריזוטו", "פאייה", "שניצל", "קבב",
            "קציצות", "המבורגר", "פילה", "מוסקה"
        )

        private val SALAD_KEYWORDS = setOf(
            "salad", "lettuce", "cucumber", "dressing", "vinaigrette",
            "coleslaw", "greens", "arugula", "spinach", "kale", "tomato",
            "mixed greens", "caesar", "greek salad", "caprese",
            "סלט", "חסה", "מלפפון", "רוטב", "ויניגרט", "ירקות", "תרד",
            "עגבנייה", "סלט ירוק", "סלט יווני", "סלט קיסר", "כרוב",
            "רוקט", "קייל", "סלט ממולא", "סלט ניסואז"
        )

        private val SOUP_KEYWORDS = setOf(
            "soup", "broth", "bisque", "chowder", "consomme",
            "stock", "gazpacho", "minestrone", "lentil soup", "chicken soup",
            "tomato soup", "onion soup", "vegetable soup",
            "מרק", "ציר", "מרקית", "מרק עדשים", "מרק עוף",
            "מרק עגבניות", "מרק בצל", "מרק ירקות", "מרק חורפי",
            "מרק שעועית", "גזפצ'ו"
        )

        private val APPETIZER_KEYWORDS = setOf(
            "appetizer", "starter", "bruschetta", "tapas", "crostini", "dip",
            "samosa", "spring roll", "shrimp cocktail", "stuffed mushroom",
            "chicken wing", "nachos", "quesadilla", "finger food",
            "מנה ראשונה", "ברוסקטה", "טפאס", "מתאבן", "סמבוסק",
            "סיגר", "כנפי עוף", "קישואדיה", "חצילים", "קובה",
            "בורקס", "פלפלים ממולאים"
        )

        private val SIDE_DISH_KEYWORDS = setOf(
            "side dish", "side", "mashed", "fries", "couscous", "quinoa",
            "puree", "gratin", "roasted vegetables", "steamed",
            "rice pilaf", "garlic bread", "potato", "vegetables",
            "תוספת", "פירה", "צ'יפס", "קוסקוס", "קינואה", "גרטן",
            "תפוח אדמה", "ירקות צלויים", "ירקות מאודים", "פילאף",
            "לחם שום", "ירקות מוקפצים"
        )

        private val BREAKFAST_KEYWORDS = setOf(
            "breakfast", "omelette", "omelet", "pancake", "pancakes",
            "waffle", "waffles", "cereal", "granola", "shakshuka",
            "french toast", "scrambled egg", "fried egg", "egg", "eggs",
            "morning", "brunch", "bagel and lox",
            "ארוחת בוקר", "חביתה", "פנקייק", "וופל", "גרנולה", "שקשוקה",
            "ביצה", "ביצים", "ביצה קשה", "ביצה רכה", "טוסט צרפתי",
"בוקר", "בראנץ'", "ביצים מקושקשות"
        )

        private val BREAD_KEYWORDS = setOf(
            "bread", "dough", "challah", "focaccia", "pita", "bagel",
            "roll", "baguette", "ciabatta", "sourdough", "croissant",
            "brioche", "breadstick", "flatbread", "rye", "whole wheat",
            "לחם", "בצק", "חלה", "פוקצ'ה", "פיתה", "בייגל", "באגט",
            "קרואסון", "בריוש", "לחם מחמצת", "לחמנייה", "לחם שיפון",
            "לחם מלא", "ג'חנון", "כורסנים"
        )

        private val DRINK_KEYWORDS = setOf(
            "smoothie", "juice", "cocktail", "shake", "lemonade",
            "tea", "coffee", "drink", "beverage", "milkshake",
            "mojito", "margarita", "sangria", "cappuccino", "latte",
            "שייק", "מיץ", "קוקטייל", "לימונדה", "תה", "קפה", "משקה",
            "מוהיטו", "מרגריטה", "סנגריה", "קפוצ'ינו", "לאטה",
            "שייק חלב", "סמוזי", "עסיס"
        )

        private val SNACK_KEYWORDS = setOf(
            "snack", "popcorn", "nachos", "pretzel", "chips",
            "energy bar", "trail mix", "protein bar", "granola bar",
            "crackers", "nuts", "seeds",
            "חטיף", "פופקורן", "נצ'וס", "בייגלה", "צ'יפס",
            "חטיף אנרגיה", "אגוזים", "קרקרים", "גרעינים", "חטיף חלבון"
        )

        private val SAUCE_KEYWORDS = setOf(
            "sauce", "dip", "salsa", "pesto", "hummus", "tahini",
            "marinade", "gravy", "aioli", "guacamole", "tzatziki",
            "hollandaise", "bechamel", "chimichurri", "teriyaki",
            "רוטב", "טבילה", "סלסה", "פסטו", "חומוס", "טחינה", "מרינדה",
            "גוואקמולי", "צזיקי", "טריאקי", "רוטב סויה", "רוטב צ'ילי",
            "מטבוחה", "חריימה"
        )
    }

    fun detect(texts: List<String>, @Suppress("UNUSED_PARAMETER") language: Language): String {
        val combined = texts.joinToString(" ").lowercase()

        // Strong indicators - if these appear in title/name, they're very likely correct
        val titleText = texts.firstOrNull()?.lowercase() ?: ""
        
        // Check for explicit category mentions in title
        when {
            titleText.contains("soup") || titleText.contains("מרק") -> return CATEGORY_SOUP
            titleText.contains("salad") || titleText.contains("סלט") -> return CATEGORY_SALAD
            titleText.contains("dessert") || titleText.contains("קינוח") || 
                titleText.contains("cake") || titleText.contains("עוגה") -> return CATEGORY_DESSERT
            titleText.contains("breakfast") || titleText.contains("בוקר") -> return CATEGORY_BREAKFAST
            titleText.contains("bread") || titleText.contains("לחם") -> return CATEGORY_BREAD
            titleText.contains("sauce") || titleText.contains("dip") || 
                titleText.contains("רוטב") || titleText.contains("טבילה") -> return CATEGORY_SAUCE
        }

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

        // Apply boost for title matches (double weight)
        val titleScores = keywordSets.mapValues { (_, keywords) ->
            keywords.count { titleText.contains(it) }
        }
        
        val finalScores = scores.mapValues { (category, score) ->
            score + (titleScores[category] ?: 0)
        }

        val best = finalScores.maxByOrNull { it.value }
        return if (best != null && best.value > 0) best.key else CATEGORY_UNCATEGORIZED
    }
}
