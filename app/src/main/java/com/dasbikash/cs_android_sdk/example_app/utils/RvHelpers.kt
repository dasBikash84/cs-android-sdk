package com.dasbikash.cs_android_sdk.example_app.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dasbikash.cs_android_sdk.R
import com.dasbikash.cs_android_sdk.android_sdk.CsChatDataService
import com.dasbikash.cs_android_sdk.android_sdk.data_service.ChatDataService
import com.dasbikash.cs_android_sdk.android_sdk.model.public_models.ChatEntry
import com.dasbikash.cs_android_sdk.android_sdk.model.public_models.ChatSessionInfo
import com.dasbikash.cs_android_sdk.android_sdk.utils.DisplayUtils
import com.dasbikash.cs_android_sdk.android_sdk.utils.debugLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


internal object ChatEntryDiff: DiffUtil.ItemCallback<ChatEntry>(){
    override fun areItemsTheSame(oldItem: ChatEntry, newItem: ChatEntry) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: ChatEntry, newItem: ChatEntry) = false//(oldItem.id==newItem.id) && (oldItem.posted == newItem.posted)
}

internal class ChatEntryListAdapter(val isCm:Boolean=false,val isUser:Boolean=false):
    ListAdapter<ChatEntry, ChatEntryViewHolder>(
        ChatEntryDiff
    ){

    init {
        if (! isCm.xor(isUser)){
            throw IllegalArgumentException()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatEntryViewHolder {
        val layoutId = when(isCm){
            true -> R.layout.view_cm_chat_entry
            false -> R.layout.view_user_chat_entry
        }
        return ChatEntryViewHolder(
            LayoutInflater.from(parent.context).inflate(
                layoutId,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ChatEntryViewHolder, position: Int) {
        val chatEntry = getItem(position)
        holder.bind(chatEntry)
    }
}

internal class ChatEntryViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
    private val timeText: TextView = itemView.findViewById(R.id.time)
    private val userEntryText: TextView = itemView.findViewById(R.id.user_entry)
    private val cmEntryText: TextView = itemView.findViewById(R.id.cm_entry)
    private val chatEntryPostErrorIcon: ImageView = itemView.findViewById(R.id.entry_post_error_icon)

    fun bind(chatEntry: ChatEntry) {
        when{
            chatEntry.fromUser==true -> {
                cmEntryText.hide()
                userEntryText.text  = chatEntry.payLoad
            }
            else -> {
                userEntryText.hide()
                cmEntryText.text = chatEntry.payLoad
            }
        }
        timeText.text = chatEntry.time!!.toString()
        if (chatEntry.posted){
            chatEntryPostErrorIcon.hide()
        }else{
            chatEntryPostErrorIcon.show()
        }
    }
}


internal object ChatSessionInfoDiff: DiffUtil.ItemCallback<ChatSessionInfo>(){
    override fun areItemsTheSame(oldItem: ChatSessionInfo, newItem: ChatSessionInfo) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: ChatSessionInfo, newItem: ChatSessionInfo) = oldItem==newItem
}

internal class ChatSessionInfoListAdapter(private val accessToken:String, private val isCm:Boolean=false)
    :ListAdapter<ChatSessionInfo, ChatSessionInfoViewHolder>
    (ChatSessionInfoDiff){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatSessionInfoViewHolder {
        return ChatSessionInfoViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.view_chat_session_info,
                parent,
                false
            ),accessToken,isCm
        )
    }

    override fun onBindViewHolder(holder: ChatSessionInfoViewHolder, position: Int) {
        val chatSessionInfo = getItem(position)
        holder.bind(chatSessionInfo)
    }
}

internal class ChatSessionInfoViewHolder(itemView: View, private val accessToken:String, private val isCm:Boolean=false)
    : RecyclerView.ViewHolder(itemView) {
    private val chatSessionInfoHolder: ViewGroup =
        itemView.findViewById(R.id.chat_session_info_holder)
    private val chatSessionIdText: TextView = itemView.findViewById(R.id.chat_session_id_text)
    private val chatDateText: TextView = itemView.findViewById(R.id.chat_date_text)
    private val chatSessionDetailsHolder: RecyclerView =
        itemView.findViewById(R.id.chat_session_details_holder)
    private val chatSessionDetailsAdapter = ChatEntryListAdapter(isCm = isCm, isUser = !isCm)
    private var dataCleared: Boolean = true
    private lateinit var chatSessionInfo: ChatSessionInfo

    init {
        chatSessionDetailsHolder.adapter = chatSessionDetailsAdapter
        chatSessionInfoHolder.setOnClickListener {
            if (!chatSessionDetailsHolder.isVisible && dataCleared) {
                dataCleared = false
                GlobalScope.launch(Dispatchers.IO) {
                    CsChatDataService.getChatSessionData(accessToken, chatSessionInfo).apply {
                        runOnMainThread {
                            debugLog(this.toString())
                            if (chatEntries != null && chatEntries!!.isNotEmpty()) {
                                chatSessionDetailsAdapter.submitList(chatEntries?.sortedBy { it.time }
                                    ?: emptyList())
                                chatSessionDetailsHolder.show()
                            } else {
                                DisplayUtils.showShortToast(
                                    itemView.context,
                                    itemView.context.getString(R.string.no_message_found)
                                )
                            }
                        }
                    }
                }
            } else {
                chatSessionDetailsHolder.toggle()
            }
        }
    }

    fun bind(chatSessionInfo: ChatSessionInfo) {
        this.chatSessionInfo = chatSessionInfo
        chatSessionDetailsHolder.hide()
        chatSessionDetailsAdapter.submitList(emptyList())
        dataCleared = true
        chatSessionIdText.text = chatSessionInfo.id!!
        chatDateText.text = chatSessionInfo.created!!.toString()
    }
}
