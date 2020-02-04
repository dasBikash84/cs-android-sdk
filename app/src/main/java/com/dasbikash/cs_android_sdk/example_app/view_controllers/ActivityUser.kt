package com.dasbikash.cs_android_sdk.example_app.view_controllers

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.dasbikash.cs_android_sdk.R
import com.dasbikash.cs_android_sdk.android_sdk.UserSessionService
import com.dasbikash.cs_android_sdk.android_sdk.interfaces.user.UserChatSessionHandler
import com.dasbikash.cs_android_sdk.android_sdk.interfaces.user.UserChatSessionTerminationEventCallback
import com.dasbikash.cs_android_sdk.android_sdk.interfaces.user.UserSessionEventCallback
import com.dasbikash.cs_android_sdk.android_sdk.model.public_models.ChatEntry
import com.dasbikash.cs_android_sdk.android_sdk.utils.DialogUtils
import com.dasbikash.cs_android_sdk.android_sdk.utils.DisplayUtils.showShortToast
import com.dasbikash.cs_android_sdk.android_sdk.utils.debugLog
import com.dasbikash.cs_android_sdk.android_sdk.utils.debugStackTrace
import com.dasbikash.cs_android_sdk.example_app.client_api_service.AuthDataService
import com.dasbikash.cs_android_sdk.example_app.utils.ChatEntryListAdapter
import com.dasbikash.cs_android_sdk.example_app.utils.hide
import com.dasbikash.cs_android_sdk.example_app.utils.isAdded
import com.dasbikash.cs_android_sdk.example_app.utils.show
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal class ActivityUser : AppCompatActivity() {

    private lateinit var mStartChatSessionButton: Button
    private lateinit var mQuitChatSessionButton: Button
    private lateinit var mChatSubmitButton: Button
    private lateinit var mCmNameTextView: TextView
    private lateinit var mChatSessionDataHolder: NestedScrollView
    private lateinit var mChatEntryHolder: RecyclerView
    private lateinit var mChatInputHolder: ViewGroup
    private lateinit var mChatInputEditText: EditText
    private lateinit var mWaitScreen: ViewGroup
    private lateinit var mChatSetUpWindow: ViewGroup
    private lateinit var mQuitChatDuringSetUpButton: Button

    private val mChatEntryListAdapter = ChatEntryListAdapter(isUser = true)

    private var mUserChatSessionHandler: UserChatSessionHandler?=null

    internal fun getUserChatSessionHandler() = mUserChatSessionHandler
    internal fun setUserChatSessionHandler(userChatSessionHandler: UserChatSessionHandler?){
        mUserChatSessionHandler = userChatSessionHandler
    }

    private fun chatEntrySubmitAction() {
        if (getUserChatSessionHandler()!=null && mChatInputEditText.text.isNotBlank()){
            getUserChatSessionHandler()!!.postChatEntry(mChatInputEditText.text.trim().toString())
        }
        mChatInputEditText.setText("")
    }

    private fun cancelChatAction() {
        getUserChatSessionHandler()?.apply {
            DialogUtils.showAlertDialog(this@ActivityUser, DialogUtils.AlertDialogDetails(
                title = getString(R.string.chat_session_end_prompt),
                doOnPositivePress = {
                    cancelChat(this)
                }
            ))
        }
    }

    private fun cancelChat(userChatSessionHandler: UserChatSessionHandler) {
        lifecycleScope.launch {
            try {
                showWaitScreen()
                userChatSessionHandler.terminateChatSession(getSessionToken(),getSessionTerminationEventCallback())
            } catch (ex: Throwable) {
                debugStackTrace(ex)
                doOnSessionTerminationFailure(ex)
            }
        }
    }

    private fun showWaitScreen(){
        mWaitScreen.bringToFront()
        mWaitScreen.show()
    }

    private fun hideWaitScreen() = mWaitScreen.hide()

    private fun doOnSessionTerminationFailure(ex: Throwable?) {
        if (isAdded()) {
            hideWaitScreen()
            ex?.let { debugStackTrace(it) }
            showShortToast(this,getString(R.string.error_message))
        }
    }

    private fun getSessionTerminationEventCallback() =
        object : UserChatSessionTerminationEventCallback() {
            override fun onSessionTerminationFailure(ex: Throwable?) {
                if (isAdded()) {
                    doOnSessionTerminationFailure(ex)
                }
            }
        }

    private fun chatSetUpLaunchAction() {
        if (getUserChatSessionHandler() == null) {
            DialogUtils.showAlertDialog(this, DialogUtils.AlertDialogDetails(
                title = getString(R.string.chat_session_start_prompt),
                doOnPositivePress = {
                    chatSetUpAction()
                }
            ))
        }
    }

    private fun chatSetUpAction() {
        showWaitScreen()
        lifecycleScope.launch {
            try {
                setUserChatSessionHandler(UserSessionService.startCmSession(
                        this@ActivityUser,getUserId(),
                        getSessionToken(), getSessionEventCallback()
                    ))
            } catch (ex: Throwable) {
                debugStackTrace(ex)
                doOnChatSessionSetUpFailure()
            }
        }
    }

    private fun doOnChatSessionSetUpFailure() {
        setUserChatSessionHandler(null)
        hideChatDisplay()
    }

    private fun getSessionEventCallback() = object : UserSessionEventCallback(){
        override fun onChatSessionSetUpFailure(ex: Throwable?) {
            if (isAdded()) {
                doOnChatSessionSetUpFailure()
            }
        }

        override fun onChatSessionConnectionSetup(chatSessionId: String) {
            if (isAdded()) {
                doOnChatSessionConnectionSetup(chatSessionId)
            }
        }

        override fun onChatSessionSetUpSuccess(userId: String, chatSessionId: String) {
            if (isAdded()) {
                showChatDisplay(userId)
            }
        }

        override fun onChatEntryReceive(chatEntryList: List<ChatEntry>) {
            if (isAdded()) {
                doOnChatEntryReceive(chatEntryList)
            }
        }

        override fun onChatEntryPostFailure(lastChatEntryId:String?,ex: Throwable?) {
            if (isAdded()) {
                processChatEntryPostFailure(lastChatEntryId,ex)
            }
        }

        override fun onChatSessionTermination(ex: Throwable?) {
            if (isAdded()) {
                setUserChatSessionHandler(null)
                hideChatDisplay()
                showShortToast(this@ActivityUser,getString(R.string.chat_session_terminated_message))
            }
        }

        override fun onChatEntryPostSuccess(payload: String) {
            if (isAdded()) {
                if (mChatInputEditText.text.trim().toString().equals(payload)) {
                    mChatInputEditText.setText("")
                }
            }
        }
    }

    private fun doOnChatSessionConnectionSetup(chatSessionId: String) {
        showWaitingForCmScreen()
    }

    private fun processChatEntryPostFailure(lastChatEntryId:String?,ex: Throwable?) {
        ex?.let { debugStackTrace(ex) }
        showShortToast(this,getString(R.string.chat_entry_post_failure_message))
        lastChatEntryId?.apply {
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

    private fun doOnChatEntryReceive(chatEntryList: List<ChatEntry>) {
        mChatEntryListAdapter.submitList(
            chatEntryList.sortedBy { it.time }
        )
        lifecycleScope.launch {
            delay(100)
            mChatSessionDataHolder.smoothScrollTo(
                mChatEntryHolder.width,
                mChatEntryHolder.height
            )
        }
    }

    private fun hideChatDisplay() {
        hideWaitScreen()
        mStartChatSessionButton.show()
        mQuitChatSessionButton.hide()
        mCmNameTextView.hide()
        mChatSessionDataHolder.hide()
        mChatInputHolder.hide()
        mChatEntryListAdapter.submitList(emptyList())
        mChatSetUpWindow.hide()
    }

    private fun showChatDisplay(userId: String) {
        hideWaitScreen()
        setUserName(userId)
        mStartChatSessionButton.hide()
        mChatSetUpWindow.hide()
        mQuitChatSessionButton.show()
        mCmNameTextView.show()
        mChatSessionDataHolder.show()
        mChatInputHolder.show()
    }

    private fun setUserName(userId: String){
        lifecycleScope.launch {
            try {
                val user = AuthDataService.findUser(getUserId(), getUserPass(), userId)
                mCmNameTextView.text = getString(R.string.cm_name_text,"${user.firstName} ${user.lastName}")
            }catch (ex:Throwable){
                debugStackTrace(ex)
                mCmNameTextView.text = getString(R.string.cm_name_text,userId)
            }
        }
    }

    private fun findViewComponents() {
        mStartChatSessionButton = findViewById(R.id.start_chat_session)
        mQuitChatSessionButton = findViewById(R.id.quit_chat_session)
        mCmNameTextView = findViewById(R.id.cm_name_text)
        mChatSessionDataHolder = findViewById(R.id.chat_session_data_holder)
        mChatEntryHolder = findViewById(R.id.chat_entry_holder)
        mChatInputHolder = findViewById(R.id.chat_input_holder)
        mChatInputEditText = findViewById(R.id.chat_input_edit_text)
        mChatSubmitButton = findViewById(R.id.submit_chat_entry_button)
        mWaitScreen = findViewById(R.id.wait_window)
        mChatSetUpWindow = findViewById(R.id.chat_set_up_window)
        mQuitChatDuringSetUpButton = findViewById(R.id.quit_chat_session_during_set_up)
    }

    private fun setListenersForViewComponents() {
        mStartChatSessionButton.setOnClickListener {
            chatSetUpLaunchAction()
        }
        mQuitChatSessionButton.setOnClickListener {
            cancelChatAction()
        }
        mQuitChatDuringSetUpButton.setOnClickListener {
            cancelChatAction()
        }
        mChatSubmitButton.setOnClickListener {
            chatEntrySubmitAction()
        }

        mWaitScreen.setOnClickListener {  }
    }

    private fun initView() {
        mChatEntryHolder.adapter = mChatEntryListAdapter
        hideChatDisplay()
    }

    override fun onResume() {
        super.onResume()
        getUserChatSessionHandler()?.let {
            chatSetUpAction()
        }
    }

    private fun showWaitingForCmScreen() {
        mStartChatSessionButton.hide()
        hideWaitScreen()
        mChatSetUpWindow.show()
        mChatSetUpWindow.bringToFront()
    }

    private suspend fun getSessionToken() = AuthDataService.getSessionToken(getUserId(), getUserPass())

    private fun getUserId() = getString(R.string.default_user_id)
    private fun getUserPass() = getString(R.string.default_user_pass)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)
        findViewComponents()
        setListenersForViewComponents()
        initView()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.chat_activity_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_item_chat_history -> {
                launchChatHistoryView()
                return true
            }
            else -> {
                return false
            }
        }
    }

    private fun launchChatHistoryView() {
        DialogUtils.showAlertDialog(this, DialogUtils.AlertDialogDetails(
            message = getString(R.string.launch_chat_history_prompt),
            doOnPositivePress = {
                startActivity(ActivityChatHistory.getIntent(this))
            }
        ))
    }
}