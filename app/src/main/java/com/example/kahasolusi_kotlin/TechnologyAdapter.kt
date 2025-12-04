package com.example.kahasolusi_kotlin

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.kahasolusi_kotlin.data.model.Technology
import com.example.kahasolusi_kotlin.databinding.ItemTechnologyBinding

class TechnologyAdapter(
    private val technologyList: List<Technology>,
    private val onEditClick: (Technology) -> Unit,
    private val onDeleteClick: (Technology) -> Unit
) : RecyclerView.Adapter<TechnologyAdapter.TechnologyViewHolder>() {

    inner class TechnologyViewHolder(private val binding: ItemTechnologyBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(technology: Technology) {
            binding.tvTechName.text = technology.nama

            // Load icon
            if (technology.iconUri.isNotEmpty()) {
                try {
                    val uri = Uri.parse(technology.iconUri)
                    binding.ivTechIcon.setImageURI(uri)
                } catch (e: Exception) {
                    binding.ivTechIcon.setImageResource(android.R.drawable.ic_menu_gallery)
                }
            } else {
                binding.ivTechIcon.setImageResource(android.R.drawable.ic_menu_gallery)
            }

            // Edit button click
            binding.btnEdit.setOnClickListener {
                onEditClick(technology)
            }

            // Delete button click
            binding.btnDelete.setOnClickListener {
                onDeleteClick(technology)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TechnologyViewHolder {
        val binding = ItemTechnologyBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TechnologyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TechnologyViewHolder, position: Int) {
        holder.bind(technologyList[position])
    }

    override fun getItemCount(): Int = technologyList.size
}
