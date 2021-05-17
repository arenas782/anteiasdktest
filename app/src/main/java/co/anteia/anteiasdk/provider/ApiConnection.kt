package co.anteia.anteiasdk.provider

import android.graphics.Bitmap
import android.util.Log
import co.anteia.anteiasdk.data.dto.DeviceInfoDto
import co.anteia.anteiasdk.data.dto.InitialRegistrationRequest
import co.anteia.anteiasdk.data.dto.UserLocation
import co.anteia.anteiasdk.data.dto.AddInitialInfoRequest
import co.anteia.anteiasdk.data.dto.OtpRequest
import co.anteia.anteiasdk.data.dto.SendEmailRequest
import co.anteia.anteiasdk.data.dto.ConfirmOtpRequest
import co.anteia.requests.registerFlow.ModifyEmailRequest
import co.anteia.anteiasdk.data.dto.ModifyPhoneRequest
import co.anteia.anteiasdk.data.dto.ConfirmOtpResponse
import co.anteia.anteiasdk.data.dto.InitialRegistrationResponse
import co.anteia.anteiasdk.data.dto.DataDoneResponse
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit


class ApiConnection : IApiConnection {

//    val baseUrl = "http://192.168.20.20:8080/"

    val baseUrl = "https://test-api.anteia.co/"
    var token: String? = null
    val gson: Gson = Gson()

    val client: OkHttpClient =
        OkHttpClient().newBuilder()
            .connectTimeout(100, TimeUnit.SECONDS)
            .callTimeout(100, TimeUnit.SECONDS)
            .readTimeout(100, TimeUnit.SECONDS)
            .writeTimeout(100, TimeUnit.SECONDS)
            .build()
    val jsonMediaType: MediaType = "application/json; charset=utf-8".toMediaType();
    var regId: String? = null
    val authHeader = "Authorization"

    private fun getTokenHeader(): String {
        return "Bearer $token"
    }

    override fun initialRegistration(projectId: String, code: String): Boolean {
        Log.e("ApiConnection", "InitialRegistration")
        val url = "${baseUrl}/registerFlow/initialRegistration"
        val dto = InitialRegistrationRequest(projectId, code)
        val json = gson.toJson(dto)
        val body: RequestBody = json.toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build();
        try {

            val response = client.newCall(request).execute()
            val responseObj = gson.fromJson<InitialRegistrationResponse>(
                response.body?.string(),
                InitialRegistrationResponse::class.java
            )
            token = responseObj.userToken
            regId = responseObj.registrationId
            Log.e("ApiConnection", "InitialRegistration: true")
            return true
        } catch (e: Exception) {
            Log.e("ApiConnection", e.toString())
            Log.e("ApiConnection", "InitialRegistration: false")
            return false
        }

    }

    override fun sendPhotoFront(bitmap : Bitmap): Boolean {
        val temp = File.createTempFile("frontTemp", ".png")

        Log.e("ApiConnection", "sendPhotoFront")
        val url = "${baseUrl}/mati/sendPhotoFront"
        val requestBody: RequestBody = MultipartBody.Builder().setType(MultipartBody.FORM)

            .addFormDataPart(
                "file", temp.name,
                temp.asRequestBody("image/png".toMediaTypeOrNull())
            )
            .build()
        Log.e("RequestBody",requestBody.toString())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .addHeader(authHeader, getTokenHeader())
            .build();
        try {
            val logging = HttpLoggingInterceptor()
            logging.apply { logging.level = HttpLoggingInterceptor.Level.BODY }
            val client: OkHttpClient =
                    OkHttpClient().newBuilder()
                            .connectTimeout(100, TimeUnit.SECONDS)
                            .addInterceptor(logging)
                            .callTimeout(100, TimeUnit.SECONDS)
                            .readTimeout(100, TimeUnit.SECONDS)
                            .writeTimeout(100, TimeUnit.SECONDS)

                            .build()
            val response = client.newCall(request).execute()
            val r = response.code == 200
            Log.e("ApiConnection", "sendPhotoFront: $r ${response.code}")
            return r
        } catch (e: Exception) {
            Log.e("ApiConnection", e.toString())
            Log.e("ApiConnection", "sendPhotoFront: false")
            return false
        }
    }

    override fun sendPhotoBack(bytes: ByteArray): Boolean {
        val temp = File.createTempFile("backTemp", ".png")
        val stream = FileOutputStream(temp)
        stream.write(bytes);

        Log.e("ApiConnection", "sendPhotoBack")
        val url = "${baseUrl}/mati/sendPhotoBack"
        val requestBody: RequestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart(
                "file", temp.name,
                temp.asRequestBody("image/png".toMediaTypeOrNull())
            )
            .build()
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .addHeader(authHeader, getTokenHeader())
            .build();
        try {
            val response = client.newCall(request).execute()
            val r = response.code == 200
            Log.e("ApiConnection", "sendPhotoBack: $r ${response.code}")
            return r
        } catch (e: Exception) {
            Log.e("ApiConnection", e.toString())
            Log.e("ApiConnection", "sendPhotoBack: false")
            return false
        }
    }

    override suspend fun sendPhotoFace(bytes: ByteArray): Boolean {
        val temp = File.createTempFile("faceTemp", ".png")
        val stream = FileOutputStream(temp)
        stream.write(bytes);

        Log.e("ApiConnection", "sendPhotoface")
        val url = "${baseUrl}/mati/sendPhotoFace"
        val requestBody: RequestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart(
                "file", temp.name,
                temp.asRequestBody("image/png".toMediaTypeOrNull())
            )
            .build()
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .addHeader(authHeader, getTokenHeader())
            .build();
        try {
            val response = client.newCall(request).execute()
            val r = response.code == 200
            Log.e("ApiConnection", "sendPhotoface: $r ${response.code}")
            return r
        } catch (e: Exception) {
            Log.e("ApiConnection", e.toString())
            Log.e("ApiConnection", "sendPhotoface: false")
            return false
        }
    }

    override suspend fun matiInit(): Boolean {
        Log.e("ApiConnection", "matiInit")
        val url = "${baseUrl}mati/init"
        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader(authHeader, getTokenHeader())
            .build();
        try {
            val response = client.newCall(request).execute()
            Log.e("ApiConnection", "matiInit: ${response.code}")
            return response.code == 200
        } catch (e: Exception) {
            Log.e("ApiConnection", e.toString())
            return false
        }
    }

    override fun addDeviceInfo(deviceInfo: DeviceInfoDto): Boolean {
        val url = "${baseUrl}registerFlow/addDeviceInfo"
        val json = gson.toJson(deviceInfo)
        val body: RequestBody = json.toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url(url)
            .post(body)
            .addHeader(authHeader, getTokenHeader())
            .build();
        try {
            val response = client.newCall(request).execute()
            return response.code == 200
        } catch (e: Exception) {
            Log.e("ApiConnection", e.toString())
            return false
        }
    }

    override fun addInitialInformation(
        lastName: String,
        cellphone: String,
        email: String,
        identification: String
    ): Boolean {
        Log.e("ApiConnection", "AddInitialInformation")
        val url = "${baseUrl}registerFlow/addInitialInformation"
        val dto = AddInitialInfoRequest(lastName, cellphone, email, identification)
        val json = gson.toJson(dto)
        val body: RequestBody = json.toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url(url)
            .post(body)
            .addHeader(authHeader, getTokenHeader())
            .build();
        try {
            val response = client.newCall(request).execute()
            Log.e("ApiConnection", "AddInitialInformation: " + (response.code == 200).toString())
            return response.code == 200
        } catch (e: Exception) {
            Log.e("ApiConnection", e.toString())
            return false
        }
    }

    override fun userLocation(): UserLocation? {
        val url = "${baseUrl}registerFlow/userLocation"
        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader(authHeader, getTokenHeader())
            .build();
        try {
            val response = client.newCall(request).execute()
            return gson.fromJson(response.body?.string(), UserLocation::class.java)
        } catch (e: Exception) {
            Log.e("ApiConnection", e.toString())
            return null
        }
    }

    override fun addPassword(password: String): Boolean {
        return true
    }

    override fun verifyRegistration(): Boolean {
        val url = "${baseUrl}registerFlow/verifyRegistration"
        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader(authHeader, getTokenHeader())
            .build();
        try {
            val response = client.newCall(request).execute()
            return response.code == 200
        } catch (e: Exception) {
            Log.e("ApiConnection", e.toString())
            return false
        }
    }

    override fun executeWebhook(): Boolean {
        val url = "${baseUrl}registerFlow/executeWebHook"
        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader(authHeader, getTokenHeader())
            .build();
        try {
            val response = client.newCall(request).execute()
            return response.code == 200
        } catch (e: Exception) {
            Log.e("ApiConnection", e.toString())
            return false
        }
    }

    override fun executeLists(): Boolean {
        val url = "${baseUrl}registerFlow/executeLists"
        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader(authHeader, getTokenHeader())
            .build();
        try {
            val response = client.newCall(request).execute()
            return response.code == 200
        } catch (e: Exception) {
            Log.e("ApiConnection", e.toString())
            return false
        }
    }

    override fun modifyEmail(email: String): Boolean {
        val url = "${baseUrl}registerFlow/modifyEmail"
        val dto = ModifyEmailRequest(email)
        val json = gson.toJson(dto)
        val body: RequestBody = json.toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url(url)
            .post(body)
            .addHeader(authHeader, getTokenHeader())
            .build();
        try {
            val response = client.newCall(request).execute()
            return response.code == 200
        } catch (e: Exception) {
            Log.e("ApiConnection", e.toString())
            return false
        }
    }

    override fun modifyPhone(cellhpone: String): Boolean {
        val url = "${baseUrl}registerFlow/modifyPhone"
        val dto = ModifyPhoneRequest(cellhpone)
        val json = gson.toJson(dto)
        val body: RequestBody = json.toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url(url)
            .post(body)
            .addHeader(authHeader, getTokenHeader())
            .build();
        try {
            val response = client.newCall(request).execute()
            return response.code == 200
        } catch (e: Exception) {
            Log.e("ApiConnection", e.toString())
            return false
        }
    }

    override fun dataDone(): Boolean {
        val url = "${baseUrl}registerFlow/dataDone"
        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader(authHeader, getTokenHeader())
            .build();
        try {
            val response = client.newCall(request).execute()
            val dataDone = gson.fromJson<DataDoneResponse>(
                response.body?.string(),
                DataDoneResponse::class.java
            )
            return dataDone.dataDone ?: false
        } catch (e: Exception) {
            Log.e("ApiConnection", e.toString())
            return false
        }
    }

    override fun sendOtpMobile(phone: String): Boolean {
        Log.e("ApiConnection", "sendOtpMobile()")
        val url = "${baseUrl}otp/sendOtpMobile"
        val dto = OtpRequest(phone)
        val json = gson.toJson(dto)
        val body: RequestBody = json.toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url(url)
            .post(body)
            .addHeader(authHeader, getTokenHeader())
            .build();
        try {
            val response = client.newCall(request).execute()
            Log.e("ApiConnection", "sendOtpMobile: " + response.code)
            return response.code == 200
        } catch (e: Exception) {
            Log.e("ApiConnection", e.toString())
            return false
        }
    }

    override fun confirmOtpMobile(code: String): Boolean {
        val url = "${baseUrl}otp/confirmOtpMobile"
        val dto = ConfirmOtpRequest(code)
        val json = gson.toJson(dto)
        val body: RequestBody = json.toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url(url)
            .post(body)
            .addHeader(authHeader, getTokenHeader())
            .build();
        try {
            val response = client.newCall(request).execute()
            val resJson = gson.fromJson<ConfirmOtpResponse>(
                response.body?.string(),
                ConfirmOtpResponse::class.java
            )
            return resJson.confirmed ?: false
        } catch (e: Exception) {
            Log.e("ApiConnection", e.toString())
            return false
        }
    }

    override fun sendEmail(email: String): Boolean {
        val url = "${baseUrl}otp/sendEmail"
        val dto = SendEmailRequest(email)
        val json = gson.toJson(dto)
        val body: RequestBody = json.toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url(url)
            .post(body)
            .addHeader(authHeader, getTokenHeader())
            .build();
        try {
            val response = client.newCall(request).execute()
            return response.code == 200
        } catch (e: Exception) {
            Log.e("ApiConnection", e.toString())
            return false
        }
    }

    override fun didEmail(): Boolean {
        val url = "${baseUrl}otp/didEmail"
        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader(authHeader, getTokenHeader())
            .build();
        try {
            val response = client.newCall(request).execute()
            val jsonResponse = gson.fromJson<ConfirmOtpResponse>(
                response.body?.string(),
                ConfirmOtpResponse::class.java
            )
            return jsonResponse.confirmed ?: false
        } catch (e: Exception) {
            Log.e("ApiConnection", e.toString())
            return false
        }
    }
}