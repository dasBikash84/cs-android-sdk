package com.dasbikash.cs_android_sdk.android_sdk.utils

import com.dasbikash.cs_android_sdk.android_sdk.model.public_models.response.ErrorResponse
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException

internal object RetrofitUtils {
    fun getErrorResponse(errorString: String?): ErrorResponse?{
        errorString?.let {
            try{
                return GsonBuilder().setLenient().create().fromJson(it, ErrorResponse::class.java)
            }catch (ex: JsonSyntaxException){}
        }
        return null
    }
}