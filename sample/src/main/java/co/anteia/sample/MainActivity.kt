package co.anteia.sample

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import co.anteia.anteiasdk.AnteiaSDK
import co.anteia.anteiasdk.ui.confirmemail.EmailActivity
import co.anteia.anteiasdk.ui.dataentry.DataEntryActivity
import co.anteia.anteiasdk.ui.aidetection.DetectionActivity
import co.anteia.anteiasdk.ui.confirmemail.PhoneActivity
import co.anteia.anteiasdk.ui.createpassword.CreatePasswordActivity
import co.anteia.anteiasdk.ui.termsandconditions.TermsAndConditionsActivity
import co.anteia.sample.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar


class MainActivity : AppCompatActivity() {

    private val testProjectId = "60987eab7dc0b8596f75aa4c";
    private val testCode = "testingMobile"
    private val phoneRequestCode = 202
    private val termsRequestCode= 200

    private val detectionRequestCode = 204
    private val createPasswordRequestCode= 203
    private val emailRequestCode = 205

    private val dataEntryRequestCode= 201
    private lateinit var binding: ActivityMainBinding
    private val userName = "5ffda817c683b046372d24b9"
    private val apiKey = "testApiKeyAnteia2020"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.lifecycleOwner = this

        val anteia = AnteiaSDK() // initialize variable
        anteia.setupKeys(testCode,testProjectId) //setup project keys
        anteia.setupUser(userName,apiKey) //setup user keys


        binding.dataEntryButton.setOnClickListener {
            launchDataEntry()
        }
        binding.terminosButton.setOnClickListener {
            launchTermsAndConditions()
        }
        binding.emailButton.setOnClickListener {
            launchConfirmEmail()
        }
        binding.frontDetectionButton.setOnClickListener {
            launchML()
        }
        binding.phoneButton.setOnClickListener {
            launchPhone()
        }
        binding.passwordButton.setOnClickListener {
            launchPassword()
        }


    }

    private fun launchPassword() {
        intent = Intent(this, CreatePasswordActivity::class.java)
        val b = Bundle()
        b.putString("registrationID","60ad1733b504814a06872e53" )
        intent.putExtras(b)
        startActivityForResult(intent, createPasswordRequestCode)
    }


    private fun launchML() {
        intent = Intent(this, DetectionActivity::class.java)
        val b = Bundle()
        b.putString("registrationID","60ad1733b504814a06872e53" )
        intent.putExtras(b)
        startActivityForResult(intent, detectionRequestCode)
    }


    private fun launchTermsAndConditions() {
        //como aqui es donde se genera el primer token, no necesitamos pasarle registrationId
        intent = Intent(this, TermsAndConditionsActivity::class.java)
        startActivityForResult(intent, termsRequestCode)

    }


    private fun launchDataEntry(){
        intent = Intent(this, DataEntryActivity::class.java)
        val b = Bundle()
        b.putString("registrationID","60ad1733b504814a06872e53" )
        intent.putExtras(b)
        startActivityForResult(intent, dataEntryRequestCode)
    }



    private fun launchConfirmEmail() {
        intent = Intent(this, EmailActivity::class.java)
        val b = Bundle()
        b.putString("registrationID","60ad1733b504814a06872e53" )
        intent.putExtras(b)
        startActivityForResult(intent, emailRequestCode)
    }
    private fun launchPhone() {
        intent = Intent(this,PhoneActivity::class.java)
        val b = Bundle()
        b.putString("registrationID","60ad1733b504814a06872e53" )
        intent.putExtras(b)
        startActivityForResult(intent, phoneRequestCode)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == termsRequestCode) {
            if (resultCode == RESULT_OK) {
                val result = data?.extras!!.getString("response")
                onSNACK(result.toString(),activity = this)
                Log.d("resultado", result.toString())
                val registrationId = data.extras!!.getString("registrationId")
                Log.d("registration ID", registrationId.toString())
            }
            else if (resultCode == RESULT_CANCELED){
                Log.d("resultado", "cancelado")
            }
        }

        else if (requestCode == dataEntryRequestCode){
            if (resultCode == RESULT_OK) {
                val result = data?.extras!!.getString("response")

                onSNACK(result.toString(),activity = this)
                Log.d("resultado", result.toString())
            }
            else if (resultCode == RESULT_CANCELED){
                val result = data?.extras?.getString("response")
                Log.d("resultado", "cancelado. ${result.toString()}")
            }
        }
        else if (requestCode == createPasswordRequestCode){
            if (resultCode == RESULT_OK) {
                val result = data?.extras!!.getString("response")

                onSNACK(result.toString(),activity = this)
                Log.d("resultado", result.toString())
            }
            else if (resultCode == RESULT_CANCELED){
                val result = data?.extras?.getString("response")
                Log.d("resultado", "cancelado. ${result.toString()}")
            }
        }
        else if (requestCode == phoneRequestCode) {
            if (resultCode == RESULT_OK) {
                val result = data?.extras!!.getString("response")
                onSNACK(result.toString(),activity = this)
                Log.d("resultado", result.toString())
                val registrationId = data.extras!!.getString("registrationId")
                Log.d("registration ID", registrationId.toString())
            }
            else if (resultCode == RESULT_CANCELED){
                data?.extras?.getString("response")?.let { Log.d("resultado", it) }
            }
        }
        else if (requestCode == emailRequestCode) {
            if (resultCode == RESULT_OK) {
                val result = data?.extras!!.getString("response")
                onSNACK(result.toString(),activity = this)
                Log.d("resultado", result.toString())
                val registrationId = data.extras!!.getString("registrationId")
                Log.d("registration ID", registrationId.toString())
            }
            else if (resultCode == RESULT_CANCELED){
                data?.extras?.getString("response")?.let { Log.d("resultado", it) }
            }
        }
        else if (requestCode == detectionRequestCode) {
            if (resultCode == RESULT_OK) {
                val result = data?.extras!!.getString("response")
                onSNACK(result.toString(),activity = this)
                Log.d("resultado", result.toString())
                val registrationId = data.extras!!.getString("registrationId")
                Log.d("registration ID", registrationId.toString())
            }
            else if (resultCode == RESULT_CANCELED){
                data?.extras?.getString("response")?.let { Log.d("resultado", it) }
            }
        }
    }

    private fun onSNACK(msg : String, activity : Activity){
        val snackBar = Snackbar.make(activity.findViewById(android.R.id.content),
            msg, Snackbar.LENGTH_SHORT)
        snackBar.show()
    }
}