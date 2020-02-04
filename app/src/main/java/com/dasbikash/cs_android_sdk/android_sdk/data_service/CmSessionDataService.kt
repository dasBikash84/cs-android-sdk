package com.dasbikash.cs_android_sdk.android_sdk.data_service

import com.dasbikash.cs_android_sdk.android_sdk.data_service.api.ChatServerBaseApi
import com.dasbikash.cs_android_sdk.android_sdk.data_service.api.CmSessionApi
import com.dasbikash.cs_android_sdk.android_sdk.data_service.api.callApi
import com.dasbikash.cs_android_sdk.android_sdk.model.FbAccessTokenReqResponse
import com.dasbikash.cs_android_sdk.android_sdk.model.public_models.response.SuccessResponse

internal object CmSessionDataService {

    suspend fun requestFbAccessToken(sessionToken:String): FbAccessTokenReqResponse {
        val authHeader = ChatServerBaseApi.getJwtAuthHeader(sessionToken)
        val call = ChatServerBaseApi.getRetrofitInstance()
                                                .create(CmSessionApi::class.java)
                                                .requestCmSession(authHeader)
        return call.callApi()!!
    }

    suspend fun terminateCmSession(sessionToken:String): SuccessResponse {
        val authHeader = ChatServerBaseApi.getJwtAuthHeader(sessionToken)
        val call = ChatServerBaseApi.getRetrofitInstance()
                                        .create(CmSessionApi::class.java)
                                        .requestCmSessionTermination(authHeader)
        return call.callApi()!!
    }

    suspend fun terminateChatSession(sessionToken:String): SuccessResponse {
        val authHeader = ChatServerBaseApi.getJwtAuthHeader(sessionToken)
        val call = ChatServerBaseApi.getRetrofitInstance()
                                        .create(CmSessionApi::class.java)
                                        .requestChatSessionTermination(authHeader)
        return call.callApi()!!
    }

    suspend fun acceptChatRequest(sessionToken:String): SuccessResponse {
        val authHeader = ChatServerBaseApi.getJwtAuthHeader(sessionToken)
        val call = ChatServerBaseApi.getRetrofitInstance()
                                        .create(CmSessionApi::class.java)
                                        .acceptChatRequest(authHeader)
        return call.callApi()!!
    }

    suspend fun declineChatRequest(sessionToken:String): SuccessResponse {
        val authHeader = ChatServerBaseApi.getJwtAuthHeader(sessionToken)
        val call = ChatServerBaseApi.getRetrofitInstance()
                                        .create(CmSessionApi::class.java)
                                        .declineChatRequest(authHeader)
        return call.callApi()!!
    }
}