package com.example.recipemanager.core.util

object SearchQueryHelper {

    /**
     * Escapes FTS special characters and appends prefix-match wildcard to each token.
     * Returns empty string for blank input.
     */
    fun escapeFtsQuery(query: String): String {
        if (query.isBlank()) return ""
        return query
            .replace(Regex("""["*(){}\[\]^~\\:+\-]"""), " ")
            .trim()
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .joinToString(" ") { "$it*" }
    }
}
