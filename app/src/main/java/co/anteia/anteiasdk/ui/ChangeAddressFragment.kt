package co.anteia.anteiasdk.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import co.anteia.anteiasdk.R
import co.anteia.anteiasdk.databinding.FragmentChangeAddressBinding


class ChangeAddressFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentChangeAddressBinding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_change_address, container, false)

        binding.nextButton.setOnClickListener {
            nextFragment()
        }
        return binding.root
    }

    private fun nextFragment(){

    }

}