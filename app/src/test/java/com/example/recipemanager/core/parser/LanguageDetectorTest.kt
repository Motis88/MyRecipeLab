package com.example.recipemanager.core.parser

import com.example.recipemanager.core.model.Language
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class LanguageDetectorTest {

    private lateinit var detector: LanguageDetector

    @Before
    fun setup() {
        detector = LanguageDetector()
    }

    @Test
    fun `pure English text is detected as EN`() {
        assertThat(detector.detect("This is an English sentence with words")).isEqualTo(Language.EN)
    }

    @Test
    fun `pure Hebrew text is detected as HE`() {
        assertThat(detector.detect("זהו משפט בעברית עם מילים רבות")).isEqualTo(Language.HE)
    }

    @Test
    fun `mixed text with majority Hebrew is HE`() {
        // ~80% Hebrew chars
        val text = "עוגת שוקולד טעימה מאוד recipe"
        assertThat(detector.detect(text)).isEqualTo(Language.HE)
    }

    @Test
    fun `mixed text with majority English is EN`() {
        val text = "This is a chocolate cake recipe with some שוקולד"
        assertThat(detector.detect(text)).isEqualTo(Language.EN)
    }

    @Test
    fun `empty string is UNKNOWN`() {
        assertThat(detector.detect("")).isEqualTo(Language.UNKNOWN)
    }

    @Test
    fun `only numbers and symbols is UNKNOWN`() {
        assertThat(detector.detect("123 456 !@#")).isEqualTo(Language.UNKNOWN)
    }

    @Test
    fun `whitespace only is UNKNOWN`() {
        assertThat(detector.detect("   \n\t  ")).isEqualTo(Language.UNKNOWN)
    }

    @Test
    fun `single English word is EN`() {
        assertThat(detector.detect("hello")).isEqualTo(Language.EN)
    }

    @Test
    fun `single Hebrew word is HE`() {
        assertThat(detector.detect("שלום")).isEqualTo(Language.HE)
    }

    @Test
    fun `balanced mix with Hebrew detects as HE due to lower threshold`() {
        // Build a string with approximately equal Hebrew and English
        val he = "אבגדהוזחט"  // 9 Hebrew chars
        val en = "abcdefghi"  // 9 English chars
        // Hebrew ratio is 0.5 which exceeds 0.3 threshold
        assertThat(detector.detect("$he $en")).isEqualTo(Language.HE)
    }
}
