package com.dasbikash.cs_android_sdk.android_sdk.data_service.api

import com.dasbikash.cs_android_sdk.android_sdk.model.public_models.response.ChatSessionData
import com.dasbikash.cs_android_sdk.android_sdk.model.public_models.response.ChatSessionInfoData
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path


internal interface ChatDataApi {
    @GET(ALL_CHAT_SESSION_INFO_PATH)
    fun getAllChatSessionInfo(@Header("Authorization") authString: String):Call<ChatSessionInfoData>

    @GET(CHAT_SESSION_DATA_PATH)
    fun getChatSessionData(@Header("Authorization") authString: String,
                          @Path("id") id:String):Call<ChatSessionData>

    companion object {
        private const val CHAT_API_BASE_PATH = "chat"
        private const val ALL_CHAT_SESSION_INFO_PATH = CHAT_API_BASE_PATH +"/all-chat-session-info"
        private const val CHAT_SESSION_DATA_PATH = CHAT_API_BASE_PATH +"/chat-session-data/{id}"
    }
}