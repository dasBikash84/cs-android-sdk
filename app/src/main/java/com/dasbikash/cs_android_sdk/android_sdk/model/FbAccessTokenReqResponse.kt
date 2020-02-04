package com.dasbikash.cs_android_sdk.android_sdk.model

import androidx.annotation.Keep
import com.dasbikash.cs_android_sdk.android_sdk.exception.InvalidFbLoginTokenException

@Keep
internal data class FbAccessTokenReqResponse(
        var fbPath:String?=null,
        var token:String?=null
){
        fun validate(){
                if (fbPath==null || token==null){
                        throw InvalidFbLoginTokenException()
                }
        }
}