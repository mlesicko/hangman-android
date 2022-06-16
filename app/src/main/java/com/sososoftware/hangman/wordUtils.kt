package com.sososoftware.hangman

import android.content.res.Resources
import androidx.annotation.RawRes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

fun getEasyPlayerWords(res: Resources): List<String> = getWords(res, R.raw.common_words)

fun getHardPlayerWords(res: Resources): List<String> = getWords(res, R.raw.treegle_words)

fun getBothPlayerWords(res: Resources): Collection<String> =
    getEasyPlayerWords(res).toSet() + getHardPlayerWords(res).toSet()

/**
 * Get the words from the complete English word list.
 * This needs to be done in a coroutine because there are so many words that it
 * can cause a noticeable performance hiccup when loading them all if it's done
 * on the UI thread.
 */
suspend fun getAllWords(resources: Resources): List<String> =
    withContext(Dispatchers.Default) { AllWordsSingleton.getWords(resources) }

fun getWords(resources: Resources, @RawRes id: Int): List<String> {
    val inputStream = resources.openRawResource(id)
    val inputText = inputStream.bufferedReader().use { it.readText() }
    return inputText.split('\n').map { it.trim().uppercase(Locale.getDefault()) }
}

private object AllWordsSingleton {
    private var wordsCache: List<String> = emptyList()
    fun getWords(resources: Resources): List<String> {
        if(wordsCache.isEmpty()) {
            wordsCache = getWords(resources, R.raw.hangman_words)
        }
        return wordsCache
    }
}