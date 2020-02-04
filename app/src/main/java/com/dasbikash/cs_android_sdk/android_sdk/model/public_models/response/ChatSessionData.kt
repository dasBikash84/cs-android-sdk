package com.dasbikash.cs_android_sdk.android_sdk.model.public_models.response

import androidx.annotation.Keep
import com.dasbikash.cs_android_sdk.android_sdk.model.public_models.ChatEntry

@Keep
data class ChatSessionData(
        var chatEntries:List<ChatEntry>?=null
)