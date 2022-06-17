package com.sososoftware.hangman.solver

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class SolverViewModelFactory(
    private val application: Application
): ViewModelProvider.AndroidViewModelFactory (application) {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SolverViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SolverViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}