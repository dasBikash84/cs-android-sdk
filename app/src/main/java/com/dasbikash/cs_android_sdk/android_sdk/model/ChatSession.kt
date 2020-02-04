package com.dasbikash.cs_android_sdk.android_sdk.model

import com.dasbikash.cs_android_sdk.android_sdk.interfaces.user.UserSessionEventCallback
import com.dasbikash.cs_android_sdk.android_sdk.model.fb.FbChatSessionInfo

internal class ChatSession(
    val userId:String,
    var currentFbChatSessionPath:String?=null,
    var userSessionEventCallback: UserSessionEventCallback?=null,
    var currentFbChatSessionInfo: FbChatSessionInfo?=null,
    var state: ChatSessionState = ChatSessionState.WAITING_CHAT_SET_UP
){
    fun getSessionId():String?{
        return currentFbChatSessionPath?.split("/")?.last()
    }
}