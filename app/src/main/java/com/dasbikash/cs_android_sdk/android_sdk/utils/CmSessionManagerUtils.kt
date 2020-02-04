package com.dasbikash.cs_android_sdk.android_sdk.utils

import android.app.Activity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.dasbikash.cs_android_sdk.android_sdk.data_service.CmSessionDataService
import com.dasbikash.cs_android_sdk.android_sdk.exception.CmSessionInitiationException
import com.dasbikash.cs_android_sdk.android_sdk.firebase.FireStoreConUtils
import com.dasbikash.cs_android_sdk.android_sdk.interfaces.cm.CmChatSessionEventCallback
import com.dasbikash.cs_android_sdk.android_sdk.interfaces.cm.CmChatSessionRequestCallback
import com.dasbikash.cs_android_sdk.android_sdk.interfaces.cm.CmSessionEventCallback
import com.dasbikash.cs_android_sdk.android_sdk.interfaces.cm.handlers.CmSessionHandler
import com.dasbikash.cs_android_sdk.android_sdk.services.CmSessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

internal object CmSessionManagerUtils: DefaultLifecycleObserver {

    private val cmSessionManagerMap = mutableMapOf<String, CmSessionManager>()
    private var syncedWithLifeCycleOwner:Boolean = false

    override fun onDestroy(owner: LifecycleOwner) {
        cmSessionManagerMap.clear()
        syncedWithLifeCycleOwner = false
    }

    fun removeCmSessionManager(key:String?){
        key?.let{ cmSessionManagerMap.remove(it)}
    }

    fun startCmSession(activity: Activity, cmId:String, sessionToken:String,
                       cmSessionEventCallback: CmSessionEventCallback,
                       cmChatSessionRequestCallback: CmChatSessionRequestCallback,
                       cmChatSessionEventCallback: CmChatSessionEventCallback
    ): CmSessionHandler {
        //launch session set up in back ground
        cmSessionManagerMap.get(cmId)?.let {
            it.refreshFrontEndConnection(
                activity,
                cmSessionEventCallback,
                cmChatSessionRequestCallback,
                cmChatSessionEventCallback
            )
            return it.getSessionHandler()
        }
        val cmSessionManager = CmSessionManager
                                    .getNewSessionManager(
                                        cmId,cmSessionEventCallback,
                                        cmChatSessionRequestCallback,cmChatSessionEventCallback)
        cmSessionManagerMap.put(cmId,cmSessionManager)
        launchSessionSetUp(activity, cmId,sessionToken,cmSessionManager)
        //create handler and return
        return cmSessionManager.getSessionHandler()
    }

    private fun launchSessionSetUp(activity: Activity, cmId:String,sessionToken:String,
                                   cmSessionManager: CmSessionManager
    ) {
        //have to run session set up on back-ground
        //have to tie session set up with activity life cycle
        GlobalScope.launch(Dispatchers.IO) {
            try {
                CmSessionDataService.requestFbAccessToken(sessionToken).apply {
                    this.validate()
                    FireStoreConUtils.loginForManualChat(activity, this)
                    cmSessionManager.initSession(activity, this)
                }
                if (!syncedWithLifeCycleOwner){
                    (activity as LifecycleOwner).lifecycle.addObserver(this@CmSessionManagerUtils)
                    syncedWithLifeCycleOwner = true
                }
            }catch (ex:Throwable){
                ex.printStackTrace()
                cmSessionManager.doOnSessionSetUpFailure(CmSessionInitiationException(ex))
                cmSessionManagerMap.remove(cmId)
            }
        }
    }
}