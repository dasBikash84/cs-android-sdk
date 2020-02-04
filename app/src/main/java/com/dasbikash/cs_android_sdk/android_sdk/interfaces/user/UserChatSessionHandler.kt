package com.dasbikash.cs_android_sdk.android_sdk.interfaces.user

import com.dasbikash.cs_android_sdk.android_sdk.interfaces.user.UserChatSessionTerminationEventCallback


interface UserChatSessionHandler {
    fun postChatEntry(payload:String)
    fun terminateChatSession(sessionAccessToken:String,
                             userChatSessionTerminationEventCallback: UserChatSessionTerminationEventCallback
    )
    fun isWaitingForCm():Boolean
    fun getChatSessionDetails():Pair<String,String>?
}