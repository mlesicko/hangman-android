package com.sososoftware.hangman.player

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.sososoftware.hangman.getAllWords
import com.sososoftware.hangman.getBothPlayerWords
import com.sososoftware.hangman.getEasyPlayerWords
import com.sososoftware.hangman.getHardPlayerWords
import com.sososoftware.hangman.settings.getAlgorithm
import com.sososoftware.hangman.settings.getWordlist
import kotlinx.coroutines.launch

class PlayerViewModel(application: Application): AndroidViewModel(application) {

    val state = MutableLiveData<PlayerState>()

    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application)
    private var words: Collection<String> = emptyList()
        set(value) {
            field = value
            resetGame()
        }

    private val onSharedPreferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener(
            fun(preferences: SharedPreferences, key: String) {
                if (key == "wordlist") {
                    getWordsForWordList(preferences.getWordlist())
                }
            }
        )

    init {
        state.value = PlayerState(getWord())
        sharedPreferences.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener)
        getWordsForWordList(sharedPreferences.getWordlist())
    }

    override fun onCleared() {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener)
        super.onCleared()
    }

    fun resetGame() {
        state.value = PlayerState(getWord())
    }

    fun onGuess(letter: Char) {
        state.value = state.value?.guessLetter(letter)
    }

    private fun getWord(): String = words.let { if (it.isEmpty()) "" else it.random() }

    private fun getWordsForWordList(wordlist: String) {
        val resources = getApplication<Application>().resources
        viewModelScope.launch {
            words = when (wordlist) {
                "easy" -> getEasyPlayerWords(resources)
                "hard" -> getHardPlayerWords(resources)
                "both" -> getBothPlayerWords(resources)
                else -> listOf()
            }
        }
    }
}