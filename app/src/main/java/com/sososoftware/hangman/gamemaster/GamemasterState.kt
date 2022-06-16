package com.sososoftware.hangman.gamemaster

import com.sososoftware.hangman.guess.Guess

data class GamemasterState (
    var prompt: List<Char?>,
    var guessedLetters: Set<Char> = emptySet(),
    var guess: Guess? = null
) {
    constructor(promptLength: Int) : this(arrayOfNulls<Char?>(promptLength).toList())

    fun withGuess(guess: Guess?) = GamemasterState(prompt, guessedLetters, guess)

    fun updatePrompt(newPrompt: List<Char?>) =
        GamemasterState(
            newPrompt,
            guessedLetters,
            guess
        )

    fun addGuessedLetter(letter: Char) =
        GamemasterState(prompt, guessedLetters + letter, Guess.thinking())

    val wrongLetterCount
        get() = guessedLetters.filter { it !in prompt }.size

    val dead
        get() = wrongLetterCount > 6
}
