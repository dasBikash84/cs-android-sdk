package com.dasbikash.cs_android_sdk.android_sdk.interfaces.cm

import com.dasbikash.cs_android_sdk.android_sdk.utils.runOnMainThread


//Implementation will be provided by front-end app during session termination request
abstract class CmSessionTerminationEventCallback{
    abstract fun onSessionTerminationSuccess()
    abstract fun onSessionTerminationFailure(ex:Throwable?)

    internal fun callOnSessionTerminationSuccess(){
        runOnMainThread { onSessionTerminationSuccess() }
    }
    internal fun callOnSessionTerminationFailure(ex:Throwable?){
        runOnMainThread {
            onSessionTerminationFailure(
                ex
            )
        }
    }
}