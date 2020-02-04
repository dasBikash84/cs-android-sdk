package com.dasbikash.cs_android_sdk.android_sdk.data_service.api

import com.dasbikash.cs_android_sdk.android_sdk.model.public_models.response.AutoChatTokenReqResponse
import com.dasbikash.cs_android_sdk.android_sdk.model.public_models.response.SuccessResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header


internal interface AutoChatSessionApi {

    @GET(GET_FB_LOGIN_TOKEN_PATH)
    fun getFbLoginToken(@Header("Authorization") authString: String):Call<AutoChatTokenReqResponse>

    @GET(TERMINATE_AUTO_CHAT_SESSION_PATH)
    fun terminateSession(@Header("Authorization") authString: String):Call<SuccessResponse>

    companion object {
        private const val AUTO_CHAT_SESSION_API_BASE_PATH = "auto-chat-session"
        private const val GET_FB_LOGIN_TOKEN_PATH = AUTO_CHAT_SESSION_API_BASE_PATH + "/login-token"
        private const val TERMINATE_AUTO_CHAT_SESSION_PATH = AUTO_CHAT_SESSION_API_BASE_PATH + "/terminate"
    }
}