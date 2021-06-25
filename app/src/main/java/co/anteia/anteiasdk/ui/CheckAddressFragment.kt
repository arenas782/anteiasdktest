package co.anteia.anteiasdk.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import co.anteia.anteiasdk.R
import co.anteia.anteiasdk.databinding.FragmentCheckAddressBinding


class CheckAddressFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentCheckAddressBinding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_check_address, container, false)

        binding.nextButton.setOnClickListener {
            nextFragment()
        }
        binding.changeAddressTv.setOnClickListener{
            goToModifyAddress()
        }
        return binding.root
    }

    private fun nextFragment(){

    }
    private fun goToModifyAddress(){

    }


}