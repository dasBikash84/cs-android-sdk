package com.dasbikash.cs_android_sdk.example_app.client_api_service

import com.dasbikash.cs_android_sdk.example_app.model.TokenReqResponse
import com.dasbikash.cs_android_sdk.example_app.model.User
import retrofit2.Call
import retrofit2.http.*


internal interface AuthApi {

    @GET(CM_SESSION_TOKEN_REQ_PATH)
    fun requestSessionToken(@Header("Authorization") authString: String):Call<TokenReqResponse>

    @GET(CM_ACCESS_TOKEN_REQ_PATH)
    fun requestAccessToken(@Header("Authorization") authString: String):Call<TokenReqResponse>

    @GET(GET_USER_DETAILS_PATH)
    fun getUserDetails(@Header("Authorization") authString: String,
                       @Path("id") id:String):Call<User>

    companion object {
        private const val CM_SESSION_TOKEN_REQ_PATH = "user/generate-session-token"
        private const val CM_ACCESS_TOKEN_REQ_PATH = "user/generate-access-token"
        private const val GET_USER_DETAILS_PATH = "user/user-details/{id}"
    }
}