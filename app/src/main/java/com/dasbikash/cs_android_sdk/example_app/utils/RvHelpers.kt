package com.dasbikash.cs_android_sdk.example_app.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dasbikash.cs_android_sdk.R
import com.dasbikash.cs_android_sdk.android_sdk.model.public_models.ChatEntry


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