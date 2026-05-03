package com.example.recipemanager.core.util

import com.example.recipemanager.core.model.GroceryCategory
import com.example.recipemanager.core.model.GroceryItem
import kotlin.math.abs

/**
 * Aggregates a list of ingredient text strings into a categorized grocery list.
 *
 * Steps:
 *  1. Parse each ingredient text into (qty, unit, name).
 *  2. Normalize names; group entries with the same name + unit.
 *  3. Sum quantities within each group.
 *  4. Classify into [GroceryCategory].
 *  5. Return sorted by category order, then alphabetically.
 */
object GroceryAggregator {

    // --- Produce ---
    private val PRODUCE_KEYWORDS = setOf(
        "apple", "apples", "banana", "bananas", "orange", "oranges", "lemon", "lemons",
        "lime", "limes", "tomato", "tomatoes", "cherry tomato", "cherry tomatoes",
        "potato", "potatoes", "sweet potato", "onion", "onions", "garlic",
        "carrot", "carrots", "celery", "spinach", "lettuce", "zucchini", "cucumber",
        "mushroom", "mushrooms", "pepper", "peppers", "bell pepper", "chili", "chili pepper",
        "broccoli", "cauliflower", "cabbage", "kale", "corn", "avocado", "eggplant",
        "asparagus", "artichoke", "beet", "beets", "fennel", "leek", "scallion",
        "shallot", "radish", "turnip", "squash", "pumpkin", "parsley", "cilantro",
        "basil", "mint", "dill", "thyme", "rosemary", "sage", "chives",
        // Hebrew
        "עגבנייה", "עגבניות", "תפוח אדמה", "תפוחי אדמה", "בצל", "בצלים", "שום",
        "גזר", "סלרי", "תרד", "חסה", "קישוא", "מלפפון", "פטריות", "פלפל", "פלפלים",
        "ברוקולי", "כרובית", "כרוב", "תירס", "אבוקדו", "חציל", "חצילים",
        "אספרגוס", "סלק", "דלעת", "בטטה", "פטרוזיליה", "כוסברה", "בזיליקום",
        "נענע", "שמיר", "עלי תרד", "ליים", "לימון", "לימונים", "תפוח", "תפוחים",
        "בננה", "תפוז", "תפוזים"
    )

    // --- Dairy ---
    private val DAIRY_KEYWORDS = setOf(
        "milk", "whole milk", "skim milk", "butter", "cream", "heavy cream",
        "sour cream", "cheese", "cheddar", "mozzarella", "parmesan", "feta",
        "ricotta", "cream cheese", "cottage cheese", "yogurt", "greek yogurt",
        "egg", "eggs", "half-and-half", "ghee", "whipping cream",
        // Hebrew
        "חלב", "חמאה", "שמנת", "גבינה", "גבינה צהובה", "גבינה לבנה", "קוטג'",
        "יוגורט", "ביצה", "ביצים", "שמנת חמוצה", "ריקוטה", "מוצרלה", "פרמזן",
        "גבינת פטה"
    )

    // --- Meat ---
    private val MEAT_KEYWORDS = setOf(
        "chicken", "beef", "pork", "lamb", "turkey", "veal", "duck",
        "ground beef", "ground chicken", "chicken breast", "chicken thigh",
        "chicken wing", "chicken leg", "steak", "bacon", "ham", "sausage",
        "pepperoni", "salami", "prosciutto", "chorizo",
        // Hebrew
        "עוף", "בקר", "כבש", "טורקיה", "חזה עוף", "שוק עוף", "כנפיים",
        "בשר טחון", "סטייק", "נקניק", "בייקון", "פסטרמה"
    )

    // --- Seafood ---
    private val SEAFOOD_KEYWORDS = setOf(
        "fish", "salmon", "tuna", "cod", "tilapia", "halibut", "sea bass",
        "shrimp", "prawn", "crab", "lobster", "clam", "oyster", "mussel",
        "squid", "octopus", "sardine", "anchovy",
        // Hebrew
        "דג", "סלמון", "טונה", "שרימפס", "צדפה", "קלמרי"
    )

    // --- Dry goods ---
    private val DRY_KEYWORDS = setOf(
        "flour", "sugar", "brown sugar", "powdered sugar", "salt", "rice",
        "pasta", "noodle", "noodles", "oat", "oats", "quinoa", "lentil", "lentils",
        "bean", "beans", "chickpea", "chickpeas", "black bean", "kidney bean",
        "cornstarch", "baking powder", "baking soda", "yeast", "cocoa",
        "chocolate", "dark chocolate", "white chocolate", "chip", "chips",
        "bread crumb", "bread crumbs", "panko", "cracker", "crackers",
        "cereal", "granola", "nut", "nuts", "almond", "almonds", "walnut", "walnuts",
        "cashew", "peanut", "peanuts", "pecan", "hazelnut", "pine nut", "pine nuts",
        "sesame", "flaxseed", "chia", "sunflower seed",
        "oil", "olive oil", "vegetable oil", "canola oil", "coconut oil",
        "vinegar", "soy sauce", "honey", "maple syrup", "molasses",
        "broth", "stock", "bouillon", "tomato paste", "tomato sauce",
        // Hebrew
        "קמח", "סוכר", "סוכר חום", "מלח", "אורז", "פסטה", "שיבולת שועל", "עדשים",
        "שעועית", "חומוס", "קורנפלור", "אבקת אפייה", "סודה לשתייה", "שמרים",
        "שוקולד", "קקאו", "שמן", "שמן זית", "שמן קנולה", "חומץ", "רוטב סויה",
        "דבש", "סירופ מייפל", "ממרח", "ציר", "מרק", "רסק עגבניות",
        "אגוזים", "שקדים", "אגוזי מלך", "גרעינים", "שומשום", "פירורי לחם"
    )

    // --- Spices ---
    private val SPICE_KEYWORDS = setOf(
        "pepper", "black pepper", "white pepper", "cayenne", "paprika",
        "cumin", "coriander", "turmeric", "cinnamon", "nutmeg", "clove", "cloves",
        "cardamom", "allspice", "oregano", "basil", "bay leaf", "bay leaves",
        "chili powder", "curry powder", "garam masala", "za'atar", "sumac",
        "vanilla", "vanilla extract", "saffron", "anise", "star anise",
        // Hebrew
        "פלפל שחור", "פלפל לבן", "קיין", "פפריקה", "כמון", "כוסברה", "כורכום",
        "קינמון", "גוז מוסקט", "קרדמום", "אורגנו", "עלה דפנה", "זעתר", "סומק",
        "וניל", "תמצית וניל", "כרכום", "אניס"
    )

    // --- Bakery ---
    private val BAKERY_KEYWORDS = setOf(
        "bread", "loaf", "roll", "rolls", "baguette", "pita", "tortilla",
        "wrap", "bagel", "muffin", "croissant", "focaccia",
        // Hebrew
        "לחם", "פיתה", "לחמניה", "בגל", "מאפין", "קרואסון"
    )

    // --- Frozen ---
    private val FROZEN_KEYWORDS = setOf(
        "frozen", "ice cream", "gelato", "sorbet", "frozen peas", "frozen corn",
        "frozen spinach",
        // Hebrew
        "קפוא", "גלידה", "סורבה"
    )

    // --- Beverages ---
    private val BEVERAGE_KEYWORDS = setOf(
        "water", "juice", "orange juice", "apple juice", "wine", "red wine",
        "white wine", "beer", "coffee", "tea", "milk" /* already in dairy; tie-break fine */,
        "coconut milk", "almond milk", "oat milk",
        // Hebrew
        "מים", "מיץ", "יין", "בירה", "קפה", "תה", "חלב קוקוס", "חלב שקדים"
    )

    private fun classify(name: String): GroceryCategory {
        val lower = name.lowercase()
        // Order matters: more specific first
        if (FROZEN_KEYWORDS.any { lower.contains(it) }) return GroceryCategory.FROZEN
        if (SEAFOOD_KEYWORDS.any { lower.contains(it) }) return GroceryCategory.SEAFOOD
        if (MEAT_KEYWORDS.any { lower.contains(it) }) return GroceryCategory.MEAT
        if (DAIRY_KEYWORDS.any { lower.contains(it) }) return GroceryCategory.DAIRY
        if (SPICE_KEYWORDS.any { lower.contains(it) }) return GroceryCategory.SPICES
        if (BAKERY_KEYWORDS.any { lower.contains(it) }) return GroceryCategory.BAKERY
        if (PRODUCE_KEYWORDS.any { lower.contains(it) }) return GroceryCategory.PRODUCE
        if (BEVERAGE_KEYWORDS.any { lower.contains(it) }) return GroceryCategory.BEVERAGES
        if (DRY_KEYWORDS.any { lower.contains(it) }) return GroceryCategory.DRY_GOODS
        return GroceryCategory.OTHER
    }

    /**
     * Aggregate [ingredientTexts] into a sorted list of [GroceryItem].
     *
     * Items with the same normalized name and unit are merged by summing quantities.
     * Items without a parseable quantity carry qty=0.0.
     */
    fun aggregate(ingredientTexts: List<String>): List<GroceryItem> {
        data class Key(val name: String, val unit: String)

        val accumulator = mutableMapOf<Key, Pair<Double, GroceryCategory>>()

        for (text in ingredientTexts) {
            if (text.isBlank()) continue
            val parsed = IngredientParser.parse(text)
            val normalName = IngredientParser.normalizeName(parsed.name).ifBlank {
                IngredientParser.normalizeName(text)
            }
            if (normalName.isBlank()) continue

            val key = Key(normalName, parsed.unit)
            val existing = accumulator[key]
            val category = classify(normalName)
            val newQty = (existing?.first ?: 0.0) + parsed.qty
            accumulator[key] = Pair(newQty, existing?.second ?: category)
        }

        return accumulator.map { (key, value) ->
            GroceryItem(
                name = key.name.replaceFirstChar { it.uppercase() },
                totalQty = value.first,
                unit = key.unit,
                category = value.second
            )
        }.sortedWith(compareBy({ it.category.order }, { it.name }))
    }

    /** Group a flat [GroceryItem] list by category, preserving item order within each group. */
    fun groupByCategory(items: List<GroceryItem>): Map<GroceryCategory, List<GroceryItem>> =
        items.groupBy { it.category }
            .toSortedMap(compareBy { it.order })

    /**
     * Formats a [GroceryItem] into a single display string suitable for copy/share.
     * Example: "Flour — 2 cup"
     */
    fun formatItem(item: GroceryItem): String {
        val qty = item.displayQty()
        return if (qty.isBlank()) item.name else "${item.name} — $qty"
    }

    /** Returns true if two quantities should be considered "the same" for display (within ε). */
    fun qtyApproxEqual(a: Double, b: Double, epsilon: Double = 0.001): Boolean =
        abs(a - b) < epsilon
}
