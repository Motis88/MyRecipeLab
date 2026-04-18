package com.example.recipemanager.core.util

object IngredientScaler {

    private val NUMBER_PATTERN = Regex("""(\d+(?:[./]\d+)?)""")
    
    private val UNICODE_FRACTIONS = mapOf(
        '½' to 0.5f,
        '¼' to 0.25f,
        '¾' to 0.75f,
        '⅓' to 1f / 3f,
        '⅔' to 2f / 3f,
        '⅛' to 0.125f,
        '⅜' to 0.375f,
        '⅝' to 0.625f,
        '⅞' to 0.875f
    )

    fun scaleText(text: String, factor: Float): String {
        if (factor == 1f) return text
        
        // First replace unicode fractions
        var result = text
        UNICODE_FRACTIONS.forEach { (char, value) ->
            if (result.contains(char)) {
                val scaled = value * factor
                result = result.replace(char.toString(), formatNumber(scaled))
            }
        }
        
        // Then replace regular numbers
        result = NUMBER_PATTERN.replace(result) { match ->
            val original = match.groupValues[1]
            val value = parseNumber(original) ?: return@replace match.value
            formatNumber(value * factor)
        }
        
        return result
    }

    private fun parseNumber(s: String): Float? {
        if (s.contains('/')) {
            val parts = s.split('/')
            if (parts.size == 2) {
                val num = parts[0].toFloatOrNull() ?: return null
                val den = parts[1].toFloatOrNull() ?: return null
                if (den == 0f) return null
                return num / den
            }
            return null
        }
        return s.toFloatOrNull()
    }

    private fun formatNumber(value: Float): String {
        if (value == value.toLong().toFloat() && value < 10000) {
            return value.toLong().toString()
        }
        // Show nice fractions for common values
        val quarter = (value * 4).toLong()
        if ((quarter.toFloat() / 4f - value).let { it > -0.01f && it < 0.01f }) {
            return when (quarter % 4) {
                1L -> if (quarter / 4 > 0) "${quarter / 4}¼" else "¼"
                2L -> if (quarter / 4 > 0) "${quarter / 4}½" else "½"
                3L -> if (quarter / 4 > 0) "${quarter / 4}¾" else "¾"
                else -> (quarter / 4).toString()
            }
        }
        val rounded = (value * 100).toLong() / 100.0
        return if (rounded == rounded.toLong().toDouble()) {
            rounded.toLong().toString()
        } else {
            "%.2g".format(rounded)
        }
    }
}
