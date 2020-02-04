package com.dasbikash.cs_android_sdk.example_app.view_controllers

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dasbikash.cs_android_sdk.R
import com.dasbikash.cs_android_sdk.android_sdk.data_service.api.ChatServerBaseApi
import com.dasbikash.cs_android_sdk.example_app.utils.jumpToSelectedActivity
import kotlinx.android.synthetic.main.activity_user_selection.*

internal class UserSelectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_selection)

        select_user_button.setOnClickListener {
            user_type_selector.selectedItem.toString().apply {
                val types: Array<String> = resources.getStringArray(R.array.user_type)
                when{
                    this == types[0] -> { loadCmActivity() }
                    this == types[1] -> { loadUserActivity() }
                    else -> { finish() }
                }
            }
        }

        initApp()
    }

    private fun initApp() {
        ChatServerBaseApi.init(this)
    }

    private fun loadUserActivity() {
        finish()
        jumpToSelectedActivity(ActivityUser::class.java)
    }

    private fun loadCmActivity() {
        finish()
        jumpToSelectedActivity(ActivityCm::class.java)
    }
}