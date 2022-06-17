package com.sososoftware.hangman.solver

import com.sososoftware.hangman.guess.Guess

data class SolverState (
    var prompt: List<Char?>,
    var guessedLetters: Set<Char> = emptySet(),
    var guess: Guess = Guess.thinking()
) {
    constructor(promptLength: Int) : this(arrayOfNulls<Char?>(promptLength).toList())

    fun withGuess(guess: Guess) = SolverState(prompt, guessedLetters, guess)

    fun updatePrompt(newPrompt: List<Char?>) =
        SolverState(
            newPrompt,
            guessedLetters + newPrompt.filterNotNull(),
            Guess.thinking()
        )

    fun addGuessedLetter(letter: Char) =
        SolverState(prompt, guessedLetters + letter, Guess.thinking())

    fun removeGuessedLetter(letter: Char): SolverState =
        if (letter !in prompt) {
            SolverState(prompt, guessedLetters - letter, Guess.thinking())
        } else {
            this
        }
}
