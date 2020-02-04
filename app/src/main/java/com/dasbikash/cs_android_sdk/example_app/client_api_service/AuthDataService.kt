package com.dasbikash.cs_android_sdk.example_app.client_api_service


import com.dasbikash.cs_android_sdk.android_sdk.data_service.api.callApi
import com.dasbikash.cs_android_sdk.example_app.model.User
import okhttp3.Credentials

internal object AuthDataService {

    suspend fun getSessionToken(userName:String,password:String):String{
        val call = ClientBeBaseApi.getRetrofitInstance()
            .create(AuthApi::class.java)
            .requestSessionToken(Credentials.basic(userName,password))
        return call.callApi()!!.token!!
    }

    suspend fun getAccessToken(userName:String,password:String):String{
        val call = ClientBeBaseApi.getRetrofitInstance()
            .create(AuthApi::class.java)
            .requestAccessToken(Credentials.basic(userName,password))
        return call.callApi()!!.token!!
    }

    suspend fun findUser(userName:String, password:String,
                         userId:String): User {
        val call = ClientBeBaseApi.getRetrofitInstance()
            .create(AuthApi::class.java)
            .getUserDetails(Credentials.basic(userName,password),userId)
        return call.callApi()!!
    }
}