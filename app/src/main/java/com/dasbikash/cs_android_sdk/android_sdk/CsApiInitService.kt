package com.dasbikash.cs_android_sdk.android_sdk

import android.content.Context
import com.dasbikash.cs_android_sdk.android_sdk.data_service.api.ChatServerBaseApi

object CsApiInitService {

    fun saveBaseApiPath(context: Context, path:String):Boolean{
        return ChatServerBaseApi.saveBaseApiPath(context, path)
    }

    fun initApi(context: Context){
        ChatServerBaseApi.init(context)
    }

    fun getBaseApiPath(context: Context):String{
        return ChatServerBaseApi.getBaseApiPath(context)
    }
}