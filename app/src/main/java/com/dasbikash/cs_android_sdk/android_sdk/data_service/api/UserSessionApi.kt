package com.dasbikash.cs_android_sdk.android_sdk.data_service.api

import com.dasbikash.cs_android_sdk.android_sdk.model.FbAccessTokenReqResponse
import com.dasbikash.cs_android_sdk.android_sdk.model.public_models.response.SuccessResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header


internal interface UserSessionApi {

    @GET(USER_REQUEST_CHAT_SESSION_PATH)
    fun requestChatSession(@Header("Authorization") authString: String):Call<FbAccessTokenReqResponse>

    @GET(USER_END_CHAT_SESSION_PATH)
    fun requestChatSessionTermination(@Header("Authorization") authString: String):Call<SuccessResponse>

    companion object {
        private const val USER_API_BASE_PATH = "client-user"
        private const val USER_END_CHAT_SESSION_PATH = USER_API_BASE_PATH +"/end-chat-session"
        private const val USER_REQUEST_CHAT_SESSION_PATH = USER_API_BASE_PATH +"/request-chat-session"
    }
}