package com.sososoftware.hangman.settings

import android.content.SharedPreferences

fun SharedPreferences.getAlgorithm(): String =
    this.getString("algorithm", "good") ?: "good"

fun SharedPreferences.getWordlist(): String =
    this.getString("wordlist", "easy") ?: "easy"