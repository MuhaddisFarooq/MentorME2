package com.example.muhaddis_i210391

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView

class NotificationAdapter(context: Context, private val notifications: List<Notification>)
    : ArrayAdapter<Notification>(context, 0, notifications) {



    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var listItemView = convertView
        if (listItemView == null) {
            listItemView = LayoutInflater.from(context).inflate(R.layout.list_item_notification, parent, false)
        }

        val currentNotification = notifications[position]

        val notificationTextView = listItemView!!.findViewById<TextView>(R.id.tvNotificationText)
        notificationTextView.text = "${currentNotification.title}: ${currentNotification.message}"

        // You can set an onClickListener on ivCross to remove the notification from the list
        val crossImageView = listItemView.findViewById<ImageView>(R.id.ivCross)
        crossImageView.setOnClickListener {
            // Implement the removal logic here
            // For example, remove the item from the list and notify the adapter
        }

        return listItemView
    }
}
