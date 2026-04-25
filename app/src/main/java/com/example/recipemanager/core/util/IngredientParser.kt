package com.example.recipemanager.core.util

/**
 * Parses a free-text ingredient line (e.g. "2 cups flour", "½ tsp salt", "3 ביצים")
 * into its constituent parts: quantity, unit, and ingredient name.
 *
 * The parser intentionally uses simple regex so it is fast, offline, and has no deps.
 */
object IngredientParser {

    // Canonical unit aliases → normalized form
    private val UNIT_MAP: Map<Regex, String> = linkedMapOf(
        Regex("""(?i)\b(tablespoons?|tbsps?|tbs)\b""") to "tbsp",
        Regex("""(?i)\b(teaspoons?|tsps?)\b""") to "tsp",
        Regex("""(?i)\b(cups?)\b""") to "cup",
        Regex("""(?i)\b(ounces?|ozs?)\b""") to "oz",
        Regex("""(?i)\b(pounds?|lbs?)\b""") to "lb",
        Regex("""(?i)\b(kilograms?|kgs?|ק"ג)\b""") to "kg",
        Regex("""(?i)\b(grams?|grs?)\b""") to "g",
        Regex("""(?i)\b(millilit(?:re|er)s?|mls?|מ"ל)\b""") to "ml",
        Regex("""(?i)\b(lit(?:re|er)s?|ls?)\b""") to "l",
        Regex("""(?i)\b(כוסות?)\b""") to "cup",
        Regex("""(?i)\b(כפות?)\b""") to "tbsp",
        Regex("""(?i)\b(כפיות?)\b""") to "tsp",
        Regex("""(?i)\b(גרם)\b""") to "g",
        Regex("""(?i)\b(ליטר)\b""") to "l",
        Regex("""(?i)\b(pinch(?:es)?)\b""") to "pinch",
        Regex("""(?i)\b(dash(?:es)?)\b""") to "dash",
        Regex("""(?i)\b(bunch(?:es)?)\b""") to "bunch",
        Regex("""(?i)\b(cloves?)\b""") to "clove",
        Regex("""(?i)\b(slices?)\b""") to "slice",
        Regex("""(?i)\b(pieces?|pcs?)\b""") to "piece",
        Regex("""(?i)\b(cans?)\b""") to "can",
        Regex("""(?i)\b(packages?|pkgs?)\b""") to "package",
        Regex("""(?i)\b(sticks?)\b""") to "stick"
    )

    private val UNICODE_FRACTIONS: Map<Char, Double> = mapOf(
        '½' to 0.5, '¼' to 0.25, '¾' to 0.75,
        '⅓' to 1.0 / 3, '⅔' to 2.0 / 3,
        '⅛' to 0.125, '⅜' to 0.375, '⅝' to 0.625, '⅞' to 0.875
    )

    // Matches leading "2", "1/2", "1 1/2", "½" etc.
    private val QTY_PATTERN = Regex(
        """^\s*(\d+)\s+(\d+)\s*/\s*(\d+)|""" + // "1 1/2"
        """^\s*(\d+)\s*/\s*(\d+)|""" +          // "1/2"
        """^\s*(\d+(?:[.,]\d+)?)|""" +           // "2" or "2.5"
        """^\s*([½¼¾⅓⅔⅛⅜⅝⅞])"""                // unicode fraction
    )

    data class ParsedIngredient(
        val qty: Double,
        val unit: String,
        val name: String
    )

    fun parse(text: String): ParsedIngredient {
        // Strip bullet/dash/number prefix
        val cleaned = text.trim()
            .replace(Regex("""^[-–—•*·▪]\s*"""), "")
            .replace(Regex("""^\d+[\s.):\-]+"""), "")
            .trim()

        // Try to extract quantity
        val (qty, afterQty) = extractQty(cleaned)

        // Try to extract unit
        val (unit, afterUnit) = extractUnit(afterQty.trim())

        // Remainder is the name
        val name = afterUnit.trim()
            .replace(Regex("""^,\s*"""), "")
            .trim()

        return ParsedIngredient(qty = qty, unit = unit, name = name.ifBlank { cleaned })
    }

    /** Returns (quantity value, remaining string after quantity). */
    private fun extractQty(text: String): Pair<Double, String> {
        val match = QTY_PATTERN.find(text) ?: return Pair(0.0, text)
        val groups = match.groupValues

        val qty = when {
            groups[1].isNotEmpty() -> {
                // "1 1/2"
                val whole = groups[1].toDoubleOrNull() ?: 0.0
                val num = groups[2].toDoubleOrNull() ?: 0.0
                val den = groups[3].toDoubleOrNull() ?: 1.0
                whole + (num / den)
            }
            groups[4].isNotEmpty() -> {
                // "1/2"
                val num = groups[4].toDoubleOrNull() ?: 0.0
                val den = groups[5].toDoubleOrNull() ?: 1.0
                num / den
            }
            groups[6].isNotEmpty() -> groups[6].replace(',', '.').toDoubleOrNull() ?: 0.0
            groups[7].isNotEmpty() -> UNICODE_FRACTIONS[groups[7].first()] ?: 0.0
            else -> 0.0
        }

        return Pair(qty, text.substring(match.value.length))
    }

    /** Returns (normalized unit, remaining string after unit). */
    private fun extractUnit(text: String): Pair<String, String> {
        for ((pattern, canonical) in UNIT_MAP) {
            val match = pattern.find(text) ?: continue
            // Only accept units at the start of the remaining text
            if (text.substring(0, match.range.first).isBlank()) {
                return Pair(canonical, text.substring(match.range.last + 1))
            }
        }
        return Pair("", text)
    }

    /** Normalizes a name for grouping: lowercase, trimmed, removes parenthetical notes. */
    fun normalizeName(name: String): String = name
        .lowercase()
        .replace(Regex("""\(.*?\)"""), "")  // strip "(optional)", etc.
        .replace(Regex(""",.*$"""), "")      // strip ", divided" etc.
        .trim()
        .replace(Regex("""\s+"""), " ")
}
