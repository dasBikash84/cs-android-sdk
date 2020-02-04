package com.dasbikash.cs_android_sdk.android_sdk

import android.app.Activity
import com.dasbikash.cs_android_sdk.android_sdk.interfaces.cm.CmChatSessionEventCallback
import com.dasbikash.cs_android_sdk.android_sdk.interfaces.cm.CmChatSessionRequestCallback
import com.dasbikash.cs_android_sdk.android_sdk.interfaces.cm.CmSessionEventCallback
import com.dasbikash.cs_android_sdk.android_sdk.interfaces.cm.handlers.CmSessionHandler
import com.dasbikash.cs_android_sdk.android_sdk.utils.CmSessionManagerUtils

object CmSessionService {

    fun startCmSession(activity: Activity, cmId:String, sessionToken:String,
                       cmSessionEventCallback: CmSessionEventCallback,
                       cmChatSessionRequestCallback: CmChatSessionRequestCallback,
                       cmChatSessionEventCallback: CmChatSessionEventCallback
    ): CmSessionHandler {
        return CmSessionManagerUtils.startCmSession(activity,cmId,sessionToken,cmSessionEventCallback,
                                                        cmChatSessionRequestCallback,cmChatSessionEventCallback)
    }
}