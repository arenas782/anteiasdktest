package co.anteia.anteiasdk.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import co.anteia.anteiasdk.R
import co.anteia.anteiasdk.databinding.FragmentGreetingBinding
import co.anteia.anteiasdk.databinding.FragmentWelcomeFlowFirstBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [WelcomeFlowFirst.newInstance] factory method to
 * create an instance of this fragment.
 */
class WelcomeFlowFragmentFirst : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentWelcomeFlowFirstBinding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_welcome_flow_first, container, false)

        binding.nextButton.setOnClickListener {
            nextFragment()
        }
        return binding.root
    }

    private fun nextFragment(){
        findNavController().navigate(R.id.action_welcomeFlowFirst_to_welcomeFlowFragmentSecond)
    }

}