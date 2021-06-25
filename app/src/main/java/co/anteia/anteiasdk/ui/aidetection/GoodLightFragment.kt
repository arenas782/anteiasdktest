package co.anteia.anteiasdk.ui.aidetection

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import co.anteia.anteiasdk.R
import co.anteia.anteiasdk.databinding.FragmentGoodLightBinding


class GoodLightFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentGoodLightBinding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_good_light, container, false)

        binding.nextButton.setOnClickListener {
            nextFragment()
        }
        return binding.root
        // Inflate the layout for this fragment

    }

    private fun nextFragment(){

        val transaction = parentFragmentManager.beginTransaction()
        transaction.add(R.id.fragment_detection, DocumentDetectionFragment()).addToBackStack(null)
        transaction.commit()
    }



}