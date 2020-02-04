package com.dasbikash.cs_android_sdk.android_sdk.data_service.api

import android.content.Context
import com.dasbikash.cs_android_sdk.android_sdk.exception.AccessTokenExpiredException
import com.dasbikash.cs_android_sdk.android_sdk.exception.RemoteApiException
import com.dasbikash.cs_android_sdk.android_sdk.utils.DisplayUtils
import com.dasbikash.cs_android_sdk.android_sdk.utils.LoggerUtils
import com.dasbikash.cs_android_sdk.android_sdk.utils.RetrofitUtils
import com.dasbikash.cs_android_sdk.android_sdk.utils.SharedPreferenceUtils
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

internal object ChatServerBaseApi {

    const val STORAGE_SHORTAGE_STATUS_CODE = 507
    const val UNAUTHORIZED_STATUS_CODE = 401
    const val DATA_NOT_FOUND_STATUS_CODE = 404
    const val ORDER_DELETED_STATUS_CODE = 410
    const val FORBIDDEN_STATUS_CODE = 403

    private const val BASE_API_PATH = "http://192.168.0.111:9110/admin/"
    private const val JWT_PREAMBLE = "Bearer "
    private const val BASE_API_PATH_SPK =
        "com.dasbikash.cs_android_sdk.android_sdk.data_service.api.ChatServerBaseApi.BASE_API_PATH_SPK"
    private val API_PATH_FORMAT = Regex("https?://\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}(:\\d{4})?.+")
    private const val PATH_SAVED_MESSAGE = "BE base path saved"

    private lateinit var mBaseApiPath:String

    fun init(context: Context){
        (SharedPreferenceUtils
            .getData(context, SharedPreferenceUtils.DefaultValues.DEFAULT_STRING, BASE_API_PATH_SPK) as String).apply {
            if (isBlank()){
                saveBaseApiPath(context)
                mBaseApiPath = BASE_API_PATH
            }else{
                mBaseApiPath = this
            }
        }
    }

    fun getBaseApiPath(context: Context):String =
        SharedPreferenceUtils.getData(context, SharedPreferenceUtils.DefaultValues.DEFAULT_STRING,
            BASE_API_PATH_SPK
        ) as String

    fun saveBaseApiPath(context: Context, path:String= BASE_API_PATH):Boolean{
        if (API_PATH_FORMAT.matches(path)) {
            SharedPreferenceUtils.saveData(context, path, BASE_API_PATH_SPK)
            DisplayUtils.showShortToast(context, PATH_SAVED_MESSAGE)
            mBaseApiPath = path
            return true
        }
        return false
    }

    fun getRetrofitInstance():Retrofit {
        return Retrofit.Builder().baseUrl(mBaseApiPath)
            .addConverterFactory(GsonConverterFactory.create(Gson()))
            .build()
    }

    fun getJwtAuthHeader(token:String) = JWT_PREAMBLE +token

}

internal fun <T,R> Response<T>.process(c: Continuation<R>){
    if (!this.isSuccessful){
        LoggerUtils.debugLog("code: ${code()}",this::class.java)
        val errorResponse = RetrofitUtils.getErrorResponse(this.errorBody()?.string())
        when(code()){
            ChatServerBaseApi.FORBIDDEN_STATUS_CODE ->
                c.resumeWithException(AccessTokenExpiredException(errorResponse?.toString()))
            else -> c.resumeWithException(RemoteApiException(errorResponse?.toString()))
        }
    }
}

internal suspend fun <T> Call<T>.callApi():T?{
    return suspendCoroutine {
        this.enqueue(object : Callback<T?> {

            override fun onFailure(call: Call<T?>, t: Throwable) {
                it.resumeWithException(RemoteApiException(t))
            }

            override fun onResponse(
                call: Call<T?>,
                response: Response<T?>
            ) {
                if (response.isSuccessful) {
                    it.resume(response.body())
                } else {
                    response.process(it)
                }
            }
        })
    }
}