package com.dasbikash.cs_android_sdk.android_sdk

import com.dasbikash.cs_android_sdk.android_sdk.data_service.ChatDataService
import com.dasbikash.cs_android_sdk.android_sdk.exception.RemoteApiException
import com.dasbikash.cs_android_sdk.android_sdk.interfaces.CsDataRequestCallback
import com.dasbikash.cs_android_sdk.android_sdk.model.public_models.ChatSessionInfo
import com.dasbikash.cs_android_sdk.android_sdk.model.public_models.response.ChatSessionData
import com.dasbikash.cs_android_sdk.android_sdk.model.public_models.response.ChatSessionInfoData
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object CsChatDataService {

    fun requestChatSessionInfoData(accessToken:String,
                                   callBack: CsDataRequestCallback<ChatSessionInfoData>
    ){

        callBack.job = GlobalScope.launch {
            try {
                callBack.callOnDataResponse(ChatDataService.getChatSessionInfoData(accessToken))
            }catch (ex:Throwable){
                ex.printStackTrace()
                callBack.callOnError(ex)
            }
        }
    }

    fun getCmChatSessionInfoData(accessToken:String)
        : ChatSessionInfoData {
        try {
            val response = ChatDataService.getChatSessionInfoDataCall(accessToken).execute()
            return response.body()!!
        }catch (ex:Throwable){
            ex.printStackTrace()
            throw RemoteApiException()
        }
    }

    fun requestChatSessionData(accessToken:String, chatSessionInfo: ChatSessionInfo,
                               callBack: CsDataRequestCallback<ChatSessionData>
    ){

        callBack.job = GlobalScope.launch {
            try {
                callBack.callOnDataResponse(ChatDataService.getChatSessionData(accessToken,chatSessionInfo))
            }catch (ex:Throwable){
                ex.printStackTrace()
                callBack.callOnError(ex)
            }
        }
    }

    fun getChatSessionData(accessToken:String,chatSessionInfo: ChatSessionInfo)
        : ChatSessionData {
        try {
            val response = ChatDataService.getChatSessionDataCall(accessToken,chatSessionInfo).execute()
            return response.body()!!
        }catch (ex:Throwable){
            ex.printStackTrace()
            throw RemoteApiException()
        }
    }
}