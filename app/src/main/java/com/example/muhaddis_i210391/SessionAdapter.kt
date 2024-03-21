package com.example.muhaddis_i210391

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView

class SessionAdapter(context: Context, private val resource: Int, private val sessions: List<BookedSessionsActivity.Session>) :
    ArrayAdapter<BookedSessionsActivity.Session>(context, resource, sessions) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        val viewHolder: ViewHolder

        if (view == null) {
            view = LayoutInflater.from(context).inflate(resource, parent, false)
            viewHolder = ViewHolder()
            viewHolder.ivProfileImage = view.findViewById(R.id.ivProfileImage)
            viewHolder.tvName = view.findViewById(R.id.tvName)
            viewHolder.tvDesignation = view.findViewById(R.id.tvDesignation)
            viewHolder.tvDate = view.findViewById(R.id.tvDate)
            view.tag = viewHolder
        } else {
            viewHolder = view.tag as ViewHolder
        }

        val session = sessions[position]

        viewHolder.ivProfileImage?.setImageResource(session.profileImageResource)
        viewHolder.tvName?.text = session.name
        viewHolder.tvDesignation?.text = session.designation
        viewHolder.tvDate?.text = session.date

        return view!!
    }

    private class ViewHolder {
        var ivProfileImage: ImageView? = null
        var tvName: TextView? = null
        var tvDesignation: TextView? = null
        var tvDate: TextView? = null
    }
}
