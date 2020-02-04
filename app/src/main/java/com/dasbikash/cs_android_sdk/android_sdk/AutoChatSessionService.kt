package com.dasbikash.cs_android_sdk.android_sdk

import android.app.Activity
import com.dasbikash.cs_android_sdk.android_sdk.data_service.api.AutoChatSessionApi
import com.dasbikash.cs_android_sdk.android_sdk.data_service.api.ChatServerBaseApi
import com.dasbikash.cs_android_sdk.android_sdk.data_service.api.callApi
import com.dasbikash.cs_android_sdk.android_sdk.firebase.FireStoreConUtils
import com.dasbikash.cs_android_sdk.android_sdk.model.public_models.response.AutoChatTokenReqResponse
import com.dasbikash.cs_android_sdk.android_sdk.model.public_models.response.SuccessResponse

object AutoChatSessionService {

    suspend fun getFbLoginToken(sessionToken:String): AutoChatTokenReqResponse {
        val authHeader = ChatServerBaseApi.getJwtAuthHeader(sessionToken)
        val call = ChatServerBaseApi.getRetrofitInstance()
                                                .create(AutoChatSessionApi::class.java)
                                                .getFbLoginToken(authHeader)
        return call.callApi()!!
    }

    suspend fun terminateSession(sessionToken:String): SuccessResponse {
        val authHeader = ChatServerBaseApi.getJwtAuthHeader(sessionToken)
        val call = ChatServerBaseApi.getRetrofitInstance()
                                        .create(AutoChatSessionApi::class.java)
                                        .terminateSession(authHeader)
        return call.callApi()!!
    }

    suspend fun logInFirebase(activity: Activity,loginToken:String){
        FireStoreConUtils.loginForAutoChat(activity,loginToken)!!
    }
}