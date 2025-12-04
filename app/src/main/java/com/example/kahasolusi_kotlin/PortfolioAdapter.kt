package com.example.kahasolusi_kotlin

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.kahasolusi_kotlin.data.model.Portfolio
import com.example.kahasolusi_kotlin.databinding.ItemPortfolioBinding

class PortfolioAdapter(
    private val portfolioList: List<Portfolio>,
    private val onEditClick: (Portfolio) -> Unit,
    private val onDeleteClick: (Portfolio) -> Unit
) : RecyclerView.Adapter<PortfolioAdapter.PortfolioViewHolder>() {

    inner class PortfolioViewHolder(private val binding: ItemPortfolioBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(portfolio: Portfolio) {
            binding.tvTitle.text = portfolio.judul
            binding.tvDescription.text = portfolio.deskripsi

            // Format detail text
            val detailParts = mutableListOf<String>()
            if (portfolio.kategori.isNotEmpty()) {
                detailParts.add(portfolio.kategori)
            }
            if (portfolio.lokasi.isNotEmpty()) {
                detailParts.add(portfolio.lokasi)
            }
            if (portfolio.techStack.isNotEmpty()) {
                detailParts.add(portfolio.techStack)
            }
            binding.tvDetail.text = detailParts.joinToString(" • ")

            // Load image thumbnail
            if (portfolio.gambarUri.isNotEmpty()) {
                try {
                    val uri = Uri.parse(portfolio.gambarUri)
                    binding.ivThumbnail.setImageURI(uri)
                } catch (e: Exception) {
                    binding.ivThumbnail.setImageResource(android.R.drawable.ic_menu_gallery)
                }
            } else {
                binding.ivThumbnail.setImageResource(android.R.drawable.ic_menu_gallery)
            }

            // Edit button click
            binding.btnEdit.setOnClickListener {
                onEditClick(portfolio)
            }

            // Delete button click
            binding.btnDelete.setOnClickListener {
                onDeleteClick(portfolio)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PortfolioViewHolder {
        val binding = ItemPortfolioBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PortfolioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PortfolioViewHolder, position: Int) {
        holder.bind(portfolioList[position])
    }

    override fun getItemCount(): Int = portfolioList.size
}
