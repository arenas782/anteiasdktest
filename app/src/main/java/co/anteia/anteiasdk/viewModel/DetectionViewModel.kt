package co.anteia.anteiasdk.viewModel

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import co.anteia.anteiasdk.data.api.ApiHelper
import co.anteia.anteiasdk.data.dto.SendPhotoRequest
import co.anteia.anteiasdk.provider.ApiSingleton
import co.anteia.anteiasdk.provider.DataProviderSingleton
import co.anteia.anteiasdk.utils.Resource
import co.anteia.anteiasdk.utils.Utilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream


class DetectionViewModel(private val apiHelper: ApiHelper) : ViewModel() {
    val api = ApiSingleton.instance
    val data = DataProviderSingleton.instance



    fun sendFace(bitmap: Bitmap) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            val file = Utilities.bitmapToBase64(bitmap)
            val body = SendPhotoRequest(file =file)
            emit(
                Resource.success(
                    data = apiHelper.sendPhotoFace(
                        body = body,
                        token = data.token!!
                    )
                )
            )

        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun sendDocumentFront(bitmap: Bitmap) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {

            val file = Utilities.bitmapToBase64(bitmap)
            val body = SendPhotoRequest(file =file)
            emit(
                    Resource.success(
                            data = apiHelper.sendPhotoFront(
                                body = body,
                                     token = data.token!!
                            )
                    )
            )

        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun sendDocumentBack(bitmap: Bitmap) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {

            val file = Utilities.bitmapToBase64(bitmap)
            val body = SendPhotoRequest(file =file)
            emit(
                Resource.success(
                    data = apiHelper.sendPhotoBack(
                        body = body,
                        token = data.token!!
                    )
                )
            )

        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

}