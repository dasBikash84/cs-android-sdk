package com.dasbikash.cs_android_sdk.android_sdk.interfaces.cm.handlers

import com.dasbikash.cs_android_sdk.android_sdk.interfaces.cm.CmChatSessionTerminationEventCallback

interface CmChatSessionHandler{
    fun postChatEntry(payload:String)
    fun terminateChat(sessionAccessToken:String,
                      cmChatSessionTerminationEventCallback: CmChatSessionTerminationEventCallback
    )
    fun getChatSessionDetails():Pair<String,String>
}