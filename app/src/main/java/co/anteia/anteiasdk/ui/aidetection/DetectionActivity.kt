package co.anteia.anteiasdk.ui.aidetection

import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.add
import androidx.fragment.app.commit
import co.anteia.anteiasdk.R
import co.anteia.anteiasdk.data.api.ApiHelper
import co.anteia.anteiasdk.data.api.DataProviderSingleton
import co.anteia.anteiasdk.data.api.RetrofitBuilder
import co.anteia.anteiasdk.data.dto.RefreshClientTokenRequest
import co.anteia.anteiasdk.utils.Utilities.closeActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.nio.charset.StandardCharsets

class DetectionActivity : AppCompatActivity(R.layout.activity_detection) {
    val data = DataProviderSingleton.instance
    private var registrationID : String? = null
    private  val TAG = "DetectionActivity"
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

        if (data.token == null){
            if (registrationID ==null){
                closeActivity(activity, "Debes indicar el registration ID")
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
                                try{
                                    val matiToken = data.token?.let { apiHelper.matiInit(it) }
                                    if (matiToken  != null){
                                            supportFragmentManager.commit {
                                                setReorderingAllowed(true)
                                                add<GoodLightFragment>(R.id.fragment_detection)
                                            }
                                    }
                                }catch (e: HttpException){
                                    closeActivity(
                                        activity,
                                        "Ha ocurrido un error refrescando el token ${e.code()}"
                                    )
                                }
                            }
                        }
                    }
                    catch (e: HttpException){
                        closeActivity(
                            activity,
                            "Ha ocurrido un error refrescando el token ${e.code()}"
                        )
                    }
                }
            }
        }
            else{
                if (savedInstanceState == null) {
                    supportFragmentManager.commit {
                        setReorderingAllowed(true)
                        add<GoodLightFragment>(R.id.fragment_detection)
                    }
                }
            }
    }

    override fun onBackPressed() {
        val fragment = this.supportFragmentManager

        if (fragment.backStackEntryCount == 0) {
            // No backstack to pop, so calling super
            super.onBackPressed()
        } else {
            fragment.popBackStack()
        }
    }
    private fun String.toBase64(): String {
        return String(
            android.util.Base64.encode(this.toByteArray(), android.util.Base64.NO_WRAP),
            StandardCharsets.UTF_8
        )
    }
}