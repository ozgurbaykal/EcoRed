package com.ozgurbaykal.ecored.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ozgurbaykal.ecored.databinding.ItemWalkthroughBinding
import com.ozgurbaykal.ecored.model.WalkthroughItem

class WalkthroughAdapter(private val items: List<WalkthroughItem>) : RecyclerView.Adapter<WalkthroughAdapter.WalkthroughViewHolder>() {

    inner class WalkthroughViewHolder(val binding: ItemWalkthroughBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WalkthroughViewHolder {
        val binding = ItemWalkthroughBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WalkthroughViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WalkthroughViewHolder, position: Int) {
        val item = items[position]
        holder.binding.imageView.setImageResource(item.imageRes)
        holder.binding.titleTextView.text = item.title
        holder.binding.descriptionTextView.text = item.description
    }

    override fun getItemCount(): Int = items.size
}
