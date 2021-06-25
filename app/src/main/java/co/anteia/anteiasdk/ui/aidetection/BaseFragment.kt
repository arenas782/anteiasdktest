package co.anteia.anteiasdk.ui.aidetection

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.fragment.app.Fragment


/*
 Created by arenas on 21/6/21.
*/
abstract class BaseFragment : Fragment() {
    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            val a: Activity = requireActivity()
            a.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        }
    }
}
