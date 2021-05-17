package co.anteia.anteiasdk.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import co.anteia.anteiasdk.R
import co.anteia.anteiasdk.databinding.FragmentGreetingBinding
import co.anteia.anteiasdk.databinding.FragmentInstructionsBinding
import co.anteia.anteiasdk.databinding.FragmentWelcomeBinding

class WelcomeFragment : Fragment() {


    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentWelcomeBinding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_welcome, container, false)


        return binding.root
        // Inflate the layout for this fragment

    }




}