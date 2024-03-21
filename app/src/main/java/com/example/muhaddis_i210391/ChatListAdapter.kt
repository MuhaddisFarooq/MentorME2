package com.example.muhaddis_i210391

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatListAdapter(private val chatList: List<ChatItem>, private val onChatClicked: (ChatItem) -> Unit) :
    RecyclerView.Adapter<ChatListAdapter.ChatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chatItem = chatList[position]
        holder.itemView.findViewById<TextView>(R.id.tvChatUserName).text = chatItem.userName
        holder.itemView.findViewById<TextView>(R.id.tvLastMessage).text = chatItem.lastMessage
        holder.itemView.setOnClickListener { onChatClicked(chatItem) }
    }

    override fun getItemCount(): Int = chatList.size

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
