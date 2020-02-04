/*
 * Copyright 2019 das.bikash.dev@gmail.com. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dasbikash.cs_android_sdk.android_sdk.utils

import android.util.Log
import com.dasbikash.cs_android_sdk.BuildConfig
import java.util.*

internal object LoggerUtils {
    private const val TAG = "CM>>"
    private const val MAX_TAG_LENGTH = 23

    fun debugStackTrace(ex: Throwable) {
        if (BuildConfig.DEBUG) {
            logStackTrace(ex)
        }
    }

    fun <T> debugLog(message: String, type: Class<T>) {
        if (BuildConfig.DEBUG) {
            return log(message, type)
        }
    }

    fun <T> debugLog(obj: Any, type: Class<T>) {
        return debugLog(obj.toString(),type)
    }

    fun <T> log(message: String, type: Class<T>) {
        var classNameEndIndex = type.simpleName.length
        if (classNameEndIndex > (MAX_TAG_LENGTH - TAG.length)) {
            classNameEndIndex = MAX_TAG_LENGTH - TAG.length
        }
        Log.d(TAG + type.simpleName.substring(0, classNameEndIndex), message)
    }

    private fun getStackTraceAsString(ex:Throwable):String{
        val stackTracebuilder = StringBuilder("")
        Arrays.asList(ex.stackTrace).asSequence().forEach {
            it.iterator().forEach {
                stackTracebuilder.append(it.toString()).append("\n")
            }
        }
        return stackTracebuilder.toString()
    }

    fun logStackTrace(ex: Throwable) {
        ex.printStackTrace()
        log(
            "Error StackTrace: \n" + getStackTraceAsString(
                ex
            ), this::class.java
        )
    }


}

internal fun Any.debugLog(message: String){
    LoggerUtils.debugLog(message,this::class.java)
}

internal fun Any.debugLog(model: Any?){
    LoggerUtils.debugLog(model.toString(),this::class.java)
}

internal fun Any.debugStackTrace(ex: Throwable){
    LoggerUtils.debugStackTrace(ex)
}