package com.dasbikash.cs_android_sdk.android_sdk.model.public_models

import androidx.annotation.Keep
import java.util.*

@Keep
data class ChatEntry(
    var id:String?=null,
    var payLoad: String?=null,
    var fromUser: Boolean = false,
    var fromCm: Boolean = false,
    var time: Date?=null,
    var posted:Boolean=true
){
    fun isValid() = fromCm.xor(fromUser)
}