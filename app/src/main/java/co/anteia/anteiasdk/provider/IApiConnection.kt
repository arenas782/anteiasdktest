package co.anteia.anteiasdk.provider

import android.graphics.Bitmap
import co.anteia.anteiasdk.data.dto.DeviceInfoDto
import co.anteia.anteiasdk.data.dto.UserLocation

interface IApiConnection {

    fun sendPhotoFront(bitmap : Bitmap): Boolean
    fun sendPhotoBack(bytes: ByteArray): Boolean
    suspend fun sendPhotoFace(bytes: ByteArray): Boolean
    suspend fun matiInit(): Boolean

    fun initialRegistration(projectId: String, code: String): Boolean

    fun addDeviceInfo(deviceInfo: DeviceInfoDto): Boolean

    fun addInitialInformation(
        lastName: String,
        cellphone: String,
        email: String,
        identification: String
    ): Boolean

    fun userLocation(): UserLocation?

    fun addPassword(password: String): Boolean

    fun verifyRegistration(): Boolean

    fun executeWebhook(): Boolean

    fun executeLists(): Boolean

    fun modifyEmail(email: String): Boolean

    fun modifyPhone(cellhpone: String): Boolean

    fun dataDone(): Boolean

    fun sendOtpMobile(phone: String): Boolean

    fun confirmOtpMobile(code: String): Boolean

    fun sendEmail(email: String): Boolean

    fun didEmail(): Boolean
}