package com.example.recipemanager.core.parser

import com.example.recipemanager.core.model.Language

class LanguageDetector {

    fun detect(text: String): Language {
        val hebrewChars = text.count { it in '\u0590'..'\u05FF' }
        val englishChars = text.count { it.isLetter() && it < '\u0590' }
        val total = hebrewChars + englishChars

        if (total == 0) return Language.UNKNOWN

        val heRatio = hebrewChars.toDouble() / total
        val enRatio = englishChars.toDouble() / total

        return when {
            heRatio > 0.3 -> Language.HE
            enRatio > 0.5 -> Language.EN
            else -> Language.UNKNOWN
        }
    }
}
