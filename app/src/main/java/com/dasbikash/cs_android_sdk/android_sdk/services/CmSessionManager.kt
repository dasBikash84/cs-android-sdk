package com.dasbikash.cs_android_sdk.android_sdk.services

import android.app.Activity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.dasbikash.cs_android_sdk.android_sdk.data_service.CmSessionDataService
import com.dasbikash.cs_android_sdk.android_sdk.exception.CmSessionInitiationException
import com.dasbikash.cs_android_sdk.android_sdk.exception.FbDocumentReadException
import com.dasbikash.cs_android_sdk.android_sdk.exception.FbDocumentWriteException
import com.dasbikash.cs_android_sdk.android_sdk.firebase.FireStoreConUtils
import com.dasbikash.cs_android_sdk.android_sdk.firebase.FireStoreUtils
import com.dasbikash.cs_android_sdk.android_sdk.interfaces.cm.handlers.CmChatSessionHandler
import com.dasbikash.cs_android_sdk.android_sdk.interfaces.cm.handlers.CmChatSessionRequestHandler
import com.dasbikash.cs_android_sdk.android_sdk.interfaces.cm.handlers.CmSessionHandler
import com.dasbikash.cs_android_sdk.android_sdk.interfaces.cm.*
import com.dasbikash.cs_android_sdk.android_sdk.model.CmSession
import com.dasbikash.cs_android_sdk.android_sdk.model.CmSession.Companion.PER_CYCLE_PING_FREQUENCY
import com.dasbikash.cs_android_sdk.android_sdk.model.CmSessionState
import com.dasbikash.cs_android_sdk.android_sdk.model.fb.FbChatSessionInfo
import com.dasbikash.cs_android_sdk.android_sdk.model.fb.FbCmSessionInfo
import com.dasbikash.cs_android_sdk.android_sdk.model.FbAccessTokenReqResponse
import com.dasbikash.cs_android_sdk.android_sdk.utils.CmSessionManagerUtils
import com.dasbikash.cs_android_sdk.android_sdk.utils.debugLog
import com.dasbikash.cs_android_sdk.android_sdk.utils.debugStackTrace
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.*
import kotlin.random.Random

internal class CmSessionManager:DefaultLifecycleObserver {

    companion object{

        fun getNewSessionManager(cmId:String, cmSessionEventCallback: CmSessionEventCallback,
                                 cmChatSessionRequestCallback: CmChatSessionRequestCallback,
                                 cmChatSessionEventCallback: CmChatSessionEventCallback
        ): CmSessionManager {
            val cmSessionManager = CmSessionManager()
            cmSessionManager.mCurrentCmSession = CmSession(cmId = cmId,
                                                            cmSessionEventCallback=cmSessionEventCallback,
                                                            cmChatSessionRequestCallback = cmChatSessionRequestCallback,
                                                            cmChatSessionEventCallback = cmChatSessionEventCallback)
            return cmSessionManager
        }
    }

    private var mCurrentCmSession: CmSession?=null
    private var mSessionInfoListener:ListenerRegistration?=null
    private var mChatInfoListener:ListenerRegistration?=null
    private var mChatSessionResponseJob:Job?=null
    private var mWelcomeResponse:String? = null

    override fun onDestroy(owner: LifecycleOwner) {
        clearChatSession(null)
        clearSession(null)
    }

    suspend fun initSession(activity: Activity, fbAccessTokenReqResponse: FbAccessTokenReqResponse){

        mCurrentCmSession?.currentFbCmSessionPath = fbAccessTokenReqResponse.fbPath!!
        try {
            readCurrentSessionInfo()
                ?.let {
                    debugLog("Init: $it")
                    mCurrentCmSession?.currentFbCmSessionInfo = it
                    initPing()
                }
        }catch (ex: FbDocumentReadException){}

        if (mCurrentCmSession?.currentFbCmSessionInfo==null){
            throw CmSessionInitiationException()
        }
        setSessionInfoListener()
        notifySessionSetUpSuccess()
        (activity as LifecycleOwner).lifecycle.addObserver(this)
    }

    private fun setSessionInfoListener(){
        mSessionInfoListener =
            FireStoreConUtils.getFsDocument(mCurrentCmSession!!.currentFbCmSessionPath!!)
                .addSnapshotListener(object : EventListener<DocumentSnapshot>{
                    override fun onEvent(snapshot: DocumentSnapshot?, exception: FirebaseFirestoreException?) {
                        if (snapshot !=null){
                            processSessionInfoSnapshot(snapshot)
                        }else{
                            processSessionInfoListenerException(exception)
                        }
                    }
                })
    }

    private fun removeSessionInfoListener(){
        mSessionInfoListener?.remove()
        mSessionInfoListener = null
        mSessionInfoListener = null
    }

    private fun processSessionInfoListenerException(exception: FirebaseFirestoreException?) {
        debugLog("exception on  is SessionInfoListener.")
        clearSession(exception)
    }

    private fun clearSession(ex:Throwable?) {
        removeSessionInfoListener()
        stopPing()
        CmSessionManagerUtils.removeCmSessionManager(
            mCurrentCmSession?.cmId
        )
        mCurrentCmSession?.cmSessionEventCallback?.callOnSessionTermination(ex)
        mChatSessionResponseJob?.let {
            if (it.isActive){
                it.cancel()
            }
        }
        mCurrentCmSession = null
    }

    private fun processSessionInfoSnapshot(snapshot: DocumentSnapshot) {
        snapshot.toObject(FbCmSessionInfo::class.java).apply {
            if (this!=null) {
                if (mCurrentCmSession != null) {
                    mCurrentCmSession?.currentFbCmSessionInfo = this

                    debugLog("From processSessionInfoSnapshot: ${mCurrentCmSession?.currentFbCmSessionInfo}")
                    debugLog(mCurrentCmSession?.state.toString())

                    if (mCurrentCmSession?.currentFbCmSessionInfo !=null &&
                        mCurrentCmSession?.currentFbCmSessionInfo?.chatSessionRequest == true &&
//                        (mCurrentCmSession?.currentFbCmSessionInfo?.chatSessionRequestResponse== true) &&
                        mCurrentCmSession?.currentFbCmSessionInfo?.currentChatSessionPath!=null &&
                        mCurrentCmSession?.state != CmSessionState.CHATTING ){
                        initChat()
                    }else if(mCurrentCmSession?.currentFbCmSessionInfo !=null &&
                                mCurrentCmSession?.currentFbCmSessionInfo?.chatSessionRequest == true &&
                                mCurrentCmSession?.state == CmSessionState.PINGING){
                        mCurrentCmSession?.state = CmSessionState.WAITING_CHAT_SET_UP
                        mCurrentCmSession?.cmChatSessionRequestCallback?.callOnChatSessionRequest(
                            getChatSessionRequestHandler()
                        )
                    }else if(mCurrentCmSession?.currentFbCmSessionInfo !=null &&
                        mCurrentCmSession?.currentFbCmSessionInfo?.chatSessionRequest !=true &&
                        mCurrentCmSession?.currentFbCmSessionInfo?.currentChatSessionPath ==null &&
                        mCurrentCmSession?.state == CmSessionState.WAITING_CHAT_SET_UP){
                        cleanChatSessionOnSetUpFailure(null)
                    }
                } else {
                    debugLog("mCmSession is empty.")
                }
            }else{
                debugLog("cm_session snapshot is empty.")
            }
        }
    }

    private fun initChat() {
        GlobalScope.launch(Dispatchers.IO) {
            //Delay to avoid posting welcome message from multiple login instances.
            delay(Random(System.currentTimeMillis()).nextLong(2000L))
            val chatSessionInfo: FbChatSessionInfo?
            try {
                chatSessionInfo=
                    FireStoreUtils.readDocument(
                        mCurrentCmSession?.currentFbCmSessionInfo?.currentChatSessionPath!!,
                        FbChatSessionInfo::class.java
                    )
                debugLog("From initChat: $chatSessionInfo")
            }catch (ex:Throwable){
                debugStackTrace(ex)
                mCurrentCmSession?.state = CmSessionState.PINGING
                return@launch
            }
            mCurrentCmSession?.currentFbChatSessionInfo =chatSessionInfo
            setChatInfoListner()
            notifyChatSessionSetUpSuccess()
            mCurrentCmSession?.state = CmSessionState.CHATTING
            if (chatSessionInfo!!.chatEntries ==null && mWelcomeResponse!=null){
                chatSessionInfo.addNewEntry(mWelcomeResponse!!,isCm = true)?.let {
                    writeFbChatSessionInfo(it)
                }
            }
        }
    }

    private fun notifyChatSessionSetUpSuccess() {
        mCurrentCmSession?.cmChatSessionEventCallback
            ?.callOnChatSessionSetUpSuccess(
                mCurrentCmSession?.currentFbChatSessionInfo?.clientUserId!!,
                mCurrentCmSession?.currentFbCmSessionInfo?.currentChatSessionPath!!,
                getChatSessionHandler()
            )
        mCurrentCmSession?.cmChatSessionRequestCallback?.callOnChatSessionSetUpSuccess()
    }

    private suspend fun writeFbChatSessionInfo(fbChatSessionInfo: FbChatSessionInfo) =
        FireStoreUtils.writeDocument(
            mCurrentCmSession?.currentFbCmSessionInfo?.currentChatSessionPath!!, fbChatSessionInfo
        )

    private fun setChatInfoListner() {
        debugLog("setUpChatInfoListner")
        mChatInfoListener = FireStoreConUtils.getFsDocument(mCurrentCmSession?.currentFbCmSessionInfo?.currentChatSessionPath!!)
                .addSnapshotListener(object : EventListener<DocumentSnapshot>{
                    override fun onEvent(snapshot: DocumentSnapshot?, exception: FirebaseFirestoreException?) {
                        if (snapshot !=null){
                            processChatInfoSnapshot(snapshot)
                        }else{
                            processChatInfoListenerException(exception)
                        }
                    }
                })
    }

    private fun processChatInfoSnapshot(snapshot: DocumentSnapshot) {
        debugLog("processChatInfoSnapshot")
        if (snapshot.exists()){
            snapshot.toObject(FbChatSessionInfo::class.java)?.let {
                mCurrentCmSession?.currentFbChatSessionInfo = it
                deliverNewChatEntriesToFrontEnd()
                debugLog("processChatInfoSnapshot: $it")

            }
        }
    }

    private fun deliverNewChatEntriesToFrontEnd() {
        mCurrentCmSession?.cmChatSessionEventCallback?.callOnNewChatEntry(
            mCurrentCmSession?.currentFbChatSessionInfo?.getChatEntries() ?: emptyList()
        )
    }

    private fun processChatInfoListenerException(exception: FirebaseFirestoreException?) {
        debugLog("processChatInfoListenerException")
        mCurrentCmSession?.cmChatSessionEventCallback?.callOnChatSessionTermination(exception)
        clearChatSession(exception)
        mCurrentCmSession?.state = CmSessionState.PINGING
    }

    private fun getChatSessionHandler() = object :
        CmChatSessionHandler {
        override fun postChatEntry(payload: String) {
            var chatEntryId:String?=null
            GlobalScope.launch {
                try {
                    mCurrentCmSession?.currentFbChatSessionInfo?.addNewEntry(payload,isCm = true)?.let {
                        chatEntryId = it.chatEntries?.keys?.last()
                        writeFbChatSessionInfo(it)!!
                        mCurrentCmSession?.cmChatSessionEventCallback?.callOnChatEntryPostSuccess(
                            payload
                        )
                    }
                }catch (ex:Throwable){
                    debugStackTrace(ex)
                    mCurrentCmSession?.cmChatSessionEventCallback?.callOnChatEntryPostFailure(chatEntryId,ex)
                }
            }
            debugLog("postChatEntry: $payload")
        }

        override fun terminateChat(sessionAccessToken:String,
                                   cmChatSessionTerminationEventCallback: CmChatSessionTerminationEventCallback
        ) {

            GlobalScope.launch(Dispatchers.IO) {
                try {
                    CmSessionDataService.terminateChatSession(sessionAccessToken)
                }catch (ex:Throwable){
                    cmChatSessionTerminationEventCallback.callOnChatSessionTerminationFailure(ex)
                }
            }
            debugLog("terminateChat")
        }

        override fun getChatSessionDetails(): Pair<String, String> {
            return Pair( mCurrentCmSession?.currentFbChatSessionInfo?.clientUserId!!,
                mCurrentCmSession?.currentFbCmSessionInfo?.currentChatSessionPath!!)
        }
    }

    private fun getChatSessionRequestHandler(): CmChatSessionRequestHandler {
        return object :
            CmChatSessionRequestHandler {
            override fun acceptCall(sessionToken:String,welcomeResponse:String?) {
                acceptChatSessionRequest(sessionToken,welcomeResponse)
            }

            override fun rejectCall(sessionToken:String) {
                declineChatSessionRequest(sessionToken)
            }
        }
    }

    private fun declineChatSessionRequest(sessionToken:String) {
        var exception:Throwable? = null
        GlobalScope.launch(Dispatchers.IO) {
            try {
                CmSessionDataService.declineChatRequest(sessionToken)
            }catch (ex:Throwable){
                debugStackTrace(ex)
                exception=ex
            }finally {
                cleanChatSessionOnSetUpFailure(exception)
            }
        }
    }

    private fun cleanChatSessionOnSetUpFailure(exception: Throwable?=null) {
        debugLog("From cleanChatSessionOnSetUpFailure")
        mCurrentCmSession?.cmChatSessionRequestCallback?.callOnChatRequestDrop(exception)
        mCurrentCmSession?.state = CmSessionState.PINGING
    }

    private fun acceptChatSessionRequest(sessionToken:String,welcomeResponse:String?) {
        mWelcomeResponse = welcomeResponse
        GlobalScope.launch(Dispatchers.IO) {
            try {
                CmSessionDataService.acceptChatRequest(sessionToken)
            }catch (ex:Throwable){
                mCurrentCmSession?.cmChatSessionEventCallback?.callOnChatSessionSetUpFailure(ex)
                clearChatSession(ex)
                mCurrentCmSession?.state = CmSessionState.PINGING
            }
        }
    }

    private fun clearChatSession(ex: Throwable?) {
        mCurrentCmSession?.cmChatSessionEventCallback?.callOnChatSessionTermination(ex)
        mChatInfoListener?.remove()
        mChatInfoListener = null
    }

    internal fun getSessionHandler(): CmSessionHandler {
        return object :
            CmSessionHandler {

            override fun terminateSession(
                sessionAccessToken: String,
                cmSessionTerminationEventCallback: CmSessionTerminationEventCallback
            ) {
                mCurrentCmSession?.cmSessionTerminationEventCallback = cmSessionTerminationEventCallback
                    GlobalScope.launch(Dispatchers.IO) {
                        try {
                            CmSessionDataService.terminateCmSession(sessionAccessToken)
                            mCurrentCmSession?.cmSessionTerminationEventCallback?.callOnSessionTerminationSuccess()
                        }catch (ex:Throwable){
                            mCurrentCmSession?.cmSessionTerminationEventCallback
                                    ?.callOnSessionTerminationFailure(ex)
                        }
                    }
            }
        }
    }

    private suspend fun readCurrentSessionInfo() = FireStoreUtils
        .readDocument(mCurrentCmSession?.currentFbCmSessionPath, FbCmSessionInfo::class.java)

    internal fun doOnSessionSetUpFailure(ex:Throwable?){
        mCurrentCmSession?.cmSessionEventCallback?.callOnSessionSetUpFailure(ex)
        clearSession(ex)
    }

    private fun stopPing(){
        mCurrentCmSession?.pingJob?.cancel()
        mCurrentCmSession?.pingJob=null
    }

    private fun initPing(){
        mCurrentCmSession?.pingJob?.let {
            if (it.isActive){
                it.cancel()
            }
        }
        mCurrentCmSession?.state = CmSessionState.PINGING
        mCurrentCmSession?.pingJob = GlobalScope.launch(Dispatchers.IO) {
            do {
                if (mCurrentCmSession?.pingEnabled() ?: false) {
                    try {
                        pingTask()
                    } catch (ex: FbDocumentWriteException) {
                        debugStackTrace(ex)
                    }
                }
                if (mCurrentCmSession?.currentFbCmSessionInfo?.minimumPingInterval == null){
                    break
                }else {
                    delay((mCurrentCmSession?.currentFbCmSessionInfo?.minimumPingInterval ?: 1000L)/PER_CYCLE_PING_FREQUENCY)
                }
            }while (isActive)
        }
    }

    private suspend fun pingTask(): FbCmSessionInfo? {
        val fbCmSessionInfoForPing = mCurrentCmSession?.currentFbCmSessionInfo?.getInstanceForPing()
        debugLog("fbCmSessionInfoForPing: ${fbCmSessionInfoForPing}")
        return FireStoreUtils.writeDocument(mCurrentCmSession?.currentFbCmSessionPath, fbCmSessionInfoForPing)
    }

    fun refreshFrontEndConnection(activity: Activity,
                                  cmSessionEventCallback: CmSessionEventCallback,
                                  cmChatSessionRequestCallback: CmChatSessionRequestCallback,
                                  cmChatSessionEventCallback: CmChatSessionEventCallback
    ) {
        if (mCurrentCmSession !=null){
            (activity as LifecycleOwner).lifecycle.addObserver(this)
            mCurrentCmSession?.cmSessionEventCallback = cmSessionEventCallback
            mCurrentCmSession?.cmChatSessionRequestCallback = cmChatSessionRequestCallback
            mCurrentCmSession?.cmChatSessionEventCallback = cmChatSessionEventCallback
            if (mSessionInfoListener !=null){
                mSessionInfoListener?.remove()
                setSessionInfoListener()
                notifySessionSetUpSuccess()
            }
            if (mChatInfoListener!=null){
                mChatInfoListener?.remove()
                setChatInfoListner()
                notifyChatSessionSetUpSuccess()
            }
        }
    }

    private fun notifySessionSetUpSuccess() {
        mCurrentCmSession?.cmSessionEventCallback?.callOnSessionSetUpSuccess()
    }

}

