package com.sososoftware.hangman.solver

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sososoftware.hangman.guess.Guess
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SolverViewModel(
    private val initialPromptLength: Int,
    private var algorithm: String
): ViewModel() {
    val state = MutableLiveData<SolverState>()
    var words: List<String> = emptyList()
        set(value) {
            field = value
            wordsMap = words.groupBy { it.length }
            updateGuess()
        }

    private var updateGuessJob: Job? = null
    private var wordsMap: Map<Int,List<String>> = emptyMap()

    init {
        state.value = SolverState(initialPromptLength)
        updateGuess()
    }

    fun resetGame() {
        val promptLength = state.value?.prompt?.size ?: initialPromptLength
        state.value = SolverState(promptLength)
        updateGuess()
    }

    fun onPromptLengthChanged(length: Int) {
        if (length != state.value?.prompt?.size) {
            state.value = SolverState(length)
            updateGuess()
        }
    }

    fun onPromptChange(newPrompt: List<Char?>) {
        state.value = state.value?.updatePrompt(newPrompt)
        updateGuess()
    }

    fun onGuess(letter: Char) {
        state.value = state.value?.addGuessedLetter(letter)
        updateGuess()
    }

    fun onLetterUnselected(letter: Char) {
        state.value = state.value?.removeGuessedLetter(letter)
        updateGuess()
    }

    fun updateAlgorithm(newAlgorithm: String) {
        if (newAlgorithm != algorithm) {
            algorithm = newAlgorithm
            updateGuess()
        }
    }

    private fun updateGuess() {
        updateGuessJob?.cancel()
        updateGuessJob = viewModelScope.launch {
            state.value?.let {
                state.value = it.withGuess(Guess.generateGuess(
                    wordsMap[it.prompt.size],
                    it.guessedLetters,
                    it.prompt,
                    algorithm
                ))
            }
        }
    }

}