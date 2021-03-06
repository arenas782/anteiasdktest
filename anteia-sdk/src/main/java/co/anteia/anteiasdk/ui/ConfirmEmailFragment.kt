package co.anteia.anteiasdk.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import co.anteia.anteiasdk.R
import co.anteia.anteiasdk.data.api.ApiHelper
import co.anteia.anteiasdk.data.api.RetrofitBuilder
import co.anteia.anteiasdk.databinding.FragmentConfirmEmailBinding
import co.anteia.anteiasdk.provider.DataProviderSingleton
import co.anteia.anteiasdk.utils.Status
import co.anteia.anteiasdk.utils.Utilities
import co.anteia.anteiasdk.viewModel.BaseViewModelFactory
import co.anteia.anteiasdk.viewModel.ConfirmEmailViewModel


class ConfirmEmailFragment : Fragment() {

    private lateinit var viewModel: ConfirmEmailViewModel
    val data = DataProviderSingleton.instance
    private lateinit var binding: FragmentConfirmEmailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, BaseViewModelFactory(ApiHelper(RetrofitBuilder.apiService)))
            .get(ConfirmEmailViewModel::class.java)
        lifecycleScope.launchWhenCreated {
            setupSendEmailObserver()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Sets the email to the view
        binding.checkEmailFolderTv.text =
            resources.getString(R.string.check_for_email, viewModel.data.email)

        binding.resendEmailTv.setOnClickListener {
            resendEmail()
        }

        binding.nextButton.setOnClickListener {
            validateEmail()
        }
        binding.modifyEmailButton.setOnClickListener {
            goToModifyEmail()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_confirm_email, container, false
        )
        binding.lifecycleOwner = this
        return binding.root
    }


    private fun setupSendEmailObserver(){
            viewModel.sendEmail(data.email!!, data.token!!).observe(requireActivity()){
                it?.let { resource ->
                    when (resource.status) {
                        Status.SUCCESS -> {
                            Utilities.showSnackbar(requireActivity(),"Email enviado")
                            Log.e("response", resource.data.toString())
                        }
                        Status.ERROR -> {
                            Utilities.showSnackbar(requireActivity(),resource.message.toString())
                        }
                        Status.LOADING -> {
                            Utilities.showSnackbar(requireActivity(),"Enviando email")
                        }
                    }
                }
            }
    }

    private fun validateEmail(){
        viewModel.didEmail(token = data.token!!).observe(viewLifecycleOwner,{
            it?.let { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        if(resource.data?.confirmed == true)
                            nextFragment()
                        else{
                            binding.animationView.visibility= View.INVISIBLE
                            binding.emailLinearLayout.visibility = View.VISIBLE
                            Utilities.showSnackbar(requireActivity(),"Por favor haz click al link en tu correo")
                        }
                    }
                    Status.ERROR -> {
                        Utilities.showSnackbar(requireActivity(),resource.message.toString())
                        binding.animationView.visibility= View.INVISIBLE
                        binding.emailLinearLayout.visibility = View.VISIBLE
                    }
                    Status.LOADING -> {
                        binding.animationView.visibility= View.VISIBLE
                        binding.emailLinearLayout.visibility = View.INVISIBLE
                    }
                }
            }
        })
    }

    private fun resendEmail(){
        setupSendEmailObserver()
    }

    private fun goToModifyEmail() {
        findNavController().navigate(R.id.action_confirmEmailFragment_to_modifyEmailFragment)
    }

    private fun nextFragment() {
        findNavController().navigate(R.id.action_confirmEmailFragment_to_createPasswordFragment)
    }
}