package com.sososoftware.hangman.guess

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class Guess(val letter: Char?, val word: String?, val type: GuessType) {
    enum class GuessType {
        LETTER_GUESS,
        WORD_GUESS,
        GIVE_UP,
        THINKING
    }

    companion object {
        fun thinking() = Guess(null, null, GuessType.THINKING)
        private fun giveUp() = Guess(null, null, GuessType.GIVE_UP)
        private fun guessLetter(letter: Char) = Guess(letter, null, GuessType.LETTER_GUESS)
        private fun guessWord(word: String) = Guess(null, word, GuessType.WORD_GUESS)

        suspend fun generateGuess(
            wordlist: List<String>?,
            guessedLetters: Set<Char>,
            prompt: List<Char?>,
            algorithm: String
        ): Guess =
            withContext(Dispatchers.Default) {
                when {
                    wordlist == null || wordlist.isEmpty() -> thinking()
                    prompt.isFinishedPrompt() -> guessWord(prompt.joinToString(""))
                    else -> {
                        val wrongLetters = guessedLetters - prompt.filterNotNull().toSet()
                        val validWords = wordlist.filter {
                            isPossibleWord(it, prompt, guessedLetters, wrongLetters)
                        }
                        when (validWords.size) {
                            0 -> giveUp()
                            1 -> guessWord(validWords.first())
                            else -> {
                                if (algorithm == "clever")
                                    cleverGuess(('A'..'Z') - guessedLetters, validWords)
                                else
                                    bestGuess(('A'..'Z') - guessedLetters, validWords)
                            }
                        }
                    }
                }
            }

        private fun cleverGuess(guessableLetters: List<Char>, words: List<String>): Guess {
            return when (guessableLetters.size) {
                0 -> giveUp()
                1 -> guessLetter(guessableLetters.first())
                else -> {
                    val halfWords = kotlin.math.max(1, words.size / 2)
                    val letterCounts = guessableLetters.map{ letter ->
                        Pair(letter, words.filter{it.contains(letter)}.size)
                    }
                    val letter = letterCounts.minByOrNull{ kotlin.math.abs(it.second - halfWords) }?.first
                    return letter?.let { guessLetter(it) } ?: giveUp()
                }
            }
        }

        private fun bestGuess(guessableLetters: List<Char>, words: List<String>): Guess {
            return when (guessableLetters.size) {
                0 -> giveUp()
                1 -> guessLetter(guessableLetters.first())
                else -> {
                    val letterCounts = guessableLetters.map{ letter ->
                        Pair(letter, words.filter{it.contains(letter)}.size)
                    }
                    val letter = letterCounts.maxByOrNull{ it.second }?.first
                    return letter?.let { guessLetter(it) } ?: giveUp()
                }
            }
        }

        private fun isPossibleWord(
            candidate: String,
            prompt: List<Char?>,
            guessedLetters: Set<Char>,
            wrongLetters: Set<Char>
        ): Boolean {
            return if (candidate.containsAny(wrongLetters) || prompt.size != candidate.length) {
                false
            } else {
                candidate.toList().zip(prompt).map {(wordLetter: Char, promptLetter: Char?) ->
                    if (promptLetter == null)
                        wordLetter !in guessedLetters
                    else
                        wordLetter == promptLetter
                }.all{it}
            }
        }

        private fun String.containsAny(chars: Collection<Char>): Boolean =
            chars.map{this.contains(it)}.any{it}

        private fun List<Char?>.isFinishedPrompt() = this.all { it != null }
    }
}