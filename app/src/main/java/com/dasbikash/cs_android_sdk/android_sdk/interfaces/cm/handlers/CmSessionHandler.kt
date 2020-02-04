package com.dasbikash.cs_android_sdk.android_sdk.interfaces.cm.handlers

import com.dasbikash.cs_android_sdk.android_sdk.interfaces.cm.CmSessionTerminationEventCallback

//In response to session set-up request SDK will return an instance
//of this class to FE for session status monitor and termination
//On session set-up request SDK will create an instance of CmSession
//class and attach with with handler
//Which SDK will update as per session state
interface CmSessionHandler{
    fun terminateSession(sessionAccessToken:String, cmSessionTerminationEventCallback: CmSessionTerminationEventCallback)
}