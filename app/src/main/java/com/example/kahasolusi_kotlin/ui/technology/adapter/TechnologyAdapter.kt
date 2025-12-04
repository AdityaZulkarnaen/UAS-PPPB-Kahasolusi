package com.example.kahasolusi_kotlin.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kahasolusi_kotlin.R
import com.example.kahasolusi_kotlin.data.model.TechnologyItem

class TechnologyAdapter(private val list: List<TechnologyItem>) :
    RecyclerView.Adapter<TechnologyAdapter.TechViewHolder>() {

    class TechViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgIcon: ImageView = view.findViewById(R.id.imgTechIcon)
        val txtName: TextView = view.findViewById(R.id.txtTechName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TechViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_technology, parent, false)
        return TechViewHolder(view)
    }

    override fun onBindViewHolder(holder: TechViewHolder, position: Int) {
        val item = list[position]
        holder.imgIcon.setImageResource(item.iconResource)
        holder.txtName.text = item.name
    }

    override fun getItemCount(): Int = list.size
}
