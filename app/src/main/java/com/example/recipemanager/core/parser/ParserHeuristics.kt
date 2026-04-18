package com.example.recipemanager.core.parser

import com.example.recipemanager.core.model.Language

class ParserHeuristics {

    companion object {
        private val EN_INGREDIENT_HEADERS = setOf(
            "ingredients", "ingredient list", "ingredient",
            "what you'll need", "what you need", "you will need",
            "shopping list", "grocery list"
        )
        private val EN_STEP_HEADERS = setOf(
            "directions", "steps", "instructions", "method",
            "preparation", "procedure", "how to make",
            "how to prepare", "cooking instructions", "cooking directions"
        )
        private val HE_INGREDIENT_HEADERS = setOf(
            "מרכיבים", "מצרכים", "חומרים", "רשימת מרכיבים",
            "רשימת חומרים", "מה צריך", "צריך"
        )
        private val HE_STEP_HEADERS = setOf(
            "הוראות הכנה", "אופן הכנה", "שלבים", "הכנה",
            "הוראות", "דרך הכנה", "שיטת הכנה",
            "אופן ההכנה", "הוראות ההכנה", "שלבי הכנה"
        )

        val EN_UNITS_PATTERN: Regex = Regex(
            """\b(\d+[\s./]*\d*)\s*(g|gram|grams|kg|ml|l|liter|liters|cup|cups|tsp|tbsp|oz|lb|lbs|teaspoon|teaspoons|tablespoon|tablespoons|ounce|ounces|pound|pounds|pinch|dash|bunch|clove|cloves|handful|piece|pieces|slice|slices|can|cans|package|pkg|stick|sticks|head|heads)\b""",
            RegexOption.IGNORE_CASE
        )

        val HE_UNITS_PATTERN: Regex = Regex(
            """(\d+[\s./]*\d*)\s*(כוס|כוסות|כפית|כפיות|כף|כפות|גרם|ק"ג|מ"ל|ליטר|יחידה|יחידות|שן|שיני|חבילה|חבילות|פרוסה|פרוסות|אגורה|קופסה|קופסאות|חופן|ענף|ענפים|עלה|עלים)"""
        )

        /** Matches a bare number at start of line (e.g., "2 eggs", "3 בצלים") */
        val BARE_NUMBER_START: Regex = Regex("""^\s*(\d+[\s./]*\d*|[½¼¾⅓⅔⅛])\s+\S""")

        val FRACTION_PATTERN: Regex = Regex("""\b\d+\s*/\s*\d+\b""")

        /** Matches bullet/dash/asterisk list markers */
        val LIST_MARKER_PATTERN: Regex = Regex("""^\s*[-–—•*·▪]\s""")

        val NUMBERED_LINE_PATTERN: Regex = Regex("""^\s*\d+[\s.):\-]""")

        /** Common food/ingredient words that strongly suggest a line is an ingredient */
        private val EN_FOOD_WORDS = setOf(
            "salt", "pepper", "sugar", "flour", "butter", "oil", "olive oil",
            "garlic", "onion", "onions", "egg", "eggs", "milk", "cream",
            "cheese", "water", "chicken", "beef", "rice", "pasta",
            "tomato", "tomatoes", "lemon", "potato", "potatoes",
            "carrot", "carrots", "celery", "parsley", "cilantro", "basil",
            "oregano", "thyme", "cinnamon", "vanilla", "honey", "vinegar",
            "soy sauce", "mustard", "mayonnaise", "yogurt", "bread",
            "mushroom", "mushrooms", "bell pepper", "zucchini", "spinach",
            "corn", "beans", "chickpeas", "lentils", "nuts", "almonds",
            "walnuts", "sesame", "ginger", "cumin", "paprika", "turmeric",
            "baking powder", "baking soda", "yeast", "cornstarch"
        )

        private val HE_FOOD_WORDS = setOf(
            "מלח", "פלפל", "סוכר", "קמח", "חמאה", "שמן", "שמן זית",
            "שום", "בצל", "ביצה", "ביצים", "חלב", "שמנת",
            "גבינה", "מים", "עוף", "בקר", "אורז", "פסטה",
            "עגבנייה", "עגבניות", "לימון", "תפוח אדמה", "תפוחי אדמה",
            "גזר", "סלרי", "פטרוזיליה", "כוסברה", "בזיליקום",
            "אורגנו", "קינמון", "וניל", "דבש", "חומץ",
            "רוטב סויה", "חרדל", "מיונז", "יוגורט", "לחם",
            "פטריות", "פלפל ירוק", "קישוא", "תרד",
            "תירס", "שעועית", "חומוס", "עדשים", "אגוזים",
            "שקדים", "אגוזי מלך", "שומשום", "ג'ינג'ר", "כמון",
            "פפריקה", "כורכום", "אבקת אפייה", "סודה לשתייה",
            "שמרים", "קורנפלור", "טחינה", "פירורי לחם",
            "בצלים", "שומן", "רוטב", "דגים", "נתחי", "מרק",
            "מלפפון", "חסה", "גבינה צהובה", "גבינה לבנה", "קוטג'",
            "פלפלים", "בטטה", "דלעת", "כרובית", "ברוקולי",
            "שעועית ירוקה", "אפונה", "גרגירי חומוס", "עגבניות שרי",
            "בזיליקום טרי", "נענע", "זעתר", "כרכום טרי", "פלפל חריף",
            "בשר טחון", "חזה עוף", "שוקיים", "כנפיים", "סטייק",
            "מטבוחה", "חציל", "חצילים", "שמן קנולה", "שמן צמחי",
            "דג", "סלמון", "טונה", "לימונים"
        )

        private val EN_COOKING_VERBS = setOf(
            "mix", "add", "bake", "heat", "chop", "stir", "whisk",
            "pour", "cook", "preheat", "combine", "fold", "knead",
            "let", "place", "remove", "serve", "set", "slice",
            "dice", "mince", "grate", "drain", "rinse", "season",
            "marinate", "sauté", "saute", "fry", "boil", "simmer",
            "roast", "grill", "broil", "blend", "melt", "spread",
            "garnish", "cover", "refrigerate", "freeze", "brush",
            "toss", "layer", "roll", "shape", "cool", "transfer"
        )

        private val HE_COOKING_VERBS = setOf(
            "לערבב", "להוסיף", "לחמם", "לאפות", "לקצוץ", "לבשל",
            "לשפוך", "להקציף", "לחתוך", "לגרד", "לסנן", "לתבל",
            "לטגן", "לרתוח", "להניח", "להגיש", "לכסות", "לקרר",
            "למזוג", "ללוש", "להמיס", "למרוח", "לערום", "לנקות",
            "לשטוף", "להפוך", "לקלף", "לרדד", "לעגל", "לצנן",
            "מערבבים", "מוסיפים", "מחממים", "אופים", "קוצצים", "מבשלים",
            "שופכים", "מקציפים", "חותכים", "גורדים", "מסננים", "מתבלים",
            "מטגנים", "מרתיחים", "מניחים", "מגישים", "מכסים", "מקררים",
            "מוזגים", "לשים", "שמים", "מערבלים",
            "לערבל", "לצלות", "צולים", "מצלים",
            "מכינים", "להכין", "מפזרים", "לפזר", "מורידים", "להוריד",
            "מעבירים", "להעביר", "מוציאים", "להוציא", "מורחים",
            "מציפים", "לציפות", "לאדות", "מאדים", "מקפיצים", "להקפיץ"
        )
    }

    fun detectSectionHeader(line: String, language: Language): LineType? {
        val trimmed = line.trim()
            .removeSuffix(":")
            .removeSuffix("：")
            .trim()
            .lowercase()

        // Skip lines that are too long to be headers
        if (trimmed.length > 50) return null

        val ingredientHeaders = when (language) {
            Language.EN -> EN_INGREDIENT_HEADERS
            Language.HE -> HE_INGREDIENT_HEADERS + EN_INGREDIENT_HEADERS
            Language.UNKNOWN -> EN_INGREDIENT_HEADERS + HE_INGREDIENT_HEADERS
        }
        val stepHeaders = when (language) {
            Language.EN -> EN_STEP_HEADERS
            Language.HE -> HE_STEP_HEADERS + EN_STEP_HEADERS
            Language.UNKNOWN -> EN_STEP_HEADERS + HE_STEP_HEADERS
        }

        // Exact match first
        if (ingredientHeaders.any { trimmed == it }) return LineType.INGREDIENT
        if (stepHeaders.any { trimmed == it }) return LineType.STEP

        // Partial match: header text is contained in the line (e.g., "רשימת מרכיבים למתכון")
        if (ingredientHeaders.any { trimmed.contains(it) }) return LineType.INGREDIENT
        if (stepHeaders.any { trimmed.contains(it) }) return LineType.STEP

        return null
    }

    fun ingredientConfidence(line: String, language: Language): Double {
        val trimmed = line.trim()
        if (trimmed.isBlank()) return 0.0

        var confidence = 0.0

        // Measurement units are a strong signal
        if (EN_UNITS_PATTERN.containsMatchIn(trimmed) || HE_UNITS_PATTERN.containsMatchIn(trimmed)) {
            confidence += 0.6
        }

        // Fractions like 1/2, 3/4
        if (FRACTION_PATTERN.containsMatchIn(trimmed)) {
            confidence += 0.3
        }

        // Bare number at start (e.g., "2 eggs", "3 בצלים")
        if (BARE_NUMBER_START.containsMatchIn(trimmed)) {
            confidence += 0.35
        } else if (trimmed.firstOrNull()?.isDigit() == true && !NUMBERED_LINE_PATTERN.containsMatchIn(trimmed)) {
            confidence += 0.2
        }

        // Unicode fractions (½, ¼, ¾, etc.)
        if (trimmed.any { it in "½¼¾⅓⅔⅛⅜⅝⅞" }) {
            confidence += 0.3
        }

        // Bullet/dash/asterisk list markers strongly suggest ingredient
        if (LIST_MARKER_PATTERN.containsMatchIn(trimmed)) {
            confidence += 0.25
        }

        // Short lines are more likely ingredients
        if (trimmed.length < 60) {
            confidence += 0.1
        }

        // Contains known food words
        val lowerTrimmed = trimmed.lowercase()
        val foodWords = when (language) {
            Language.EN -> EN_FOOD_WORDS
            Language.HE -> HE_FOOD_WORDS
            Language.UNKNOWN -> EN_FOOD_WORDS + HE_FOOD_WORDS
        }
        if (foodWords.any { lowerTrimmed.contains(it) }) {
            confidence += 0.3
        }

        // Cooking verbs suggest this is a step, not ingredient
        if (startsWithCookingVerb(trimmed, language)) {
            confidence -= 0.3
        }

        // Long lines are less likely to be ingredients
        if (trimmed.length > 80) {
            confidence -= 0.2
        }

        return confidence.coerceIn(0.0, 1.0)
    }

    fun stepConfidence(line: String, language: Language): Double {
        val trimmed = line.trim()
        if (trimmed.isBlank()) return 0.0

        var confidence = 0.0

        if (NUMBERED_LINE_PATTERN.containsMatchIn(trimmed)) {
            confidence += 0.5
        }

        if (startsWithCookingVerb(trimmed, language)) {
            confidence += 0.6
        }

        // Longer lines (sentences) are more likely steps
        if (trimmed.length > 40) {
            confidence += 0.15
        }
        if (trimmed.length > 80) {
            confidence += 0.1
        }

        // Lines ending with period suggest prose/instructions
        if (trimmed.endsWith(".") || trimmed.endsWith("。")) {
            confidence += 0.1
        }

        // Measurement units suggest ingredient, not step
        if (EN_UNITS_PATTERN.containsMatchIn(trimmed) || HE_UNITS_PATTERN.containsMatchIn(trimmed)) {
            confidence -= 0.2
        }

        // Bullet/list markers without cooking verbs suggest ingredient
        if (LIST_MARKER_PATTERN.containsMatchIn(trimmed) && !startsWithCookingVerb(trimmed, language)) {
            confidence -= 0.15
        }

        return confidence.coerceIn(0.0, 1.0)
    }

    private fun startsWithCookingVerb(line: String, language: Language): Boolean {
        val trimmed = line.trim()
        val textToCheck = if (NUMBERED_LINE_PATTERN.containsMatchIn(trimmed)) {
            trimmed.replace(Regex("""^\s*\d+[\s.):\-]+"""), "").trim()
        } else {
            trimmed
        }
        val firstWord = textToCheck.split(Regex("\\s+")).firstOrNull()?.lowercase() ?: return false

        val verbs = when (language) {
            Language.EN -> EN_COOKING_VERBS
            Language.HE -> HE_COOKING_VERBS
            Language.UNKNOWN -> EN_COOKING_VERBS + HE_COOKING_VERBS
        }
        return verbs.any { firstWord == it || firstWord.startsWith(it) }
    }
}
