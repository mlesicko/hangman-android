package com.sososoftware.hangman.solver

import android.content.SharedPreferences
import android.graphics.Color
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
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.sososoftware.hangman.R
import com.sososoftware.hangman.databinding.FragmentHangmanSolverBinding
import com.sososoftware.hangman.getAllWords
import com.sososoftware.hangman.guess.Guess
import com.sososoftware.hangman.settings.getAlgorithm
import kotlinx.coroutines.launch


/**
 * A simple [Fragment] subclass.
 * Use the [SolverFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SolverFragment : Fragment() {
    private lateinit var binding: FragmentHangmanSolverBinding
    private lateinit var viewModel: SolverViewModel
    private lateinit var sharedPreferences: SharedPreferences

    private val onSharedPreferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener(
            fun(preferences: SharedPreferences, key: String) {
                if (key == "algorithm") {
                    viewModel.updateAlgorithm(preferences.getAlgorithm())
                }
            }
        )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_hangman_solver,container,false)
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
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val algorithm = sharedPreferences.getString("algorithm", "good") ?: "good"
        val viewModelFactory = SolverViewModelFactory(
            DEFAULT_PROMPT_LENGTH,
            algorithm
        )
        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(SolverViewModel::class.java)
        viewModel.state.observe(viewLifecycleOwner) { updateDisplay(it) }
        sharedPreferences.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener)
        getWords()
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener)
    }

    private fun getWords() {
        lifecycleScope.launch {
            if (viewModel.words.isEmpty()) {
                viewModel.words = getAllWords(resources)
            }
        }
    }

    private fun onLetterSelected(state: SolverState, newLetter: Char?, index: Int) {
        val newPrompt = state.prompt.mapIndexed {
                promptIndex, oldLetter -> if (promptIndex == index) newLetter else oldLetter
        }
        viewModel.onPromptChange(newPrompt)
    }

    private fun updateDisplay(state: SolverState) {
        updateGuess(state)
        buildPrompt(state)
        buildLetters(state)
    }

    private fun updateGuess(state: SolverState) {
        binding.guessText.text = state.guess?.let { guess ->
            when (guess.type) {
                Guess.GuessType.LETTER_GUESS -> "I guess the letter \"${guess.letter}\"."
                Guess.GuessType.WORD_GUESS -> "The word is \"${guess.word}\"."
                Guess.GuessType.GIVE_UP -> "I don't know this word."
                Guess.GuessType.THINKING -> "Thinking..."
            }
        } ?: ""
        binding.rejectGuessButton.isEnabled = state.guess?.letter != null
        binding.rejectGuessButton.setOnClickListener {
            state.guess?.letter?.let { viewModel.onGuess(it) }
        }
    }

    private fun buildPrompt(state: SolverState) {
        binding.promptHolder.removeAllViews()
        state.prompt.forEachIndexed{ index, promptLetter ->
            val promptLetterView = layoutInflater.inflate(R.layout.view_prompt_letter, null) as TextView
            promptLetterView.text = promptLetter?.toString() ?: "_"
            promptLetterView.setOnClickListener {
                val dialog = LetterListDialogFragment.newInstance()
                dialog.callback = { letter ->
                    onLetterSelected(state, letter, index)
                }
                dialog.show(parentFragmentManager, "letterPickerDialog")
            }
            binding.promptHolder.addView(promptLetterView)
        }
    }

    private fun buildLetters(state: SolverState) {
        val letters = 'A'..'Z'
        binding.letterHolder.removeAllViews()
        letters.forEach { c ->
            binding.letterHolder.addView(buildLetterView(state, c))
        }
    }

    private fun buildLetterView(state: SolverState, c: Char): View {
        val letterView = layoutInflater.inflate(R.layout.view_letter, null) as TextView
        letterView.text = c.toString()
        if (c !in state.guessedLetters){
            letterView.setTextColor(Color.parseColor("black"))
            letterView.setBackgroundResource(R.drawable.letter_unguessed_background)
            letterView.setOnClickListener { viewModel.onGuess(c) }
        } else {
            letterView.setTextColor(Color.parseColor("white"))
            letterView.setBackgroundResource(
                if (c in state.prompt)
                    R.drawable.letter_correct_guess_background
                else
                    R.drawable.letter_incorrect_guess_background
            )
            letterView.setOnClickListener { viewModel.onLetterUnselected(c) }
        }
        return letterView
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment.
         *
         * @return A new instance of fragment HangmanPlayerFragment.
         */
        @JvmStatic
        fun newInstance() = SolverFragment()
        private const val DEFAULT_PROMPT_LENGTH = 6
    }
}