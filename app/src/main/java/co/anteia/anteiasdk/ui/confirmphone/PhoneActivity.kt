package co.anteia.anteiasdk.ui.confirmemail

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.add
import androidx.fragment.app.commit
import co.anteia.anteiasdk.R
import co.anteia.anteiasdk.data.api.ApiHelper
import co.anteia.anteiasdk.data.api.DataProviderSingleton
import co.anteia.anteiasdk.data.api.RetrofitBuilder
import co.anteia.anteiasdk.data.dto.RefreshClientTokenRequest
import co.anteia.anteiasdk.ui.confirmphone.ConfirmPhoneFragment
import co.anteia.anteiasdk.ui.confirmphone.ModifyPhoneFragment
import co.anteia.anteiasdk.utils.Utilities.closeActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.nio.charset.StandardCharsets


class PhoneActivity : AppCompatActivity(R.layout.activity_phone) {
    val data = DataProviderSingleton.instance
    private var registrationID : String? = null
    private  val TAG = "PhoneActitivy"
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
                closeActivity(this, "Debes indicar el registration ID")
        }else{
            closeActivity(this, "Debes indicar el registration ID")
        }

        apiHelper = ApiHelper(RetrofitBuilder.apiService)

        if(data.phone==null){
            closeActivity(this,"Debes proveer un numero de telefono")
        }
        else
            if (data.token == null){
                if (registrationID ==null){
                    closeActivity(this,"Debes indicar el registration ID")
                }
                else{
                    Log.d(TAG,"No hay token.. generando uno nuevo")
                    CoroutineScope(Dispatchers.IO).launch{
                        var credentials = data.userName+":"+data.apiKey
                        credentials = credentials.toBase64()
                        try{
                            val newToken =  apiHelper.refreshClientToken(RefreshClientTokenRequest(registrationID),credentials)
                            withContext(Dispatchers.Main){
                                if (newToken.token !=null){
                                    data.token = newToken.token
                                    Log.d(TAG,"nuevotoken")
                                }
                            }
                        }
                        catch (e: HttpException){
                            closeActivity(activity,"Ha ocurrido un error refrescando el token ${e.code()}")
                        }
                    }
                }
            }
            else{
                if (savedInstanceState == null) {
                    supportFragmentManager.commit {
                        setReorderingAllowed(true)
                        add<ConfirmPhoneFragment>(R.id.fragment_phone)
                    }
                }
            }
    }

    override fun onBackPressed() {
        val fragment = this.supportFragmentManager
        fragment.findFragmentById(R.id.fragment_phone)


        if (fragment.backStackEntryCount == 0) {
            // No backstack to pop, so calling super
            super.onBackPressed()
        } else {
                fragment.popBackStack()
        }
    }
    private fun getCurrentFragment(): Fragment? {
        return this.supportFragmentManager.findFragmentById(R.id.fragment_phone)
    }

    private fun String.toBase64(): String {
        return String(
            android.util.Base64.encode(this.toByteArray(), android.util.Base64.NO_WRAP),
            StandardCharsets.UTF_8
        )
    }
}