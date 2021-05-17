package co.anteia.anteiasdk.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import co.anteia.anteiasdk.R
import co.anteia.anteiasdk.data.api.ApiHelper
import co.anteia.anteiasdk.data.api.RetrofitBuilder
import co.anteia.anteiasdk.databinding.FragmentModifyEmailBinding
import co.anteia.anteiasdk.provider.DataProviderSingleton
import co.anteia.anteiasdk.viewModel.BaseViewModelFactory
import co.anteia.anteiasdk.utils.Status
import co.anteia.anteiasdk.utils.Utilities
import co.anteia.anteiasdk.viewModel.ModifyEmailViewModel


class ModifyEmailFragment : Fragment() {
    private lateinit var binding: FragmentModifyEmailBinding
    private lateinit var viewModel: ModifyEmailViewModel
    val data = DataProviderSingleton.instance


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, BaseViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
            ModifyEmailViewModel::class.java)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.nextButton.setOnClickListener {
            modifyEmail()
        }
        if (!viewModel.data.email.isNullOrBlank()) {
            viewModel.email.value = viewModel.data.email
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_modify_email, container, false
        )

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        return binding.root
    }


    private fun modifyEmail(){
        viewModel.modifyEmail(viewModel.email.value!!, data.token!!).observe(viewLifecycleOwner,{
            it?.let { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        data.email=viewModel.email.value
                        nextFragment()
                    }

                    Status.ERROR -> {
                        Utilities.showSnackbar(requireActivity(),resource.message.toString())
                        binding.animationView.visibility= View.INVISIBLE
                        binding.nextButton.visibility = View.VISIBLE
                    }
                    Status.LOADING -> {
                        binding.animationView.visibility= View.VISIBLE
                        binding.nextButton.visibility = View.INVISIBLE
                    }
                }
            }
        })
    }

    private fun nextFragment() {
        findNavController().navigate(R.id.action_modifyEmailFragment_to_confirmEmailFragment)
    }
}