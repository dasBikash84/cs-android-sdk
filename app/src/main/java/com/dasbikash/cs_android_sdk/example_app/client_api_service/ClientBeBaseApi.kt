package com.dasbikash.cs_android_sdk.example_app.client_api_service

import com.google.gson.Gson
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

internal object ClientBeBaseApi {

    private const val BASE_API_PATH = "http://192.168.0.111:9110/client2/"

    fun getRetrofitInstance():Retrofit {
        return Retrofit.Builder().baseUrl(BASE_API_PATH)
            .addConverterFactory(GsonConverterFactory.create(Gson()))
            .build()
    }
}