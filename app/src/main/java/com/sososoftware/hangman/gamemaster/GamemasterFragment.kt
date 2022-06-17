package com.sososoftware.hangman.gamemaster
import android.app.Application
import android.os.Bundle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.sososoftware.hangman.R
import com.sososoftware.hangman.databinding.FragmentHangmanGamemasterBinding
import com.sososoftware.hangman.guess.Guess


/**
 * A simple [Fragment] subclass.
 * Use the [GamemasterFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GamemasterFragment : Fragment() {
    private lateinit var binding: FragmentHangmanGamemasterBinding
    private lateinit var viewModel: GamemasterViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_hangman_gamemaster,container,false)
        binding.resetButton.setOnClickListener { viewModel.resetGame() }
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.word_length,
            R.layout.count_picker_item
        ).also { adapter ->
            adapter.setDropDownViewResource(R.layout.count_picker_item_dropdown)
            binding.spinnerWordLength.adapter = adapter
            binding.spinnerWordLength.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val length = (view as? TextView)?.text?.toString()?.toInt()
                    if (length != null) {
                        viewModel.onPromptLengthChanged(length)
                    }
                }
            }
            binding.spinnerWordLength.setSelection(DEFAULT_PROMPT_LENGTH - 1)
        }
        val viewModelFactory = GamemasterViewModelFactory(
            requireContext().applicationContext as Application
        )
        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(GamemasterViewModel::class.java)
        viewModel.state.observe(viewLifecycleOwner) { updateDisplay(it) }
        return binding.root
    }

    private fun onLetterSelected(state: GamemasterState, currentLetterGuess: Char?, index: Int) {
        val currentValue = state.prompt[index]
        val updateValue = when {
            currentLetterGuess == null -> currentValue
            currentValue != null && currentValue != currentLetterGuess -> currentValue
            currentValue == currentLetterGuess -> null
            else -> currentLetterGuess
        }

        if (updateValue != currentValue) {
            val newPrompt = state.prompt.mapIndexed { promptIndex, oldLetter ->
                if (promptIndex == index) updateValue else oldLetter
            }
            viewModel.onPromptChange(newPrompt)
        }
    }

    private fun updateDisplay(state: GamemasterState) {
        updateGuess(state)
        buildPrompt(state)
        updateGallows(state)
    }

    private fun updateGuess(state: GamemasterState) {
        binding.guessText.text =
            if (state.dead) {
                "Game over. I lose."
            } else {
                state.guess?.let { guess ->
                    when (guess.type) {
                        Guess.GuessType.LETTER_GUESS -> "I guess the letter \"${guess.letter}\"."
                        Guess.GuessType.WORD_GUESS -> "The word is \"${guess.word}\"."
                        Guess.GuessType.GIVE_UP -> "I don't know this word."
                        Guess.GuessType.THINKING -> "Thinking..."
                    }
                } ?: ""
            }
        state.guess?.letter?.let {
            binding.finishGuessButton.isEnabled = true
            binding.finishGuessButton.setOnClickListener {
                state.guess?.letter?.let { letter -> viewModel.onGuess(letter) }
            }
            binding.finishGuessButton.visibility = View.VISIBLE
            if (it in state.prompt) {
                binding.finishGuessButton.setBackgroundResource(R.color.letterBackgroundCorrectGuess)
                binding.finishGuessButton.setText(R.string.done)
            } else {
                binding.finishGuessButton.setBackgroundResource(R.color.letterBackgroundIncorrectGuess)
                binding.finishGuessButton.setText(R.string.not_in_word)

            }
        } ?: run {
            binding.finishGuessButton.isEnabled = false
            binding.finishGuessButton.setOnClickListener {  }
            binding.finishGuessButton.visibility = View.INVISIBLE
        }
    }

    private fun buildPrompt(state: GamemasterState) {
        binding.promptHolder.removeAllViews()
        state.prompt.forEachIndexed{ index, promptLetter ->
            val promptLetterView = layoutInflater.inflate(R.layout.view_prompt_letter, null) as TextView
            promptLetterView.text = promptLetter?.toString() ?: "_"
            promptLetterView.setOnClickListener {
                state.guess?.let {
                    if (it.type == Guess.GuessType.LETTER_GUESS) {
                        onLetterSelected(state, it.letter, index)
                    }
                }
            }
            binding.promptHolder.addView(promptLetterView)
        }
    }

    private fun updateGallows(state: GamemasterState) {
        binding.gallows.setImageResource(when(state.wrongLetterCount) {
            0 -> R.drawable.gallows_0
            1 -> R.drawable.gallows_1
            2 -> R.drawable.gallows_2
            3 -> R.drawable.gallows_3
            4 -> R.drawable.gallows_4
            5 -> R.drawable.gallows_5
            6 -> R.drawable.gallows_6
            else -> R.drawable.gallows_7
        })
        binding.gallows.contentDescription = when(state.wrongLetterCount) {
            0 -> getString(R.string.zero_wrong_guesses)
            1 -> getString(R.string.one_wrong_guess)
            2 -> getString(R.string.two_wrong_guesses)
            3 -> getString(R.string.three_wrong_guesses)
            4 -> getString(R.string.four_wrong_guesses)
            5 -> getString(R.string.five_wrong_guesses)
            6 -> getString(R.string.six_wrong_guesses)
            else -> getString(R.string.seven_wrong_guesses)
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment.
         *
         * @return A new instance of fragment HangmanPlayerFragment.
         */
        @JvmStatic
        fun newInstance() = GamemasterFragment()
        private const val DEFAULT_PROMPT_LENGTH = 6
    }
}