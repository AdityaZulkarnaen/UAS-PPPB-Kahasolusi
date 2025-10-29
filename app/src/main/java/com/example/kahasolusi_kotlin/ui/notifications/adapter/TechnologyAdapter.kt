package com.example.kahasolusi_kotlin.ui.notifications.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kahasolusi_kotlin.R
import com.example.kahasolusi_kotlin.data.model.TechnologyItem

class TechnologyAdapter(
    private val technologyList: List<TechnologyItem>,
    private val onItemClick: (TechnologyItem) -> Unit = {}
) : RecyclerView.Adapter<TechnologyAdapter.TechnologyViewHolder>() {

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
        val technology = technologyList[position]
        
        holder.iconImageView.setImageResource(technology.iconResource)
        holder.nameTextView.text = technology.name
        
        holder.itemView.setOnClickListener {
            onItemClick(technology)
        }
    }

    override fun getItemCount(): Int = technologyList.size
}