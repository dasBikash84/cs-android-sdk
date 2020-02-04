package com.dasbikash.cs_android_sdk.android_sdk.model.public_models.response

import androidx.annotation.Keep

@Keep
data class ErrorResponse(
    val errorCode:String?=null,
    val errorMessage:String?=null
)