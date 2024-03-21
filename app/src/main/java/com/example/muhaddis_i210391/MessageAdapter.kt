package com.example.muhaddis_i210391

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView




class MessageAdapter(
    private val messageList: List<MessageItem>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(messageItem: MessageItem)
    }

    class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.ivMessageProfile)
        val nameTextView: TextView = view.findViewById(R.id.tvMessageName)
        val messagePreviewTextView: TextView = view.findViewById(R.id.tvMessagePreview)
        val newMessageCountTextView: TextView = view.findViewById(R.id.tvNewMessageCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_messages, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val item = messageList[position]
        holder.itemView.setOnClickListener { listener.onItemClick(item) }
        with(holder) {
            imageView.setImageResource(item.imageResId)
            nameTextView.text = item.name
            messagePreviewTextView.text = item.messagePreview
            if (item.newMessageCount > 0) {
                newMessageCountTextView.visibility = View.VISIBLE
                newMessageCountTextView.text = item.newMessageCount.toString()
            } else {
                newMessageCountTextView.visibility = View.GONE
            }

            itemView.setOnClickListener {
                listener.onItemClick(item)
            }
        }
    }

    override fun getItemCount() = messageList.size
}
