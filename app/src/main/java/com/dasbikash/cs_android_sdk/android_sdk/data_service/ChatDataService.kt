package com.dasbikash.cs_android_sdk.android_sdk.data_service

import com.dasbikash.cs_android_sdk.android_sdk.data_service.api.ChatDataApi
import com.dasbikash.cs_android_sdk.android_sdk.data_service.api.ChatServerBaseApi
import com.dasbikash.cs_android_sdk.android_sdk.data_service.api.callApi
import com.dasbikash.cs_android_sdk.android_sdk.model.public_models.ChatSessionInfo
import com.dasbikash.cs_android_sdk.android_sdk.model.public_models.response.ChatSessionData
import com.dasbikash.cs_android_sdk.android_sdk.model.public_models.response.ChatSessionInfoData
import retrofit2.Call

internal object ChatDataService {

    fun getChatSessionInfoDataCall(accessToken:String): Call<ChatSessionInfoData> {
        val authHeader = ChatServerBaseApi.getJwtAuthHeader(accessToken)
        val call = ChatServerBaseApi.getRetrofitInstance()
                                        .create(ChatDataApi::class.java)
                                        .getAllChatSessionInfo(authHeader)
        return call
    }

    suspend fun getChatSessionInfoData(accessToken:String): ChatSessionInfoData {
        val authHeader = ChatServerBaseApi.getJwtAuthHeader(accessToken)
        val call = ChatServerBaseApi.getRetrofitInstance()
                                        .create(ChatDataApi::class.java)
                                        .getAllChatSessionInfo(authHeader)
        return call.callApi()!!
    }

    fun getChatSessionDataCall(accessToken:String, chatSessionInfo: ChatSessionInfo): Call<ChatSessionData>{
        val authHeader = ChatServerBaseApi.getJwtAuthHeader(accessToken)
        return ChatServerBaseApi.getRetrofitInstance()
            .create(ChatDataApi::class.java)
            .getChatSessionData(authHeader,chatSessionInfo.id!!)
    }

    suspend fun getChatSessionData(accessToken:String, chatSessionInfo: ChatSessionInfo): ChatSessionData {
        val authHeader = ChatServerBaseApi.getJwtAuthHeader(accessToken)
        val call = ChatServerBaseApi.getRetrofitInstance()
                                        .create(ChatDataApi::class.java)
                                        .getChatSessionData(authHeader,chatSessionInfo.id!!)
        return call.callApi()!!
    }
}