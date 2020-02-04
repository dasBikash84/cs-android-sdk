package com.dasbikash.cs_android_sdk.android_sdk.utils

import android.app.Activity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.dasbikash.cs_android_sdk.android_sdk.data_service.UserSessionDataService
import com.dasbikash.cs_android_sdk.android_sdk.exception.ChatSessionInitiationException
import com.dasbikash.cs_android_sdk.android_sdk.firebase.FireStoreConUtils
import com.dasbikash.cs_android_sdk.android_sdk.interfaces.user.UserChatSessionHandler
import com.dasbikash.cs_android_sdk.android_sdk.interfaces.user.UserSessionEventCallback
import com.dasbikash.cs_android_sdk.android_sdk.services.ChatSessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

internal object ChatSessionManagerUtils: DefaultLifecycleObserver {

    private val chatSessionManagerMap = mutableMapOf<String, ChatSessionManager>()
    private var syncedWithLifeCycleOwner:Boolean = false

    override fun onDestroy(owner: LifecycleOwner) {
        chatSessionManagerMap.clear()
        syncedWithLifeCycleOwner = false
    }


    fun removeChatSessionManager(key:String?){
        key?.let{ chatSessionManagerMap.remove(it)}
    }

    fun startChatSession(activity: Activity, userId:String,sessionToken:String,
                         userSessionEventCallback: UserSessionEventCallback
    ): UserChatSessionHandler {
        chatSessionManagerMap.get(userId)?.let {
            it.refreshFrontEndConnection(userSessionEventCallback)
            return it.getSessionHandler()
        }
        //launch session set up in back ground
        val chatSessionManager = ChatSessionManager.getHandlerForNewSession(userId,userSessionEventCallback)
        chatSessionManagerMap.put(userId,chatSessionManager)
        launchSessionSetUp(activity, userId,sessionToken,chatSessionManager)
        //create handler and return
        return chatSessionManager.getSessionHandler()
    }

    private fun launchSessionSetUp(activity: Activity, userId:String,sessionToken:String,
                                   chatSessionManager: ChatSessionManager
    ) {
        //have to run session set up on back-ground
        //have to tie session set up with activity life cycle
        GlobalScope.launch(Dispatchers.IO) {
            try {
                UserSessionDataService.requestChatSession(sessionToken).apply {
                    debugLog(this.toString())
                    this.validate()
                    FireStoreConUtils.loginForManualChat(activity, this)
                    chatSessionManager.initSession(activity, this)
                }

                if (!syncedWithLifeCycleOwner){
                    (activity as LifecycleOwner).lifecycle.addObserver(this@ChatSessionManagerUtils)
                    syncedWithLifeCycleOwner = true
                }
            }catch (ex:Throwable){
                debugStackTrace(ex)
                chatSessionManager.doOnSessionSetUpFailure(ChatSessionInitiationException(ex))
                chatSessionManagerMap.remove(userId)
            }
        }
    }
}