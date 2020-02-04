package com.dasbikash.cs_android_sdk.android_sdk.interfaces.user

import com.dasbikash.cs_android_sdk.android_sdk.model.public_models.ChatEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

abstract class UserSessionEventCallback {
    abstract fun onChatSessionSetUpFailure(ex:Throwable?)
    abstract fun onChatSessionConnectionSetup(chatSessionId:String)
    abstract fun onChatSessionSetUpSuccess(userId:String,chatSessionId:String)
    abstract fun onChatEntryReceive(chatEntryList: List<ChatEntry>)
    abstract fun onChatEntryPostSuccess(payload:String)
    abstract fun onChatEntryPostFailure(lastChatEntryId:String?,ex:Throwable?)
    abstract fun onChatSessionTermination(ex:Throwable?)

    internal fun callOnChatSessionSetUpFailure(ex:Throwable?) {
        runOnMainThread { onChatSessionSetUpFailure(ex)}
    }
    internal fun callOnChatSessionConnectionSetup(chatSessionId:String){
        runOnMainThread { onChatSessionConnectionSetup(chatSessionId) }
    }
    internal fun callOnChatSessionSetUpSuccess(userId:String,chatSessionId:String){
        runOnMainThread { onChatSessionSetUpSuccess(userId,chatSessionId) }
    }
    internal fun callOnChatEntryReceive(chatEntryList: List<ChatEntry>){
        runOnMainThread { onChatEntryReceive(chatEntryList) }
    }
    internal fun callOnChatEntryPostFailure(lastChatEntryId:String?,ex:Throwable?){
        runOnMainThread { onChatEntryPostFailure(lastChatEntryId,ex) }
    }
    internal fun callOnChatSessionTermination(ex:Throwable?){
        runOnMainThread { onChatSessionTermination(ex) }
    }
    internal fun callOnChatEntryPostSuccess(payload:String){
        runOnMainThread { onChatEntryPostSuccess(payload) }
    }
}

internal fun runOnMainThread(task: () -> Any?){
    GlobalScope.launch(Dispatchers.Main) {
        task()
    }
}