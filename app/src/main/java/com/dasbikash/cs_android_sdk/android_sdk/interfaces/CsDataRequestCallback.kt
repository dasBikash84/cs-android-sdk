package com.dasbikash.cs_android_sdk.android_sdk.interfaces

import com.dasbikash.cs_android_sdk.android_sdk.utils.runOnMainThread
import kotlinx.coroutines.Job

abstract class CsDataRequestCallback<T:Any> {
    internal var job:Job?=null
    abstract fun onDataResponse(data:T)
    abstract fun onError(ex:Throwable?)

    fun cancelRequest(){
        job?.cancel()
    }

    internal fun callOnDataResponse(data:T){
        runOnMainThread { onDataResponse(data) }
    }

    internal fun callOnError(ex:Throwable?){
        runOnMainThread { onError(ex) }
    }
}