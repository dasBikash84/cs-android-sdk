package com.dasbikash.cs_android_sdk.android_sdk

import android.app.Activity
import com.dasbikash.cs_android_sdk.android_sdk.interfaces.user.UserChatSessionHandler
import com.dasbikash.cs_android_sdk.android_sdk.interfaces.user.UserSessionEventCallback
import com.dasbikash.cs_android_sdk.android_sdk.utils.ChatSessionManagerUtils

object UserSessionService {

    fun startCmSession(activity: Activity, userId:String,sessionToken:String,
                       userSessionEventCallback: UserSessionEventCallback
    ): UserChatSessionHandler {
        return ChatSessionManagerUtils.startChatSession(activity,userId,sessionToken,userSessionEventCallback)
    }
}