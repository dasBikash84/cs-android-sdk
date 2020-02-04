package com.dasbikash.cs_android_sdk.android_sdk.interfaces.cm

import com.dasbikash.cs_android_sdk.android_sdk.interfaces.cm.handlers.CmChatSessionHandler
import com.dasbikash.cs_android_sdk.android_sdk.model.public_models.ChatEntry
import com.dasbikash.cs_android_sdk.android_sdk.utils.runOnMainThread

abstract class CmChatSessionEventCallback {
    abstract fun onChatSessionSetUpFailure(ex:Throwable?)
    abstract fun onChatSessionSetUpSuccess(userId:String, chatSessionId:String, cmChatSessionHandler: CmChatSessionHandler)
    abstract fun onNewChatEntry(chatEntryList: List<ChatEntry>)
    abstract fun onChatEntryPostSuccess(payload: String)
    abstract fun onChatEntryPostFailure(chatEntryId: String?,ex:Throwable?)
    abstract fun onChatSessionTermination(ex:Throwable?)

    internal fun callOnChatSessionSetUpFailure(ex:Throwable?) {
        runOnMainThread {
            onChatSessionSetUpFailure(
                ex
            )
        }
    }
    internal fun callOnChatSessionSetUpSuccess(userId:String, chatSessionId:String, cmChatSessionHandler: CmChatSessionHandler){
        runOnMainThread {
            onChatSessionSetUpSuccess(
                userId,
                chatSessionId,
                cmChatSessionHandler
            )
        }
    }
    internal fun callOnNewChatEntry(chatEntryList: List<ChatEntry>){
        runOnMainThread {
            onNewChatEntry(
                chatEntryList
            )
        }
    }
    internal fun callOnChatEntryPostSuccess(payload: String){
        runOnMainThread {
            onChatEntryPostSuccess(
                payload
            )
        }
    }
    internal fun callOnChatEntryPostFailure(chatEntryId: String?,ex:Throwable?){
        runOnMainThread {
            onChatEntryPostFailure(
                chatEntryId,ex
            )
        }
    }
    internal fun callOnChatSessionTermination(ex:Throwable?){
        runOnMainThread {
            onChatSessionTermination(
                ex
            )
        }
    }
}