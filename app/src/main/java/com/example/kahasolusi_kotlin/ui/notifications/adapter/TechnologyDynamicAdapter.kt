package com.example.kahasolusi_kotlin.ui.notifications.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.kahasolusi_kotlin.R
import com.example.kahasolusi_kotlin.data.model.Technology

class TechnologyDynamicAdapter(
    private val onItemClick: (Technology) -> Unit
) : ListAdapter<Technology, TechnologyDynamicAdapter.TechnologyViewHolder>(TechnologyDiffCallback()) {

    class TechnologyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val iconImageView: ImageView = itemView.findViewById(R.id.ivTechnologyIcon)
        val nameTextView: TextView = itemView.findViewById(R.id.tvTechnologyName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TechnologyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_technology_card, parent, false)
        return TechnologyViewHolder(view)
    }

    override fun onBindViewHolder(holder: TechnologyViewHolder, position: Int) {
        val technology = getItem(position)
        
        holder.nameTextView.text = technology.nama
        
        // Load icon from URI if available
        if (technology.iconUri.isNotEmpty()) {
            try {
                holder.iconImageView.setImageURI(Uri.parse(technology.iconUri))
            } catch (e: Exception) {
                // If URI parsing fails, keep default image
                e.printStackTrace()
            }
        }
        
        holder.itemView.setOnClickListener {
            onItemClick(technology)
        }
    }

    private class TechnologyDiffCallback : DiffUtil.ItemCallback<Technology>() {
        override fun areItemsTheSame(oldItem: Technology, newItem: Technology): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Technology, newItem: Technology): Boolean {
            return oldItem == newItem
        }
    }
}
