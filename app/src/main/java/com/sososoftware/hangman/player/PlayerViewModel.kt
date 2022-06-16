package com.sososoftware.hangman.player

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PlayerViewModel(private var words: Collection<String>): ViewModel() {

    val state = MutableLiveData<PlayerState>()

    init {
        state.value = PlayerState(getWord())
    }

    fun resetGame() {
        state.value = PlayerState(getWord())
    }

    fun onGuess(letter: Char) {
        state.value = state.value?.guessLetter(letter)
    }

    fun updateWords(newWords: Collection<String>) {
        words = newWords
        resetGame()
    }

    private fun getWord(): String = words.random()
}