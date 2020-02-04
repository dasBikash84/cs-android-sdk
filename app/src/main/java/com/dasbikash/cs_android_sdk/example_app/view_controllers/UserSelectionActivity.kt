package com.dasbikash.cs_android_sdk.example_app.view_controllers

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.dasbikash.cs_android_sdk.R
import com.dasbikash.cs_android_sdk.android_sdk.data_service.api.ChatServerBaseApi
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
//        jumpToSelectedActivity(ActivityCm::class.java)
    }
}

internal fun <T: Activity> Activity.jumpToSelectedActivity(type:Class<T>){
    val intent = Intent(this,type)
    startActivity(intent)
}
internal fun View.hide(){
    visibility = View.GONE
}
internal fun View.show(){
    visibility = View.VISIBLE
}
internal fun View.invisiable(){
    visibility = View.INVISIBLE
}
internal fun View.toggle(){
    if (visibility== View.GONE) {
        visibility = View.VISIBLE
    }else{
        visibility = View.GONE
    }
}

internal fun <R> Fragment.runWithContext(task:(Context)->R){
    if (isAdded) {
        context?.let { task(it) }
    }
}

internal fun <R> Fragment.runWithActivity(task:(Activity)->R){
    if (isAdded) {
        activity?.let { task(it) }
    }
}

internal fun Activity.isAdded():Boolean{
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        return !isDestroyed
    }else {
        return true
    }
}