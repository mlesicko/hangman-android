package com.sososoftware.hangman.gamemaster

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class GamemasterViewModelFactory(
    private val application: Application
): ViewModelProvider.AndroidViewModelFactory (application) {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GamemasterViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GamemasterViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}