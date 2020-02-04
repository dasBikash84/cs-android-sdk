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
import android.widget.Toast
import androidx.annotation.StringRes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

internal object DisplayUtils {

    fun showShortToast(context: Context, message: String) {
        runOnMainThread {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    fun showShortToast(context: Context, @StringRes messageId: Int):Toast {
        val toast = Toast.makeText(context, context.getString(messageId), Toast.LENGTH_SHORT)
        runOnMainThread {
            toast.show()
        }
        return toast
    }

    fun showLongToast(context: Context, message: String):Toast {
        val toast = Toast.makeText(context, message, Toast.LENGTH_LONG)
        runOnMainThread {
            toast.show()
        }
        return toast
    }
}

internal fun runOnMainThread(task: () -> Any?){
    GlobalScope.launch(Dispatchers.Main) {
        task()
    }
}
