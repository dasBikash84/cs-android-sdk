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

import android.content.Context

internal object SharedPreferenceUtils {

    private const val SP_FILE_KEY = "com.dasbikash.cs_android_sdk.android_sdk.utils.SP_FILE_KEY"

    enum class DefaultValues private constructor(val value: Any) {
        DEFAULT_STRING(""),
        DEFAULT_LONG(0L),
        DEFAULT_INT(0),
        DEFAULT_FLOAT(0F),
        DEFAULT_BOOLEAN(false)
    }

    /**
     * Supports Long,Int,Float,String and Boolean data storing
     * */
    fun <T : Any> saveData(context: Context, data: T, key: String) {

        val sharedPref = context.getSharedPreferences(
            SP_FILE_KEY, Context.MODE_PRIVATE)

        val editor = sharedPref.edit()

        when (data) {
            is Long     -> editor.putLong(key, data as Long)
            is Int      -> editor.putInt(key, data as Int)
            is Float    -> editor.putFloat(key, data as Float)
            is String   -> editor.putString(key, data as String)
            is Boolean  -> editor.putBoolean(key, data as Boolean)
            else        -> throw IllegalArgumentException()
        }
        editor.apply()
    }

    /**
     * Supports Long,Int,Float,String and Boolean data storing
     * Has to provide default data of esired type
     * */
    fun getData(context: Context, defaultValue: DefaultValues, key: String): Any {

        val sharedPref =
                context.getSharedPreferences(SP_FILE_KEY, Context.MODE_PRIVATE)

        @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        when (defaultValue.value) {
            is Long     -> return sharedPref.getLong(key, defaultValue.value)
            is Int      -> return sharedPref.getInt(key, defaultValue.value)
            is Float    -> return sharedPref.getFloat(key, defaultValue.value)
            is String   -> return sharedPref.getString(key, defaultValue.value) as Any
            is Boolean  -> return sharedPref.getBoolean(key, defaultValue.value)
            else        -> throw IllegalArgumentException()
        }
    }
}
