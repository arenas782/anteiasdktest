package co.anteia.anteiasdk

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import co.anteia.anteiasdk.provider.ApiSingleton
import co.anteia.anteiasdk.provider.DataProviderSingleton
import co.anteia.anteiasdk.utils.DeviceInfo

class AnteiaSDKMainActivity : AppCompatActivity() {
    val testProjectId = "60987eab7dc0b8596f75aa4c";
    val testCode = "testingMobile"
    val api = ApiSingleton.instance
    val dataProvider = DataProviderSingleton.instance
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.anteia_sdk_activity_main)
        this.supportActionBar?.hide()
        val deviceInfo = DeviceInfo
        Log.e("Manufacturer", deviceInfo.manufacturer)
        Log.e("Device", deviceInfo.model)
        Log.e("OS version", deviceInfo.osVersion)
        Log.e("numCpus",deviceInfo.cores.toString())
        Log.e("Height", deviceInfo.height.toString())
        Log.e("Width",deviceInfo.width.toString())
        setData()
    }

    fun setData() {
        dataProvider.code = testCode
        dataProvider.projectId = testProjectId
    }
}