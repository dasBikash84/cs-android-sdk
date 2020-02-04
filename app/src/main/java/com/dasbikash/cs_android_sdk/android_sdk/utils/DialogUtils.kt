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
import android.content.DialogInterface
import android.view.View
import androidx.appcompat.app.AlertDialog

internal object DialogUtils {

    const val DEFAULT_POS_BUTTON_TEXT = "Ok"
    const val DEFAULT_NEG_BUTTON_TEXT = "Cancel"
    const val DEFAULT_NEUTRAL_BUTTON_TEXT = ""

    fun showAlertDialog(context: Context,alertDialogDetails: AlertDialogDetails):AlertDialog{
        val dialog =  createAlertDialog(context,alertDialogDetails)
        dialog.show()
        return dialog
    }

    fun createAlertDialog(context: Context,alertDialogDetails: AlertDialogDetails):AlertDialog{
        val dialog =  getAlertDialogBuilder(context,alertDialogDetails).create()
        dialog.getButton(DialogInterface.BUTTON_POSITIVE)?.isAllCaps = false
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE)?.isAllCaps = false
        dialog.getButton(DialogInterface.BUTTON_NEUTRAL)?.isAllCaps = false
        return dialog
    }

    private fun getAlertDialogBuilder(context: Context, alertDialogDetails: AlertDialogDetails):AlertDialog.Builder{
        val dialogBuilder = AlertDialog.Builder(context)

        if (alertDialogDetails.title.isNotBlank()){
            dialogBuilder.setTitle(alertDialogDetails.title.trim())
        }
        if (alertDialogDetails.message.isNotBlank()){
            dialogBuilder.setMessage(alertDialogDetails.message.trim())
        }
        dialogBuilder.setPositiveButton(alertDialogDetails.positiveButtonText.trim(),
                {dialog: DialogInterface?, which: Int -> alertDialogDetails.doOnPositivePress() })

        dialogBuilder.setNegativeButton(alertDialogDetails.negetiveButtonText.trim(),
                {dialog: DialogInterface?, which: Int -> alertDialogDetails.doOnNegetivePress() })

        dialogBuilder.setNeutralButton(alertDialogDetails.neutralButtonText.trim(),
                {dialog: DialogInterface?, which: Int -> alertDialogDetails.doOnNeutralPress() })

        dialogBuilder.setCancelable(alertDialogDetails.isCancelable)
        alertDialogDetails.view?.let {
            dialogBuilder.setView(it)
        }
        return dialogBuilder
    }


    data class AlertDialogDetails(
        val title:String = "",
        val message:String = "",
        val positiveButtonText:String = DEFAULT_POS_BUTTON_TEXT,
        val negetiveButtonText:String = DEFAULT_NEG_BUTTON_TEXT,
        val neutralButtonText:String = DEFAULT_NEUTRAL_BUTTON_TEXT,
        val isCancelable:Boolean = true,
        val doOnPositivePress:()->Unit = {},
        val doOnNegetivePress:()->Unit = {},
        val doOnNeutralPress:()->Unit = {},
        val view:View? = null
    )

}