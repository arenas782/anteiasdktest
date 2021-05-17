package co.anteia.anteiasdk

import android.content.Context
import android.content.Intent
import android.provider.AlarmClock.EXTRA_MESSAGE
import co.anteia.anteiasdk.utils.DeviceInfo


/*
 Created by arenas on 18/04/21.
*/
class AnteiaSDK {


    lateinit var intent: Intent
    lateinit var context: Context
    lateinit var deviceInfo: DeviceInfo


    public fun AnteiaSDK(context: Context) {

        this.context = context


    }


    fun launchSDK() {


        intent = Intent(context, AnteiaSDKMainActivity::class.java).apply {
            putExtra(EXTRA_MESSAGE, "hola")


        }

        context.startActivity(intent)
    }

}