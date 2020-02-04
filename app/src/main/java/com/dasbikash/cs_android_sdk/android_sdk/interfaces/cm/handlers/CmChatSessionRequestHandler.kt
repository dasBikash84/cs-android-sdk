package com.dasbikash.cs_android_sdk.android_sdk.interfaces.cm.handlers

interface CmChatSessionRequestHandler{
    fun acceptCall(sessionToken:String,welcomeResponse:String?=null)
    fun rejectCall(sessionToken:String)
}