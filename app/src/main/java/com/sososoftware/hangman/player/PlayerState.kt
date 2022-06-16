package com.sososoftware.hangman.player

data class PlayerState (val wordToGuess: String, val guessedLetters: Set<Char> = emptySet()) {
    fun guessLetter(letter: Char): PlayerState =
        PlayerState(wordToGuess, guessedLetters.plus(letter))

    fun wasGuessed(letter: Char): Boolean = letter in guessedLetters
    fun inWord(letter: Char): Boolean = letter in wordToGuess
    val wrongGuessCount
        get() = guessedLetters.filter { !inWord(it) }.size
}
