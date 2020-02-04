package com.dasbikash.cs_android_sdk.android_sdk.data_service

import com.dasbikash.cs_android_sdk.android_sdk.data_service.api.ChatServerBaseApi
import com.dasbikash.cs_android_sdk.android_sdk.data_service.api.UserSessionApi
import com.dasbikash.cs_android_sdk.android_sdk.data_service.api.callApi
import com.dasbikash.cs_android_sdk.android_sdk.model.FbAccessTokenReqResponse
import com.dasbikash.cs_android_sdk.android_sdk.model.public_models.response.SuccessResponse

internal object UserSessionDataService {

    suspend fun requestChatSession(sessionToken:String): FbAccessTokenReqResponse {
        val authHeader = ChatServerBaseApi.getJwtAuthHeader(sessionToken)
        val call = ChatServerBaseApi.getRetrofitInstance()
                                                .create(UserSessionApi::class.java)
                                                .requestChatSession(authHeader)
        return call.callApi()!!
    }

    suspend fun terminateChatSession(sessionToken:String): SuccessResponse {
        val authHeader = ChatServerBaseApi.getJwtAuthHeader(sessionToken)
        val call = ChatServerBaseApi.getRetrofitInstance()
                                        .create(UserSessionApi::class.java)
                                        .requestChatSessionTermination(authHeader)
        return call.callApi()!!
    }
}