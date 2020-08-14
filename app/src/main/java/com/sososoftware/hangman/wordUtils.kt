package com.sososoftware.hangman

import android.content.res.Resources
import androidx.annotation.RawRes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

fun getPlayerWords(resources: Resources): List<String> = getWords(resources, R.raw.common_words)

fun getDifficultWords(resources: Resources): List<String> = getWords(resources, R.raw.treegle_words)

/**
 * Get the words from the complete English word list.
 * This needs to be done in a coroutine because there are so many words that it
 * can cause a noticeable performance hiccup when loading them all if it's done
 * on the UI thread.
 */
suspend fun getGamemasterWords(resources: Resources): List<String> =
    withContext(Dispatchers.Default) { getWords(resources, R.raw.hangman_words) }

fun getWords(resources: Resources, @RawRes id: Int): List<String> {
    val inputStream = resources.openRawResource(id)
    val inputText = inputStream.bufferedReader().use { it.readText() }
    return inputText.split('\n').map { it.trim().toUpperCase(Locale.getDefault()) }
}