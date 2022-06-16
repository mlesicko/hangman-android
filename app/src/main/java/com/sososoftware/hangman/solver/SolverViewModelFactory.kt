package com.sososoftware.hangman.solver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class SolverViewModelFactory(
    private val initialPromptLength: Int,
    private val algorithm: String
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SolverViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SolverViewModel(initialPromptLength, algorithm) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}