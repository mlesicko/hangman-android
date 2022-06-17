package com.sososoftware.hangman.solver

import android.app.Application
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.sososoftware.hangman.R
import com.sososoftware.hangman.databinding.FragmentHangmanSolverBinding
import com.sososoftware.hangman.guess.Guess


/**
 * A simple [Fragment] subclass.
 * Use the [SolverFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SolverFragment : Fragment() {
    private lateinit var binding: FragmentHangmanSolverBinding
    private lateinit var viewModel: SolverViewModel
    private lateinit var letterHolderAdapter: BaseAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_hangman_solver,container,false)
        val viewModelFactory = SolverViewModelFactory(
            requireContext().applicationContext as Application
        )
        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(SolverViewModel::class.java)
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
        letterHolderAdapter = object: BaseAdapter() {
            override fun getCount(): Int = 26

            override fun getItem(position: Int): Char = ('A'..'Z').elementAt(position)

            override fun getItemId(position: Int): Long = position.toLong()

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val letterView = layoutInflater.inflate(R.layout.view_letter, null) as TextView
                val c = getItem(position)
                letterView.text = c.toString()
                viewModel.state.value?.let {
                    if (c !in it.guessedLetters) {
                        letterView.setTextColor(Color.parseColor("black"))
                        letterView.setBackgroundResource(R.drawable.letter_unguessed_background)
                        letterView.setOnClickListener { viewModel.onGuess(c) }
                    } else {
                        letterView.setTextColor(Color.parseColor("white"))
                        letterView.setBackgroundResource(
                            if (c in it.prompt)
                                R.drawable.letter_correct_guess_background
                            else
                                R.drawable.letter_incorrect_guess_background
                        )
                        letterView.setOnClickListener { viewModel.onLetterUnselected(c) }
                    }
                }
                return letterView
            }
        }
        binding.letterHolder.adapter = letterHolderAdapter

        viewModel.state.observe(viewLifecycleOwner) { updateDisplay(it) }
        return binding.root
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
        letterHolderAdapter.notifyDataSetChanged()
    }

    private fun updateGuess(state: SolverState) {
        binding.guessText.text = when (state.guess.type) {
            Guess.GuessType.LETTER_GUESS -> "I guess the letter \"${state.guess.letter}\"."
            Guess.GuessType.WORD_GUESS -> "The word is \"${state.guess.word}\"."
            Guess.GuessType.GIVE_UP -> "I don't know this word."
            Guess.GuessType.THINKING -> "Thinking..."
        }
        binding.updateGuessButton.setOnClickListener {
            state.guess.letter?.let { viewModel.updateGuess() }
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