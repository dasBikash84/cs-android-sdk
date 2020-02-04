package com.dasbikash.cs_android_sdk.android_sdk.services

import android.app.Activity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.dasbikash.cs_android_sdk.android_sdk.data_service.UserSessionDataService
import com.dasbikash.cs_android_sdk.android_sdk.firebase.FireStoreConUtils
import com.dasbikash.cs_android_sdk.android_sdk.firebase.FireStoreUtils
import com.dasbikash.cs_android_sdk.android_sdk.interfaces.user.UserChatSessionHandler
import com.dasbikash.cs_android_sdk.android_sdk.interfaces.user.UserChatSessionTerminationEventCallback
import com.dasbikash.cs_android_sdk.android_sdk.interfaces.user.UserSessionEventCallback
import com.dasbikash.cs_android_sdk.android_sdk.model.ChatSession
import com.dasbikash.cs_android_sdk.android_sdk.model.ChatSessionState
import com.dasbikash.cs_android_sdk.android_sdk.model.fb.FbChatEntry
import com.dasbikash.cs_android_sdk.android_sdk.model.fb.FbChatSessionInfo
import com.dasbikash.cs_android_sdk.android_sdk.model.FbAccessTokenReqResponse
import com.dasbikash.cs_android_sdk.android_sdk.utils.ChatSessionManagerUtils
import com.dasbikash.cs_android_sdk.android_sdk.utils.debugLog
import com.dasbikash.cs_android_sdk.android_sdk.utils.debugStackTrace
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

internal class ChatSessionManager: DefaultLifecycleObserver {

    companion object{

        fun getHandlerForNewSession(userId:String,userSessionEventCallback: UserSessionEventCallback): ChatSessionManager {
            val chatSessionManager = ChatSessionManager()
            chatSessionManager.mChatSession =
                ChatSession(
                    userId = userId,
                    userSessionEventCallback = userSessionEventCallback
                )
            return chatSessionManager
        }
    }

    private var mChatSession: ChatSession?=null
    private var mChatInfoListener:ListenerRegistration?=null

    private val mDeliveredChatEntries = mutableMapOf<String, FbChatEntry>()

    override fun onDestroy(owner: LifecycleOwner) {
        clearChatSession(null)
    }

    private fun clearChatSession(ex: Throwable?) {
        ChatSessionManagerUtils.removeChatSessionManager(
            mChatSession?.userId
        )
        mChatSession?.userSessionEventCallback?.callOnChatSessionTermination(ex)
        mChatInfoListener?.remove()
        mDeliveredChatEntries.clear()
        mChatSession = null
    }

    suspend fun initSession(activity: Activity, fbAccessTokenReqResponse: FbAccessTokenReqResponse) {
        mChatSession?.currentFbChatSessionPath = fbAccessTokenReqResponse.fbPath!!
        mChatSession?.currentFbChatSessionInfo =
            FireStoreUtils
                .readDocument(mChatSession?.currentFbChatSessionPath!!, FbChatSessionInfo::class.java)!!
        mChatSession?.state = ChatSessionState.WAITING_CHAT_SET_UP
        setUpChatInfoListner()
        (activity as LifecycleOwner).lifecycle.addObserver(this)
        notifySessionConnectionSetUp()
    }

    private fun notifySessionConnectionSetUp() {
        mChatSession?.userSessionEventCallback?.callOnChatSessionConnectionSetup(mChatSession!!.getSessionId()!!)
    }

    private fun setUpChatInfoListner() {
        debugLog("setUpChatInfoListner")
        mChatInfoListener = FireStoreConUtils.getFsDocument(mChatSession?.currentFbChatSessionPath!!)
            .addSnapshotListener(object : EventListener<DocumentSnapshot> {
                override fun onEvent(snapshot: DocumentSnapshot?, exception: FirebaseFirestoreException?) {
                    if (snapshot !=null){
                        processChatInfoSnapshot(snapshot)
                    }else{
                        processChatInfoListenerException(exception)
                    }
                }
            })
    }

    private fun processChatInfoListenerException(exception: Throwable?) {
        mChatSession?.userSessionEventCallback?.callOnChatSessionSetUpFailure(exception)
        clearChatSession(exception)
    }

    private fun processChatInfoSnapshot(snapshot: DocumentSnapshot) {
        if (snapshot.exists()){
            snapshot.toObject(FbChatSessionInfo::class.java)?.let {
                debugLog(it.toString())
                debugLog(mChatSession?.state?.name ?: "No state")
                mChatSession?.currentFbChatSessionInfo = it
                if (mChatSession?.state == ChatSessionState.WAITING_CHAT_SET_UP &&
                        mChatSession?.currentFbChatSessionInfo?.clientCmId !=null &&
                        mChatSession?.currentFbChatSessionInfo?.readOn == true &&
                        mChatSession?.currentFbChatSessionInfo?.writeOn == true){
                    mChatSession?.state = ChatSessionState.CHATTING
                    notifyChatSetUp()
                }
                if (mChatSession?.state == ChatSessionState.CHATTING){
                    deliverChatEntriesToFrontEnd()
                }
            }
        }else {
            clearChatSession(null)
        }
    }

    private fun notifyChatSetUp() {
        mChatSession?.userSessionEventCallback?.callOnChatSessionSetUpSuccess(
            mChatSession!!.currentFbChatSessionInfo!!.clientCmId!!,
            mChatSession!!.getSessionId()!!
        )
    }

    private fun deliverChatEntriesToFrontEnd() {
        mChatSession?.currentFbChatSessionInfo?.chatEntries?.apply {

            mChatSession?.userSessionEventCallback?.callOnChatEntryReceive(
                FbChatEntry.getChatEntries(this))
        }
    }

    fun doOnSessionSetUpFailure(ex: Exception) {
        clearChatSession(ex)
    }

    fun getSessionHandler() = object :
        UserChatSessionHandler {
        override fun terminateChatSession(
            sessionAccessToken: String,
            userChatSessionTerminationEventCallback: UserChatSessionTerminationEventCallback
        ) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    UserSessionDataService.terminateChatSession(sessionAccessToken)
                }catch (ex:Throwable){
                    userChatSessionTerminationEventCallback.callOnSessionTerminationFailure(ex)
                }
            }
        }

        override fun postChatEntry(payload: String) {
            var lastChatEntryId:String?=null
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    mChatSession?.currentFbChatSessionInfo?.addNewEntry(payload,isUser = true)?.let {
                        lastChatEntryId = it.chatEntries?.keys?.last()
                        writeFbChatSessionInfo(it)
                    }
                    mChatSession?.userSessionEventCallback?.callOnChatEntryPostSuccess(payload)
                }catch (ex:Throwable){
                    debugLog("${ex.message} ${ex.cause?.message} ${ex.javaClass.simpleName}")
                    debugStackTrace(ex)
                    mChatSession?.userSessionEventCallback?.callOnChatEntryPostFailure(lastChatEntryId,ex)
                }
            }
            debugLog("postChatEntry: $payload")
        }

        override fun isWaitingForCm() = (mChatSession?.state == ChatSessionState.WAITING_CHAT_SET_UP)
        override fun getChatSessionDetails(): Pair<String, String>? {
            mChatSession?.let {
                return Pair(it.currentFbChatSessionInfo!!.clientCmId!!,it.getSessionId()!!)
            }
            return null
        }
    }

    private suspend fun writeFbChatSessionInfo(fbChatSessionInfo: FbChatSessionInfo) =
        FireStoreUtils.writeDocument(
            mChatSession?.currentFbChatSessionPath!!, fbChatSessionInfo
        )

    fun refreshFrontEndConnection(userSessionEventCallback: UserSessionEventCallback) {
        mChatSession?.userSessionEventCallback = userSessionEventCallback
        mChatSession?.state?.apply {
            when(this){
                ChatSessionState.WAITING_CHAT_SET_UP -> {
                    notifySessionConnectionSetUp()
                }
                ChatSessionState.CHATTING ->{
                    mChatSession?.state = ChatSessionState.WAITING_CHAT_SET_UP
                    mChatInfoListener?.remove()
                    setUpChatInfoListner()
                }
            }
        }
    }
}