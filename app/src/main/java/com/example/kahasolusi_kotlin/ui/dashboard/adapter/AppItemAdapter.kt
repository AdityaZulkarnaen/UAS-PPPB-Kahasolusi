package com.example.kahasolusi_kotlin.ui.dashboard.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.kahasolusi_kotlin.data.model.AppItem
import com.example.kahasolusi_kotlin.databinding.ItemAppCardBinding

class AppItemAdapter(
    private val onItemClick: (AppItem) -> Unit
) : ListAdapter<AppItem, AppItemAdapter.AppItemViewHolder>(AppItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppItemViewHolder {
        val binding = ItemAppCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AppItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AppItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AppItemViewHolder(
        private val binding: ItemAppCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }

        fun bind(item: AppItem) {
            binding.apply {
                tvAppTitle.text = item.title
                tvAppSubtitle.text = item.subtitle
                tvCategory.text = item.category
                // In a real app, you would use an image loading library like Glide or Coil
                // Glide.with(ivAppImage.context).load(item.imageUrl).into(ivAppImage)
            }
        }
    }

    private class AppItemDiffCallback : DiffUtil.ItemCallback<AppItem>() {
        override fun areItemsTheSame(oldItem: AppItem, newItem: AppItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: AppItem, newItem: AppItem): Boolean {
            return oldItem == newItem
        }
    }
}
