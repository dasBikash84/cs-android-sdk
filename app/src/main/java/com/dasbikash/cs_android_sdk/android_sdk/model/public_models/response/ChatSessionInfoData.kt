package com.dasbikash.cs_android_sdk.android_sdk.model.public_models.response

import androidx.annotation.Keep
import com.dasbikash.cs_android_sdk.android_sdk.model.public_models.ChatSessionInfo

@Keep
data class ChatSessionInfoData(
        var chatSessions:List<ChatSessionInfo>
)