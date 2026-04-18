package com.example.recipemanager.core.util

object IngredientScaler {

    private val NUMBER_PATTERN = Regex("""(\d+(?:[./]\d+)?)""")

    fun scaleText(text: String, factor: Float): String {
        if (factor == 1f) return text
        return NUMBER_PATTERN.replace(text) { match ->
            val original = match.groupValues[1]
            val value = parseNumber(original) ?: return@replace match.value
            formatNumber(value * factor)
        }
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
        val rounded = (value * 100).toLong() / 100.0
        return if (rounded == rounded.toLong().toDouble()) {
            rounded.toLong().toString()
        } else {
            "%.2g".format(rounded)
        }
    }
}
