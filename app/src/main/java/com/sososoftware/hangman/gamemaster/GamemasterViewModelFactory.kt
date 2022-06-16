package com.sososoftware.hangman.gamemaster

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class GamemasterViewModelFactory(
    private val initialPromptLength: Int,
    private val algorithm: String
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GamemasterViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GamemasterViewModel(initialPromptLength, algorithm) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}