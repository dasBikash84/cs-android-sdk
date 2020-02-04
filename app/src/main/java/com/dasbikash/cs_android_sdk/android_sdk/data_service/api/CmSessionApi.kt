package com.dasbikash.cs_android_sdk.android_sdk.data_service.api

import com.dasbikash.cs_android_sdk.android_sdk.model.FbAccessTokenReqResponse
import com.dasbikash.cs_android_sdk.android_sdk.model.public_models.response.SuccessResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header


internal interface CmSessionApi {

    @GET(CM_SESSION_START_REQ_PATH)
    fun requestCmSession(@Header("Authorization") authString: String):Call<FbAccessTokenReqResponse>

    @GET(CM_SESSION_END_REQ_PATH)
    fun requestCmSessionTermination(@Header("Authorization") authString: String):Call<SuccessResponse>

    @GET(CM_END_CHAT_SESSION_PATH)
    fun requestChatSessionTermination(@Header("Authorization") authString: String):Call<SuccessResponse>

    @GET(CM_ACCEPT_CHAT_REQUEST_PATH)
    fun acceptChatRequest(@Header("Authorization") authString: String):Call<SuccessResponse>

    @GET(CM_DECLINE_CHAT_REQUEST_PATH)
    fun declineChatRequest(@Header("Authorization") authString: String):Call<SuccessResponse>

    companion object {
        private const val CM_API_BASE_PATH = "client-cm/"
        private const val CM_SESSION_START_REQ_PATH = CM_API_BASE_PATH +"start-session"
        private const val CM_SESSION_END_REQ_PATH = CM_API_BASE_PATH +"end-session"
        private const val CM_END_CHAT_SESSION_PATH = CM_API_BASE_PATH +"end-chat-session"
        private const val CM_ACCEPT_CHAT_REQUEST_PATH = CM_API_BASE_PATH +"accept-chat-request"
        private const val CM_DECLINE_CHAT_REQUEST_PATH = CM_API_BASE_PATH +"decline-chat-request"
    }
}