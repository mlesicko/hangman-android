package com.sososoftware.hangman.player

import android.app.Application
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.sososoftware.hangman.*
import com.sososoftware.hangman.databinding.FragmentHangmanPlayerBinding

/**
 * A simple [Fragment] subclass.
 * Use the [PlayerFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PlayerFragment : Fragment() {
    private lateinit var binding: FragmentHangmanPlayerBinding
    private lateinit var viewModel: PlayerViewModel
    private lateinit var letterHolderAdapter: BaseAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_hangman_player,container,false)
        binding.resetButton.setOnClickListener { viewModel.resetGame() }
        val viewModelFactory = PlayerViewModelFactory(
            requireContext().applicationContext as Application
        )
        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(PlayerViewModel::class.java)
        viewModel.state.observe(viewLifecycleOwner) { updateDisplay(it) }

        letterHolderAdapter = object: BaseAdapter() {
            override fun getCount(): Int = 26

            override fun getItem(position: Int): Char = ('A'..'Z').elementAt(position)

            override fun getItemId(position: Int): Long = position.toLong()

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val letterView = layoutInflater.inflate(R.layout.view_letter, null) as TextView
                val c = getItem(position)
                letterView.text = c.toString()
                viewModel.state.value?.let {
                    if (it.wasGuessed(c)){
                        letterView.setTextColor(Color.parseColor("white"))
                        letterView.setBackgroundResource(
                            if (it.inWord(c))
                                R.drawable.letter_correct_guess_background
                            else
                                R.drawable.letter_incorrect_guess_background
                        )
                    } else {
                        letterView.setTextColor(Color.parseColor("black"))
                        letterView.setBackgroundResource(R.drawable.letter_unguessed_background)
                        letterView.setOnClickListener { viewModel.onGuess(c) }
                    }
                }
                return letterView
            }
        }
        binding.letterHolder.adapter = letterHolderAdapter

        return binding.root
    }

    private fun updateDisplay(state: PlayerState) {
        updatePrompt(state)
        updateGallows(state)
        letterHolderAdapter.notifyDataSetChanged()
    }

    private fun updatePrompt(state: PlayerState) {
        binding.wordHolder.text = state.wordToGuess.toCharArray().joinToString(" ") {
            if (state.wasGuessed(it)) it.toString() else "_"
        }
    }

    private fun updateGallows(state: PlayerState) {
        binding.gallows.setImageResource(when(state.wrongGuessCount) {
            0 -> R.drawable.gallows_0
            1 -> R.drawable.gallows_1
            2 -> R.drawable.gallows_2
            3 -> R.drawable.gallows_3
            4 -> R.drawable.gallows_4
            5 -> R.drawable.gallows_5
            6 -> R.drawable.gallows_6
            else -> R.drawable.gallows_7
        })
        binding.gallows.contentDescription = when(state.wrongGuessCount) {
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
        fun newInstance() =
            PlayerFragment()
    }
}