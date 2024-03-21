package com.example.muhaddis_i210391

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class CommunityMessageAdapter(private val messages: List<CommunityMessage>) :
    RecyclerView.Adapter<CommunityMessageAdapter.MessageViewHolder>() {

    class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val senderNameTextView: TextView = view.findViewById(R.id.senderName)
        private val messageTextView: TextView = view.findViewById(R.id.message)

        fun bind(message: CommunityMessage) {
            senderNameTextView.text = message.senderName
            messageTextView.text = message.message
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_community_messages, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount(): Int = messages.size
}
