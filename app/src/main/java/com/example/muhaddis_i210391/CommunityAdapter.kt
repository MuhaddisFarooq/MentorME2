package com.example.muhaddis_i210391

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

data class CommunityItem(
    val id: Int,
    val name: String,
    val profileImage: Int
)

class CommunityAdapter(
    private val communityList: List<CommunityItem>,
    private val onItemClickListener: (CommunityItem) -> Unit
) : RecyclerView.Adapter<CommunityAdapter.CommunityViewHolder>() {

    class CommunityViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.ivCommunityMember)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommunityViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_community, parent, false)
        return CommunityViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommunityViewHolder, position: Int) {
        val item = communityList[position]
        holder.imageView.setImageResource(item.profileImage)
        holder.itemView.setOnClickListener { onItemClickListener(item) }
    }

    override fun getItemCount() = communityList.size
}
