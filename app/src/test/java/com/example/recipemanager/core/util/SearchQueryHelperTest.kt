package com.example.recipemanager.core.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SearchQueryHelperTest {

    @Test
    fun `simple single word gets prefix wildcard`() {
        assertThat(SearchQueryHelper.escapeFtsQuery("cake")).isEqualTo("cake*")
    }

    @Test
    fun `multi-word query has wildcard per token`() {
        assertThat(SearchQueryHelper.escapeFtsQuery("chocolate chip")).isEqualTo("chocolate* chip*")
    }

    @Test
    fun `blank input returns empty string`() {
        assertThat(SearchQueryHelper.escapeFtsQuery("")).isEmpty()
        assertThat(SearchQueryHelper.escapeFtsQuery("   ")).isEmpty()
    }

    @Test
    fun `special FTS characters are stripped`() {
        assertThat(SearchQueryHelper.escapeFtsQuery("test\"query")).isEqualTo("test* query*")
    }

    @Test
    fun `parentheses and brackets are stripped`() {
        assertThat(SearchQueryHelper.escapeFtsQuery("(test) [query]")).isEqualTo("test* query*")
    }

    @Test
    fun `asterisks are stripped`() {
        assertThat(SearchQueryHelper.escapeFtsQuery("test*")).isEqualTo("test*")
    }

    @Test
    fun `multiple spaces collapse to single separator`() {
        assertThat(SearchQueryHelper.escapeFtsQuery("  hello    world  ")).isEqualTo("hello* world*")
    }

    @Test
    fun `Hebrew text gets prefix wildcard`() {
        assertThat(SearchQueryHelper.escapeFtsQuery("עוגה")).isEqualTo("עוגה*")
    }

    @Test
    fun `mixed Hebrew and English tokens`() {
        assertThat(SearchQueryHelper.escapeFtsQuery("chocolate שוקולד")).isEqualTo("chocolate* שוקולד*")
    }
}
