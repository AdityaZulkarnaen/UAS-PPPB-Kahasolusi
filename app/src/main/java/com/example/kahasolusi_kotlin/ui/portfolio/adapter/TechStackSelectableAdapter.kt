package com.example.kahasolusi_kotlin.ui.portfolio.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.kahasolusi_kotlin.data.model.Technology
import com.example.kahasolusi_kotlin.databinding.ItemTechStackSelectableBinding

class TechStackSelectableAdapter(
    private val onSelectionChanged: (List<Technology>) -> Unit
) : ListAdapter<Technology, TechStackSelectableAdapter.ViewHolder>(TechnologyDiffCallback()) {

    private val selectedTechnologies = mutableSetOf<String>()

    fun setSelectedTechnologies(techIds: List<String>) {
        selectedTechnologies.clear()
        selectedTechnologies.addAll(techIds)
        notifyDataSetChanged()
    }

    fun getSelectedTechnologies(): List<Technology> {
        return currentList.filter { selectedTechnologies.contains(it.id) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTechStackSelectableBinding.inflate(
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
        private val binding: ItemTechStackSelectableBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(technology: Technology) {
            binding.apply {
                cbTechStack.text = technology.nama
                cbTechStack.isChecked = selectedTechnologies.contains(technology.id)

                // Load icon
                if (technology.iconUri.isNotEmpty()) {
                    try {
                        ivTechIcon.setImageURI(Uri.parse(technology.iconUri))
                    } catch (e: Exception) {
                        // Use default icon if loading fails
                        ivTechIcon.setImageResource(android.R.drawable.ic_menu_info_details)
                    }
                } else {
                    ivTechIcon.setImageResource(android.R.drawable.ic_menu_info_details)
                }

                // Handle checkbox click
                root.setOnClickListener {
                    cbTechStack.isChecked = !cbTechStack.isChecked
                    handleSelectionChange(technology)
                }

                cbTechStack.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        selectedTechnologies.add(technology.id)
                    } else {
                        selectedTechnologies.remove(technology.id)
                    }
                    onSelectionChanged(getSelectedTechnologies())
                }
            }
        }

        private fun handleSelectionChange(technology: Technology) {
            if (selectedTechnologies.contains(technology.id)) {
                selectedTechnologies.remove(technology.id)
            } else {
                selectedTechnologies.add(technology.id)
            }
            onSelectionChanged(getSelectedTechnologies())
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
