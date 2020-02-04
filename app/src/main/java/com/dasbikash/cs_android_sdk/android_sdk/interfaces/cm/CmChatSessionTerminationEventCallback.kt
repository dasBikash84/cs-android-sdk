package com.dasbikash.cs_android_sdk.android_sdk.interfaces.cm

import com.dasbikash.cs_android_sdk.android_sdk.utils.runOnMainThread

abstract class CmChatSessionTerminationEventCallback{

    abstract fun onChatSessionTerminationFailure(ex:Throwable?)

    internal fun callOnChatSessionTerminationFailure(ex:Throwable?){
        runOnMainThread {
            onChatSessionTerminationFailure(
                ex
            )
        }
    }
}