package co.anteia.anteiasdk.ui.createpassword

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import co.anteia.anteiasdk.R
import co.anteia.anteiasdk.data.api.ApiHelper
import co.anteia.anteiasdk.data.api.DataProviderSingleton
import co.anteia.anteiasdk.data.api.RetrofitBuilder
import co.anteia.anteiasdk.data.dto.RefreshClientTokenRequest
import co.anteia.anteiasdk.databinding.FragmentCreatePasswordBinding
import co.anteia.anteiasdk.utils.Status
import co.anteia.anteiasdk.utils.Utilities
import co.anteia.anteiasdk.viewModel.BaseViewModelFactory
import co.anteia.anteiasdk.viewModel.CreatePasswordViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.nio.charset.StandardCharsets

/*
 Created by arenas on 10/6/21.
*/
class CreatePasswordActivity : AppCompatActivity(){
    private lateinit var binding: FragmentCreatePasswordBinding
    val data = DataProviderSingleton.instance
    private lateinit var viewModel: CreatePasswordViewModel
    private  val TAG = "PasswordActivity"
    private var registrationID : String? = null
    private lateinit var apiHelper : ApiHelper
    private lateinit var activity: Activity



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity = this

        val b = intent.extras
        if (b != null){
            registrationID = b.getString("registrationID")
            Log.d(TAG,"Registration id : $registrationID")
            if (registrationID == null)
                Utilities.closeActivity(this, "Debes indicar el registration ID")
        }else{
            Utilities.closeActivity(this, "Debes indicar el registration ID")
        }

        setContentView(R.layout.fragment_create_password)


        apiHelper = ApiHelper(RetrofitBuilder.apiService)

        viewModel = ViewModelProvider(this, BaseViewModelFactory(apiHelper)).get(
            CreatePasswordViewModel::class.java)
        binding = DataBindingUtil.setContentView(this,R.layout.fragment_create_password)
        binding.viewModel = viewModel

        binding.lifecycleOwner = this
        binding.nextButton.setOnClickListener {
            setupAddPasswordObserver()
        }

        if (data.token == null){
            if (registrationID ==null){
                Utilities.closeActivity(this, "Debes indicar el registration ID")
            }
            else{
                Log.d(TAG,"No hay token.. generando uno nuevo")
                CoroutineScope(Dispatchers.IO).launch{
                    binding.nextButton.visibility= View.INVISIBLE
                    var credentials = data.userName+":"+data.apiKey
                    credentials = credentials.toBase64()
                    try{
                        val newToken =  apiHelper.refreshClientToken(RefreshClientTokenRequest(registrationID),credentials)
                        withContext(Dispatchers.Main){
                            if (newToken.token !=null){
                                data.token = newToken.token
                                binding.nextButton.visibility= View.VISIBLE
                                Log.d(TAG,"nuevotoken")
                            }
                        }
                    }
                    catch (e: HttpException){
                        Utilities.closeActivity(
                            activity,
                            "Ha ocurrido un error refrescando el token ${e.code()}"
                        )
                    }
                }
            }
        }

    }
    private fun setupAddPasswordObserver(){
        viewModel.addPassword().observe(this,{
            it.let { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        Log.e("response", resource.data.toString())
                        nextFragment()
                    }
                    Status.ERROR -> {
                        Log.e(TAG,"Error.. "+resource.message.toString())
                        binding.passwordConfirmTv.isEnabled = true
                        binding.passwordTv.isEnabled = true
                        binding.nextButton.isEnabled = true
                        Utilities.showSnackbar(activity,resource.message.toString())
                    }
                    Status.LOADING -> {
                        binding.passwordConfirmTv.isEnabled = false
                        binding.passwordTv.isEnabled = false
                        binding.nextButton.isEnabled = false
                        Log.e(TAG,"cargando..")
                    }
                }
            }
        })

    }
    private fun nextFragment() {
        val intent = Intent()
        intent.putExtra("response", "Ha finalizado el proceso")
        setResult(Activity.RESULT_OK,intent)
        finish()
    }

    private fun String.toBase64(): String {
        return String(
            android.util.Base64.encode(this.toByteArray(), android.util.Base64.NO_WRAP),
            StandardCharsets.UTF_8
        )
    }
}