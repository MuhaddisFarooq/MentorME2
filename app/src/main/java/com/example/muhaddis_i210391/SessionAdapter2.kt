package com.example.muhaddis_i210391

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView

class SessionAdapter2(context: Context, private val resource: Int, private val notifications: List<String>) :
    ArrayAdapter<String>(context, resource, notifications) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        val viewHolder: ViewHolder

        if (view == null) {
            view = LayoutInflater.from(context).inflate(resource, parent, false)
            viewHolder = ViewHolder()
            viewHolder.tvNotificationText = view.findViewById(R.id.tvNotificationText)
            viewHolder.ivCross = view.findViewById(R.id.ivCross)
            view.tag = viewHolder
        } else {
            viewHolder = view.tag as ViewHolder
        }

        val notification = notifications[position]

        viewHolder.tvNotificationText?.text = notification

        viewHolder.ivCross?.setOnClickListener {

            notifyDataSetChanged()
        }

        return view!!
    }

    private class ViewHolder {
        var tvNotificationText: TextView? = null
        var ivCross: ImageView? = null
    }
}
