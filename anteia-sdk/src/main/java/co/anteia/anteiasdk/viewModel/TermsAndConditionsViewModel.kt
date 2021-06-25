package co.anteia.anteiasdk.viewModel

import androidx.lifecycle.*
import co.anteia.anteiasdk.data.api.ApiHelper
import co.anteia.anteiasdk.data.dto.InitialRegistrationRequest
import co.anteia.anteiasdk.provider.DataProviderSingleton
import co.anteia.anteiasdk.utils.Resource
import kotlinx.coroutines.Dispatchers

class TermsAndConditionsViewModel(private val apiHelper: ApiHelper) : ViewModel() {
    val marked = MutableLiveData(false)
    val data = DataProviderSingleton.instance




    fun initMati(token : String) = liveData(Dispatchers.IO) {





        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiHelper.matiInit(token = token)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun initialRegistration() = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            val  body = InitialRegistrationRequest(data.projectId!!, data.code!!)


            emit(Resource.success(data = apiHelper.initialRegistration(body = body)))

        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }


}