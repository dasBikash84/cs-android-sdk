package com.dasbikash.cs_android_sdk.android_sdk.model.fb

import androidx.annotation.Keep
import com.dasbikash.cs_android_sdk.android_sdk.exception.InvalidChatEntryException
import com.dasbikash.cs_android_sdk.android_sdk.exception.InvalidFbChatSessionInfoException
import com.dasbikash.cs_android_sdk.android_sdk.model.public_models.ChatEntry
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import java.util.*
@Keep
internal data class FbChatSessionInfo(
    var clientUserId: String? = null,//for access control of user
    var userId: String? = null,//for access control of user
    var clientCmId: String? = null,//for access control of CM
    var cmId: String? = null,//for access control of CM
    var readOn: Boolean = false,//For access control
    var writeOn: Boolean = false,//For access control
    var chatEntries: Map<String, FbChatEntry>? = null,
    var positionOnQueue:Int?=null,
    var createdOn:Date?=null,
    var activatedOn:Date?=null,
    var disabledOn:Date?=null
){
    @Exclude
    fun validateData(){
        if (clientCmId ==null ||
                userId==null ||
                clientCmId==null ||
                cmId==null){
            throw  InvalidFbChatSessionInfoException()
        }
    }

    @Exclude
    fun addNewEntry(payLoad: String,isCm:Boolean=false,isUser:Boolean=false): FbChatSessionInfo?{
        if (payLoad.isNotBlank() && isCm.xor(isUser)){
            val newFbChatEntry = FbChatEntry(payLoad = payLoad,time = Timestamp.now())
            newFbChatEntry.fromCm = isCm
            newFbChatEntry.fromUser = isUser
            val newChatEntries = mutableMapOf<String, FbChatEntry>()
            chatEntries?.keys?.forEach {
                newChatEntries.put(it,chatEntries!!.get(it)!!)
            }
            newChatEntries.put(UUID.randomUUID().toString(),newFbChatEntry)
            return this.copy(chatEntries = newChatEntries)
        }
        return null
    }

    @Exclude
    fun getChatEntries():List<ChatEntry>{
        chatEntries?.let {
            return FbChatEntry.getChatEntries(it)
        }
        return emptyList()
    }
}
@Keep
internal data class FbChatEntry(
        var payLoad: String? = null,
        var fromUser: Boolean = false,
        var fromCm: Boolean = false,
        var time: Timestamp?=null
){
    @Exclude
    fun validateData(){
        if (!fromCm.xor(fromCm) ||
            payLoad==null ||
            payLoad!!.isEmpty()){
            throw  InvalidChatEntryException()
        }
    }

    @Exclude
    fun getChatEntry(key:String): ChatEntry?{
        ChatEntry(
            id = key, payLoad = payLoad!!, fromUser = fromUser,
            fromCm = fromCm, time = time!!.toDate()
        ).apply {
            if (isValid()){
                return this
            }
        }
        return null
    }

    companion object {
        fun getChatEntries(fbChatEntryMap: Map<String, FbChatEntry>): List<ChatEntry> {
            val chatEntries = mutableListOf<ChatEntry>()
            fbChatEntryMap.keys.asSequence().forEach {
                chatEntries.add(fbChatEntryMap.get(it)!!.getChatEntry(it)!!)
            }
            return chatEntries.toList()
        }
    }
}