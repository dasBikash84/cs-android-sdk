package com.dasbikash.cs_android_sdk.example_app.view_controllers

import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.dasbikash.cs_android_sdk.R
import com.dasbikash.cs_android_sdk.android_sdk.CmSessionService
import com.dasbikash.cs_android_sdk.android_sdk.interfaces.cm.*
import com.dasbikash.cs_android_sdk.android_sdk.interfaces.cm.handlers.CmChatSessionHandler
import com.dasbikash.cs_android_sdk.android_sdk.interfaces.cm.handlers.CmChatSessionRequestHandler
import com.dasbikash.cs_android_sdk.android_sdk.interfaces.cm.handlers.CmSessionHandler
import com.dasbikash.cs_android_sdk.android_sdk.model.public_models.ChatEntry
import com.dasbikash.cs_android_sdk.android_sdk.utils.DialogUtils
import com.dasbikash.cs_android_sdk.android_sdk.utils.DisplayUtils
import com.dasbikash.cs_android_sdk.android_sdk.utils.DisplayUtils.showShortToast
import com.dasbikash.cs_android_sdk.android_sdk.utils.debugLog
import com.dasbikash.cs_android_sdk.android_sdk.utils.debugStackTrace
import com.dasbikash.cs_android_sdk.example_app.client_api_service.AuthDataService
import com.dasbikash.cs_android_sdk.example_app.utils.ChatEntryListAdapter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal class ActivityCm : AppCompatActivity() {

    private lateinit var mCmSessionOnLineSwitch: SwitchCompat
    private lateinit var mCmSessionOffLineSwitch: SwitchCompat
    private lateinit var mChatSessionDataHolder: NestedScrollView
    private lateinit var mChatParamHolder: ViewGroup
    private lateinit var mQuitChatButton: Button
    private lateinit var mChatEntryHolder: RecyclerView
    private lateinit var mWaitScreen: ViewGroup
    private lateinit var mUserIdText: TextView
    private lateinit var mChatInputHolder: ViewGroup
    private lateinit var mChatInputEditText: EditText
    private lateinit var mSubmitChatButton: Button
    private lateinit var mStandbyMessageWindow: ViewGroup

    private lateinit var mNewChatRequestDialog: ViewGroup
    private lateinit var mAcceptChatButton: Button
    private lateinit var mDeclineChatButton: Button

    private val mChatEntryListAdapter = ChatEntryListAdapter(isCm = true)

    private var mCmSessionHandler: CmSessionHandler?=null
    private var mCmChatSessionHandler: CmChatSessionHandler?=null
    private var mCmChatSessionRequestHandler: CmChatSessionRequestHandler?=null

    private fun hideChatBlock(){
        mChatParamHolder.hide()
        mChatInputHolder.hide()
        mChatSessionDataHolder.hide()
    }

    private fun showChatBlock(){
        mChatParamHolder.show()
        mChatSessionDataHolder.show()
        mChatInputHolder.show()
    }

    private fun initChatBlock(userId: String, cmChatSessionHandler: CmChatSessionHandler){
        //set user name
        setUserName(userId)
        mCmChatSessionHandler = cmChatSessionHandler
        //clear all previous display data
        mChatInputEditText.setText("")
        mChatEntryListAdapter.submitList(emptyList())
        //show data
        showChatBlock()
    }

    private fun setUserName(userId: String){
        lifecycleScope.launch {
            try {
                val user = AuthDataService.findUser(getCmId(), getCmPass(), userId)
                mUserIdText.text = getString(R.string.user_name_text,"${user.firstName} ${user.lastName}")
            }catch (ex:Throwable){
                debugStackTrace(ex)
                mUserIdText.text = getString(R.string.user_name_text,userId)
            }
        }
    }

    private fun submitChatEntryAction() {
        mCmChatSessionHandler?.let {
            mChatInputEditText.text.apply {
                if (isNotBlank()){
                    it.postChatEntry(this.toString().trim())
                }
            }
        }
        mChatInputEditText.setText("")
    }

    private fun startSession(){
        lifecycleScope.launch{
            try {
                showWaitScreen()
                mCmSessionHandler =
                    CmSessionService.startCmSession(
                        this@ActivityCm,getCmId(),getSessionToken(),
                        getSessionEventCallback(),
                        getCmChatSessionRequestCallback(),
                        getCmChatSessionEventCallback()
                    )
            }catch (ex:Throwable){
                debugStackTrace(ex)
                onSessionSetUpCancel()
                showShortToast(this@ActivityCm,R.string.error_message)
            }
        }
    }

    private fun terminateSession(){
        lifecycleScope.launch{
            mCmSessionHandler?.let {
                try {
                    showWaitScreen()
                    val terminateSessionToken =
                        getSessionToken()
                    it.terminateSession(terminateSessionToken,getSessionTerminationEventCallback())
                }catch (ex:Exception){
                    debugStackTrace(ex)
                    onSessionTerminationCancel()
                    showShortToast(this@ActivityCm,R.string.error_message)
                }
            }
        }
    }

    private fun quitChatSession() {
        mCmChatSessionHandler?.let {
            DialogUtils.showAlertDialog(this, DialogUtils.AlertDialogDetails(
                title = getString(R.string.quit_chat_session_prompt),
                doOnPositivePress = {
                    showWaitScreen()
                    lifecycleScope.launch {
                        try {
                            it.terminateChat(getSessionToken(),getChatSessionTerminationEventCallback())
                        }catch (ex:Throwable){
                            hideChatBlock()
                            showShortToast(this@ActivityCm,getString(R.string.error_message))
                        }
                    }
                }
            ))
        }
    }

    private fun getChatSessionTerminationEventCallback() = object : CmChatSessionTerminationEventCallback() {

        override fun onChatSessionTerminationFailure(ex: Throwable?) {
            if (isAdded()) {
                hideWaitScreen()
                showShortToast(this@ActivityCm,getString(R.string.error_message))
            }
        }
    }

    private fun getSessionEventCallback() = object : CmSessionEventCallback(){
        override fun onSessionSetUpSuccess() {
            if (isAdded()) {
                doOnCmSessionSetUp()
            }
        }

        override fun onSessionSetUpFailure(ex: Throwable?) {
            if (isAdded()) {
                onSessionSetUpCancel()
                showShortToast(this@ActivityCm,R.string.session_set_up_failure_message)
            }
        }

        override fun onSessionTermination(ex: Throwable?) {
            if (isAdded()) {
                lifecycleScope.launch {
                    debugLog("onSessionTermination")
                    onSessionTerminationAction()
                }
            }
        }
    }

    private fun getSessionTerminationEventCallback() = object : CmSessionTerminationEventCallback(){
        override fun onSessionTerminationSuccess() {
            if (isAdded()) {
                onSessionTerminationAction()
            }
        }

        override fun onSessionTerminationFailure(ex: Throwable?) {
            if (isAdded()) {
                onSessionTerminationCancel()
                showShortToast(this@ActivityCm,getString(R.string.session_termination_message))
            }
        }
    }

    private fun getCmChatSessionEventCallback() = object : CmChatSessionEventCallback(){
        override fun onChatSessionSetUpFailure(ex: Throwable?) {
            if (isAdded()) {
                ex?.let { debugStackTrace(it) }
                showShortToast(this@ActivityCm,getString(R.string.chat_session_set_up_failure_message))
                hideWaitScreen()
                mStandbyMessageWindow.show()
            }
        }

        override fun onChatSessionSetUpSuccess(
            userId: String,
            chatSessionId: String,
            cmChatSessionHandler: CmChatSessionHandler
        ) {
            if (isAdded()) {
                doOnChatSessionSetUp(cmChatSessionHandler)
                hideWaitScreen()
            }
        }

        override fun onNewChatEntry(chatEntryList: List<ChatEntry>) {
            if (isAdded()) {
                debugLog("onUserResponse")
                debugLog(chatEntryList.toString())
                doOnNewChatEntry(chatEntryList)
            }
        }
        override fun onChatEntryPostSuccess(payload: String) {
            if (isAdded()) {
                debugLog("onChatEntryPostSuccess: $payload")
            }
        }

        override fun onChatEntryPostFailure(chatEntryId:String?,ex: Throwable?) {
            if (isAdded()) {
                debugLog("onChatEntryPostFailure")
                ex?.let { debugStackTrace(it) }
                showShortToast(this@ActivityCm,getString(R.string.chat_entry_post_failure_message))
                processChatEntryPostFailure(chatEntryId)
            }
        }

        override fun onChatSessionTermination(ex: Throwable?) {
            if (isAdded()){
                debugLog("onChatSessionTermination")
                hideWaitScreen()
                hideChatBlock()
                ex?.let { debugStackTrace(it) }
                showShortToast(this@ActivityCm,getString(R.string.chat_session_termination_message))
                if (mCmSessionOffLineSwitch.isVisible) {
                    mStandbyMessageWindow.show()
                }
            }
        }
    }

    private fun processChatEntryPostFailure(chatEntryId: String?) {
        debugLog("processChatEntryPostFailure: $chatEntryId")
        chatEntryId?.apply {
            val modifiedChatEntries = mutableListOf<ChatEntry>()
            mChatEntryListAdapter.currentList.asSequence().forEach {
                debugLog("Before: ${it.payLoad} ${it.posted}")
                if (it.id.equals(this)) {
                    it.posted = false
                }
                debugLog("After: ${it.payLoad} ${it.posted}")
                modifiedChatEntries.add(it)
            }
            mChatEntryListAdapter.submitList(modifiedChatEntries.toList())
        }
    }

    private fun doOnNewChatEntry(chatEntryList: List<ChatEntry>) {
        mChatEntryListAdapter.submitList(chatEntryList.sortedBy { it.time }.toList())
        lifecycleScope.launch {
            delay(100)
            mChatSessionDataHolder.smoothScrollTo(
                mChatEntryHolder.width,
                mChatEntryHolder.height
            )
        }
    }

    private fun onSessionSetUpCancel() {
        mCmSessionHandler = null
        mCmSessionOnLineSwitch.isChecked = false
        mCmSessionOnLineSwitch.show()
        hideWaitScreen()
    }

    private fun doOnCmSessionSetUp() {
        hideWaitScreen()
        showShortToast(this,getString(R.string.session_start_message))
        mCmSessionOnLineSwitch.hide()
        mCmSessionOffLineSwitch.isChecked = true
        mCmSessionOffLineSwitch.show()
        mStandbyMessageWindow.show()
    }

    private fun onSessionTerminationAction() {
        showShortToast(this,getString(R.string.session_terminated_message))
        mCmSessionOffLineSwitch.hide()
        mStandbyMessageWindow.hide()
        onSessionSetUpCancel()
    }

    private fun onSessionTerminationCancel() {
        mCmSessionOffLineSwitch.isChecked = true
        hideWaitScreen()
    }

    private fun doOnChatSessionSetUp(cmChatSessionHandler: CmChatSessionHandler
    ) {
        initChatBlock(cmChatSessionHandler.getChatSessionDetails().first,cmChatSessionHandler)
        showShortToast(this,getString(R.string.chat_session_set_up_success_message))
    }

    private fun showWaitScreen(){
        mWaitScreen.show()
        mWaitScreen.bringToFront()
    }

    private fun hideWaitScreen() = mWaitScreen.hide()


    private fun findViewComponents() {
        mCmSessionOnLineSwitch = findViewById(R.id.cm_session_online_switch)
        mCmSessionOffLineSwitch = findViewById(R.id.cm_session_offline_switch)
        mChatSessionDataHolder= findViewById(R.id.chat_session_data_holder)
        mQuitChatButton= findViewById(R.id.quit_chat_session)
        mChatEntryHolder= findViewById(R.id.chat_entry_holder)
        mWaitScreen= findViewById(R.id.wait_window)
        mChatParamHolder= findViewById(R.id.chat_param_holder)
        mUserIdText= findViewById(R.id.user_name_text)
        mChatInputHolder= findViewById(R.id.chat_input_holder)
        mChatInputEditText= findViewById(R.id.chat_input_edit_text)
        mSubmitChatButton= findViewById(R.id.submit_chat_entry_button)
        mStandbyMessageWindow= findViewById(R.id.standby_message_window)

        mNewChatRequestDialog = findViewById(R.id.new_chat_request_dialog)
        mAcceptChatButton= findViewById(R.id.accept_chat)
        mDeclineChatButton= findViewById(R.id.decline_chat)
    }

    private fun setListenersForViewComponents() {
        mCmSessionOnLineSwitch.setOnCheckedChangeListener { compoundButton, state ->
            if (state){
                DialogUtils.showAlertDialog(this, DialogUtils.AlertDialogDetails(
                    getString(R.string.cm_session_online_prompt)+"?",
                    doOnPositivePress = {
                        startSession()
                    },
                    doOnNegetivePress = {
                        onSessionSetUpCancel()
                    }
                ))
            }
        }
        mCmSessionOffLineSwitch.setOnCheckedChangeListener { compoundButton, state ->
            if (!state){
                DialogUtils.showAlertDialog(this, DialogUtils.AlertDialogDetails(
                    getString(R.string.cm_session_off_line_prompt)+"?",
                    doOnPositivePress = {
                        terminateSession()
                    },
                    doOnNegetivePress = {
                        onSessionTerminationCancel()
                    }
                ))
            }
        }

        mSubmitChatButton.setOnClickListener { submitChatEntryAction() }
        mQuitChatButton.setOnClickListener { quitChatSession() }

        mAcceptChatButton.setOnClickListener { chatCallAcceptAction() }
        mDeclineChatButton.setOnClickListener { chatCallDeclineAction() }
        mNewChatRequestDialog.setOnClickListener {  }
    }

    private fun initView() {

        mChatEntryHolder.adapter = mChatEntryListAdapter

        mCmSessionOnLineSwitch.isChecked = false
        mCmSessionOffLineSwitch.hide()
        mStandbyMessageWindow.hide()
        mChatSessionDataHolder.hide()
        hideChatBlock()
    }

    private suspend fun getSessionToken() = AuthDataService.getSessionToken(getCmId(), getCmPass())
    private fun getCmId() = getString(R.string.default_cm_id)
    private fun getCmPass() = getString(R.string.default_cm_pass)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cm)
        findViewComponents()
        setListenersForViewComponents()
        initView()
    }

    private fun chatCallDeclineAction() {
        DialogUtils.showAlertDialog(this, DialogUtils.AlertDialogDetails(
            title = getString(R.string.decline_chat_request_prompt),
            doOnPositivePress = {
                lifecycleScope.launch {
                    mCmChatSessionRequestHandler?.rejectCall(getSessionToken())
                    disableChatRequestDialog()
                    showWaitScreen()
                }
            }
        ))
    }

    private fun chatCallAcceptAction() {
        lifecycleScope.launch {
            mCmChatSessionRequestHandler?.acceptCall(getSessionToken(),getWelcomeResponse())
            disableChatRequestDialog()
            showWaitScreen()
        }
    }

    private fun disableChatRequestDialog() {
        mNewChatRequestDialog.hide()
    }

    private fun getWelcomeResponse():String{
        return getString(R.string.cm_welcome_message)
    }

    private fun getCmChatSessionRequestCallback() = object : CmChatSessionRequestCallback(){

        override fun onChatSessionRequest(cmChatSessionRequestHandler: CmChatSessionRequestHandler) {
            if (isAdded()) {
                debugLog("Chat session request")
                mCmChatSessionRequestHandler = cmChatSessionRequestHandler
                mNewChatRequestDialog.show()
                mNewChatRequestDialog.bringToFront()
            }
        }

        override fun onChatRequestDrop(ex: Throwable?) {
            if (isAdded()) {
                disableChatRequestDialog()
                showShortToast(this@ActivityCm, getString(R.string.cm_session_start_failure_message))
                hideWaitScreen()
            }
        }

        override fun onChatSessionSetUpSuccess() {
            if (isAdded()) {
                disableChatRequestDialog()
                showShortToast(this@ActivityCm, getString(R.string.cm_session_start_message))
                hideWaitScreen()
            }
        }
    }
}
