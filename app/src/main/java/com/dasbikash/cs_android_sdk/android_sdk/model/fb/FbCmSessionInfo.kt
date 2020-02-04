package com.dasbikash.cs_android_sdk.android_sdk.model.fb

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
@Keep
internal data class FbCmSessionInfo(
        var pings:List<Timestamp>?=null,
        var currentChatSessionPath:String?=null,
        var cmId:String?=null,
        var clientCmId:String?=null,
        var active:Boolean=false,
        var minimumPingInterval:Long?=null,
        var chatSessionRequest:Boolean?=null
){
        @Exclude
        fun getInstanceForPing(): FbCmSessionInfo {
                val newPings = mutableListOf<Timestamp>()
                pings?.let { newPings.addAll(it) }
                newPings.add(Timestamp.now())
                pings = newPings.toList()
                return this
        }

        override fun toString(): String {
                return "FbCmSessionInfo(pings=${pings?.map { it.seconds }}, " +
                        "currentChatSessionPath=$currentChatSessionPath, " +
                        "cmId=$cmId, " +
                        "clientCmId=$clientCmId, " +
                        "active=$active, " +
                        "minimumPingInterval=$minimumPingInterval, " +
                        "chatSessionRequest=$chatSessionRequest)"
        }
}