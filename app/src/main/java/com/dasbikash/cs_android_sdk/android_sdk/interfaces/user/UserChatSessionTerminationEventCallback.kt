package com.dasbikash.cs_android_sdk.android_sdk.interfaces.user

import com.dasbikash.cs_android_sdk.android_sdk.interfaces.user.runOnMainThread

abstract class UserChatSessionTerminationEventCallback {
    abstract fun onSessionTerminationFailure(ex:Throwable?)

    internal fun callOnSessionTerminationFailure(ex:Throwable?) {
        runOnMainThread { onSessionTerminationFailure(ex)}
    }
}