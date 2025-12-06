package com.example.kahasolusi_kotlin.ui.dashboard.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.kahasolusi_kotlin.data.model.Portfolio
import com.example.kahasolusi_kotlin.databinding.ItemAppCardBinding

class PortfolioItemAdapter(
    private val onItemClick: (Portfolio) -> Unit
) : ListAdapter<Portfolio, PortfolioItemAdapter.PortfolioItemViewHolder>(PortfolioDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PortfolioItemViewHolder {
        val binding = ItemAppCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PortfolioItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PortfolioItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PortfolioItemViewHolder(
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

        fun bind(portfolio: Portfolio) {
            binding.apply {
                tvAppTitle.text = portfolio.judul
                tvAppSubtitle.text = portfolio.lokasi
                tvCategory.text = portfolio.kategori
                
                // Load image from URI if available
                if (portfolio.gambarUri.isNotEmpty()) {
                    try {
                        ivAppImage.setImageURI(Uri.parse(portfolio.gambarUri))
                    } catch (e: Exception) {
                        // If URI parsing fails, keep default image
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private class PortfolioDiffCallback : DiffUtil.ItemCallback<Portfolio>() {
        override fun areItemsTheSame(oldItem: Portfolio, newItem: Portfolio): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Portfolio, newItem: Portfolio): Boolean {
            return oldItem == newItem
        }
    }
}
