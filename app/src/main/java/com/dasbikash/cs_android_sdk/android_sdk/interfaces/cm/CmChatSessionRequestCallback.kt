package com.dasbikash.cs_android_sdk.android_sdk.interfaces.cm

import com.dasbikash.cs_android_sdk.android_sdk.interfaces.cm.handlers.CmChatSessionRequestHandler
import com.dasbikash.cs_android_sdk.android_sdk.interfaces.user.runOnMainThread

abstract class CmChatSessionRequestCallback{
    abstract fun onChatSessionRequest(cmChatSessionRequestHandler: CmChatSessionRequestHandler)
    abstract fun onChatRequestDrop(ex:Throwable?)
    abstract fun onChatSessionSetUpSuccess()

    internal fun callOnChatSessionRequest(cmChatSessionRequestHandler: CmChatSessionRequestHandler){
        runOnMainThread { onChatSessionRequest(cmChatSessionRequestHandler) }
    }
    internal fun callOnChatRequestDrop(ex:Throwable?){
        runOnMainThread { onChatRequestDrop(ex) }
    }
    internal fun callOnChatSessionSetUpSuccess(){
        runOnMainThread { onChatSessionSetUpSuccess() }
    }
}