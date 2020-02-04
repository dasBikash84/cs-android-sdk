package com.dasbikash.cs_android_sdk.example_app.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.View
import androidx.fragment.app.Fragment
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

internal fun <T: Activity> Activity.jumpToSelectedActivity(type:Class<T>){
    val intent = Intent(this, type)
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

internal fun runOnMainThread(task: () -> Any?){
    GlobalScope.launch(Dispatchers.Main) {
        task()
    }
}

internal suspend fun <T:Any> CoroutineContext.runAsync(task:()->T):T {
    return withContext(this) {
        return@withContext async(Dispatchers.IO) { task() }.await()
    }
}