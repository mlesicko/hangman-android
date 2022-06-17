package com.sososoftware.hangman.solver

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.sososoftware.hangman.gamemaster.GamemasterViewModel
import com.sososoftware.hangman.getAllWords
import com.sososoftware.hangman.guess.Guess
import com.sososoftware.hangman.settings.getAlgorithm
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SolverViewModel(
    application: Application
): AndroidViewModel(application) {
    val state = MutableLiveData<SolverState>()
    private var words: List<String> = emptyList()
        set(value) {
            field = value
            wordsMap = words.groupBy { it.length }
            updateGuess()
        }

    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application)
    private var algorithm = sharedPreferences.getAlgorithm()
    private var updateGuessJob: Job? = null
    private var wordsMap: Map<Int,List<String>> = emptyMap()

    private val onSharedPreferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener(
            fun(preferences: SharedPreferences, key: String) {
                if (key == "algorithm") {
                    algorithm = preferences.getAlgorithm()
                    updateGuess()
                }
            }
        )

    init {
        state.value = SolverState(GamemasterViewModel.DEFAULT_PROMPT_LENGTH)
        sharedPreferences.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener)
        getWords()
    }

    override fun onCleared() {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener)
        super.onCleared()
    }

    fun resetGame() {
        val promptLength = state.value?.prompt?.size ?: DEFAULT_PROMPT_LENGTH
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

    fun updateGuess() {
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

    private fun getWords() {
        viewModelScope.launch {
            words = getAllWords(getApplication<Application>().resources)
        }
    }

    companion object {
        const val DEFAULT_PROMPT_LENGTH = 6
    }
}