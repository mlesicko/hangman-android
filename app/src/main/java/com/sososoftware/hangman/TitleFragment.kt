package com.sososoftware.hangman

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.sososoftware.hangman.databinding.FragmentTitleBinding

/**
 * A simple [Fragment] subclass.
 * Use the [TitleFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TitleFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = DataBindingUtil.inflate<FragmentTitleBinding>(inflater,
            R.layout.fragment_title,container,false)
        binding.hangmanPlayerButton.setOnClickListener { view: View ->
            view.findNavController().navigate(R.id.action_titleFragment_to_hangmanPlayerFragment)
        }
        binding.hangmanGamemasterButton.setOnClickListener { view: View ->
            view.findNavController().navigate(R.id.action_titleFragment_to_hangmanGamemasterFragment)
        }
        binding.hangmanSolverButton.setOnClickListener { view: View ->
            view.findNavController().navigate(R.id.action_titleFragment_to_hangmanSolverFragment)
        }
        return binding.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment.
         *
         * @return A new instance of fragment TitleFragment.
         */
        @JvmStatic
        fun newInstance() = TitleFragment()
    }
}