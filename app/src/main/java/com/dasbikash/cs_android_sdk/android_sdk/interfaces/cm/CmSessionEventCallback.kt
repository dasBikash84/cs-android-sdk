package com.dasbikash.cs_android_sdk.android_sdk.interfaces.cm

import com.dasbikash.cs_android_sdk.android_sdk.utils.runOnMainThread


abstract class CmSessionEventCallback {
    abstract fun onSessionSetUpSuccess()
    abstract fun onSessionSetUpFailure(ex:Throwable?)
    abstract fun onSessionTermination(ex:Throwable?)

    internal fun callOnSessionSetUpSuccess(){
        runOnMainThread { onSessionSetUpSuccess() }
    }

    internal fun callOnSessionSetUpFailure(ex:Throwable?){
        runOnMainThread {
            onSessionSetUpFailure(
                ex
            )
        }
    }

    internal fun callOnSessionTermination(ex:Throwable?){
        runOnMainThread {
            onSessionTermination(
                ex
            )
        }
    }
}