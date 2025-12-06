package com.example.kahasolusi_kotlin.ui.home.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.kahasolusi_kotlin.data.model.Technology
import com.example.kahasolusi_kotlin.databinding.ItemTechnologyHorizontalBinding

class TechnologyHorizontalAdapter(
    private val onItemClick: (Technology) -> Unit
) : ListAdapter<Technology, TechnologyHorizontalAdapter.ViewHolder>(TechnologyDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTechnologyHorizontalBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemTechnologyHorizontalBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }

        fun bind(technology: Technology) {
            binding.apply {
                tvTechName.text = technology.nama
                
                // Load icon from URI if available
                if (technology.iconUri.isNotEmpty()) {
                    try {
                        ivTechIcon.setImageURI(Uri.parse(technology.iconUri))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
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
