package com.dasbikash.cs_android_sdk.android_sdk.model.public_models

import androidx.annotation.Keep
import java.util.*

@Keep
data class ChatSessionInfo(
    var id:String?=null,
    var userId:String?=null,
    var customerMangerId:String?=null,
    var created:Date?=null
)