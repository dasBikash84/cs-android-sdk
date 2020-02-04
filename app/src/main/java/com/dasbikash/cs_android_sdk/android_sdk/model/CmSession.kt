package com.dasbikash.cs_android_sdk.android_sdk.model

import com.dasbikash.cs_android_sdk.android_sdk.interfaces.cm.CmChatSessionEventCallback
import com.dasbikash.cs_android_sdk.android_sdk.interfaces.cm.CmChatSessionRequestCallback
import com.dasbikash.cs_android_sdk.android_sdk.interfaces.cm.CmSessionEventCallback
import com.dasbikash.cs_android_sdk.android_sdk.interfaces.cm.CmSessionTerminationEventCallback
import com.dasbikash.cs_android_sdk.android_sdk.model.fb.FbChatSessionInfo
import com.dasbikash.cs_android_sdk.android_sdk.model.fb.FbCmSessionInfo
import kotlinx.coroutines.Job

internal class CmSession(
    val cmId:String,
    var cmSessionEventCallback: CmSessionEventCallback,
    var cmChatSessionRequestCallback: CmChatSessionRequestCallback,
    var cmSessionTerminationEventCallback: CmSessionTerminationEventCallback?=null,
    var currentFbCmSessionPath:String?=null,
    var currentFbCmSessionInfo: FbCmSessionInfo?=null,
    var pingJob: Job?=null,
    var state: CmSessionState = CmSessionState.PINGING,
    var currentFbChatSessionInfo: FbChatSessionInfo?=null,
    var cmChatSessionEventCallback: CmChatSessionEventCallback
){
    fun pingEnabled() = (state== CmSessionState.PINGING)
    companion object{
        const val PER_CYCLE_PING_FREQUENCY = 3
    }
}