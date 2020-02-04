package com.dasbikash.cs_android_sdk.example_app.view_controllers

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.dasbikash.cs_android_sdk.R
import com.dasbikash.cs_android_sdk.android_sdk.CsChatDataService
import com.dasbikash.cs_android_sdk.android_sdk.data_service.ChatDataService
import com.dasbikash.cs_android_sdk.android_sdk.utils.debugLog
import com.dasbikash.cs_android_sdk.android_sdk.utils.debugStackTrace
import com.dasbikash.cs_android_sdk.example_app.client_api_service.AuthDataService
import com.dasbikash.cs_android_sdk.example_app.utils.ChatSessionInfoListAdapter
import com.dasbikash.cs_android_sdk.example_app.utils.hide
import com.dasbikash.cs_android_sdk.example_app.utils.runAsync
import com.dasbikash.cs_android_sdk.example_app.utils.show
import kotlinx.coroutines.launch

internal class ActivityChatHistory : AppCompatActivity() {

    private lateinit var mWaitScreen: ViewGroup
    private lateinit var mChatSessionInfoHolder: RecyclerView

    private lateinit var mChatSessionInfoAdapter: ChatSessionInfoListAdapter

    private fun findViewComponents() {
        mWaitScreen = findViewById(R.id.wait_screen)
        mChatSessionInfoHolder = findViewById(R.id.chat_session_info_holder)
    }

    private fun setListenersForViewComponents() {
        mWaitScreen.setOnClickListener {  }
    }

    private fun showWaitScreen(){
        mWaitScreen.bringToFront()
        mWaitScreen.show()
    }

    private fun hideWaitScreen() = mWaitScreen.hide()

    private fun initView() {
        hideWaitScreen()
        refreshChatSessionInfoData()
    }

    private fun refreshChatSessionInfoData() {
        lifecycleScope.launch {
            showWaitScreen()
            try {
                getAccessToken().let {
                    debugLog("Access token: $it")
                    coroutineContext
                        .runAsync { CsChatDataService.getCmChatSessionInfoData(it)}
                        .let {
                            it.chatSessions.asSequence().forEach {
                                debugLog(it.toString())
                            }
                            if (!::mChatSessionInfoAdapter.isInitialized){
                                mChatSessionInfoAdapter = ChatSessionInfoListAdapter(getAccessToken(),isCm())
                                mChatSessionInfoHolder.adapter = mChatSessionInfoAdapter
                            }
                            mChatSessionInfoAdapter.submitList(it.chatSessions)
                    }
                }
            }catch (ex:Throwable){
                ex.printStackTrace()
                debugStackTrace(ex)
            }finally {
                hideWaitScreen()
            }
        }
    }

    private fun getUserName():String{
        if (isCm()){
            return getString(R.string.default_cm_id)
        }else{
            return getString(R.string.default_user_id)
        }
    }
    private fun getPassword():String{
        if (isCm()){
            return getString(R.string.default_cm_pass)
        }else{
            return getString(R.string.default_user_pass)
        }
    }

    private fun isCm():Boolean =
        intent?.hasExtra(EXTRA_CM) ?: false

    private var mAccessToken:String?=null

    private suspend fun getAccessToken():String{
        if (mAccessToken == null) {
            mAccessToken = AuthDataService.getAccessToken(getUserName(), getPassword())
        }
        return mAccessToken!!
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_history)
        findViewComponents()
        setListenersForViewComponents()
        initView()
    }

    companion object{

        private const val EXTRA_CM =
            "com.dasbikash.cs_android_sdk.example_app.view_controllers.EXTRA_CM"

        internal fun getIntent(context: Context,forCm:Boolean=false):Intent{
            val intent = Intent(context,ActivityChatHistory::class.java)
            if (forCm){
                intent.putExtra(EXTRA_CM, EXTRA_CM)
            }
            return intent
        }
    }
}
